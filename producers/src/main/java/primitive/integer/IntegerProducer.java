package primitive.integer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.serialization.IntegerSerializer;

import java.util.Random;
import java.time.Duration;
import java.time.Instant;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class IntegerProducer {

    private static final String TOPIC = "integer_stream";

    private static final int TOTAL_RECORDS = 1000;

    // Report number of records sent every this many seconds.
    private static final long PROGRESS_REPORTING_INTERVAL_IN_SECONDS = 1;

    private static Logger log = LoggerFactory.getLogger("PrimitiveProducer");

    public static void main(String[] args) throws InterruptedException {

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "1");

        // Start a timer to measure how long this run takes overall.
        Instant start = Instant.now();

        final KafkaProducer<Integer, String> producer = new KafkaProducer<>(props);

        Runtime.getRuntime().addShutdownHook(new Thread(producer::close, "Shutdown-thread"));

        AtomicLong errorCount = new AtomicLong();
        CountDownLatch requestLatch = new CountDownLatch(TOTAL_RECORDS);

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
            requestLatch.countDown();
        };

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(
                ()->log.info("Successfully created {} Kafka records", successCount.get()),
                2, PROGRESS_REPORTING_INTERVAL_IN_SECONDS, TimeUnit.SECONDS);

        String[] continents = {"North America", "South America", "Africa", "Asia", "Europe", "Antartica", "Australia"};

        Random rand = new Random();

        for (int i = 0; i < TOTAL_RECORDS; i++) {
            producer.send(new ProducerRecord<>(TOPIC, i, continents[rand.nextInt(continents.length)]), postSender);
        }

        // Wait for sends to complete.
        requestLatch.await();

        // Stop the thread that periodically reports progress.
        scheduler.shutdown();
        producer.close();

        long duration = Duration.between(start, Instant.now()).getSeconds();
        log.info("Completed loading {}/{} records to Kafka in {} seconds",
                TOTAL_RECORDS - errorCount.get(), TOTAL_RECORDS, duration);
    }
}

