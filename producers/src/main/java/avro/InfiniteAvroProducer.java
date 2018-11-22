package avro;

import com.google.common.util.concurrent.RateLimiter;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;


/***
 * start with:
 * mvn clean compile exec:java -Dexec.mainClass=avro.InfiniteAvroProducer -Dexec.args="10"
 */
public class InfiniteAvroProducer {

  private static final String TOPIC = "avro-stream-infinite";
  private static final int SEGMENTS_PER_RECORD = 10;
  private static final int FIELDS_PER_SEGMENT = 10;

  private static final AtomicLong ID_GENERATOR = new AtomicLong();

  // Report number of records sent every this many seconds.
  private static final long PROGRESS_REPORTING_INTERVAL = 5;

  private static Logger log = LoggerFactory.getLogger("InfiniteBigAvroProducer");
  private static final int NUMBER_OF_THREADS = Runtime.getRuntime().availableProcessors();
  private static final ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

  public static void main(String[] args) {

    if (args.length != 1) {
      throw new IllegalArgumentException("you need to supply one number that is maxRequestsPerSecond");
    }

    final int maxRequestsPerSecond = Integer.parseInt(args[0]);
    final RateLimiter rateLimiter = RateLimiter.create(maxRequestsPerSecond);

    Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
    props.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
    props.put(ProducerConfig.ACKS_CONFIG, "1");

    final KafkaProducer<GenericRecord, GenericRecord> producer = new KafkaProducer<>(props);
    Runtime.getRuntime().addShutdownHook(new Thread(producer::close, "Shutdown-thread"));

    String schemaPath = "src/main/resources/avro/bigavro.asvc";
    String keySchemaString = "{\"type\": \"record\",\"name\": \"key\",\"fields\":[{\"type\": \"string\",\"name\": \"key\"}]}}";
    String valueSchemaString = "";

    try {
      valueSchemaString = new String(Files.readAllBytes(Paths.get(schemaPath)));
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }

    Schema avroKeySchema = new Schema.Parser().parse(keySchemaString);
    Schema avroValueSchema = new Schema.Parser().parse(valueSchemaString);


    // Calculate the total number of records we expect to generate, an object
    // to keep track of the number of errors we encounter, and a latch that
    // will be signalled every time a "send" completes. This latch allows us
    // to wait for all sends to complete before terminating the program.
    AtomicLong errorCount = new AtomicLong();


    // Create a counter to track the number of records we've successfully
    // created so far.
    final AtomicLong successCount = new AtomicLong();

    // This callback will be invoked whenever a send completes. It reports any
    // errors (and bumps the error-count) and signals the latch as described above.
    Callback postSender = (recordMetadata, e) -> {
      if (e != null) {
        log.error("Error adding to topic", e);
        errorCount.incrementAndGet();
      } else {
        successCount.incrementAndGet();
      }
    };

    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    scheduler.scheduleAtFixedRate(
        () -> log.info("Successfully created {} Kafka records", successCount.get()),
        2, PROGRESS_REPORTING_INTERVAL, TimeUnit.SECONDS);

    log.info("start sending {} records per second", maxRequestsPerSecond);

    IntStream.range(0, NUMBER_OF_THREADS).forEach(ignore ->
        executorService.submit(() -> {
          while (true) {
            GenericRecord thisKeyRecord = new GenericData.Record(avroKeySchema);
            GenericRecord thisValueRecord = new GenericData.Record(avroValueSchema);
            for (int j = 0; j < SEGMENTS_PER_RECORD; j++) {
              GenericRecord nestedRecord = new GenericData.Record(avroValueSchema.getField("segment" + j).schema().getTypes().get(1));
              for (int i = 0; i < FIELDS_PER_SEGMENT; i++) {
                nestedRecord.put("segment" + j + "_" + i, Integer.toString(i));
              }
              thisValueRecord.put("segment" + j, nestedRecord);
            }
            thisKeyRecord.put("key", Long.toString(ID_GENERATOR.incrementAndGet()));

            rateLimiter.acquire();
            producer.send(new ProducerRecord<>(TOPIC, thisKeyRecord, thisValueRecord), postSender);
          }
        }));
  }
}
