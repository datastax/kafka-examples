package json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.RateLimiter;
import json.ticks.TickData;
import json.ticks.TickGenerator;
import json.utils.ExchangeUtils;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.connect.json.JsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * start with:
 * mvn clean compile exec:java -Dexec.mainClass=json.InfiniteJsonProducer -Dexec.args="5 topic-1 localhost:9092"
 */

public class InfiniteJsonProducer {

  private static final int RECORDS_PER_STOCK = 2_000_000;
  private static final int NUMBER_OF_STOCKS = 500;
  // Report number of records sent every this many seconds.
  private static final long PROGRESS_REPORTING_INTERVAL = 5;

  private static Logger log = LoggerFactory.getLogger("JsonProducer");

  public static void main(String[] args) {

    if (args.length != 3) {
      throw new IllegalArgumentException("" +
          "you need to supply " +
          "[1]: number that is maxRequestsPerSecond, " +
          "[2]: string topic name" +
          "[3]: kafka bootstrap.servers");
    }

    final int maxRequestsPerSecond = Integer.parseInt(args[0]);
    final String topicName = args[1];
    final RateLimiter rateLimiter = RateLimiter.create(maxRequestsPerSecond);

    Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, args[2]);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class.getName());
    props.put(ProducerConfig.ACKS_CONFIG, "1");

    final KafkaProducer<String, JsonNode> producer = new KafkaProducer<>(props);

    TickGenerator generator = new TickGenerator(ExchangeUtils.getExchangeData());

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

    ObjectMapper mapper = new ObjectMapper();

    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    scheduler.scheduleAtFixedRate(
        () -> log.info("Successfully created {} Kafka records", successCount.get()),
        2, PROGRESS_REPORTING_INTERVAL, TimeUnit.SECONDS);


    while (true) {
      for (int j = 0; j < RECORDS_PER_STOCK; j++) {
        for (int i = 0; i < NUMBER_OF_STOCKS; i++) {
          TickData tickData = generator.getStockWithRandomValue(i);
          tickData.setDateTime();
          rateLimiter.acquire();
          try {
            producer.send(new ProducerRecord<>(topicName, tickData.getName(), mapper.valueToTree(tickData)), postSender);
          } catch (Exception ex) {
            log.warn("problem when send - ignoring", ex);
          }
        }
      }
    }
  }
  //block indefinitely
}