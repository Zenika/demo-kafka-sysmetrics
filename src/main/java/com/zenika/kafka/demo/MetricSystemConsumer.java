package com.zenika.kafka.demo;

import com.google.common.collect.Lists;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Simple class to consume and report metrics into InfluxDB.
 */
public class MetricSystemConsumer implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(MetricSystemConsumer.class);

    private KafkaConsumer<Object, String> consumer;

    private final List<String> topics;
    private final AtomicBoolean shutdown;
    private final CountDownLatch shutdownLatch;
    private final InfluxDBRepository repository;

    private final JsonReaderWriter jsonReaderWriter = new JsonReaderWriter(); // not thread-safe

    /**
     * Creates a new {@link MetricSystemConsumer} instance.
     * @param config
     * @param topics
     */
    public MetricSystemConsumer(Properties config, List<String> topics, InfluxDBRepository repository) {
        this.consumer = new KafkaConsumer<>(config);
        this.topics = topics;
        this.shutdown = new AtomicBoolean(false);
        this.shutdownLatch = new CountDownLatch(1);
        this.repository = repository;
    }

    public void run() {
        try {
            consumer.subscribe(topics);

            while (!shutdown.get()) {
                ConsumerRecords<Object, String> records = consumer.poll(500);
                records.forEach(record -> save(record.value()));
            }
        }catch (Exception e) {
            LOG.error("Unexpected error occurred while consuming records", e);

        } finally {
            consumer.close();
            shutdownLatch.countDown();
        }
    }

    public void save(String json) {
        LOG.info(json);
        repository.write(jsonReaderWriter.fromJson(json));
    }

    public void shutdown() throws InterruptedException {
        shutdown.set(true);
        shutdownLatch.await();
    }

    public static void main(String[] args) throws UnknownHostException, InterruptedException {

        Properties config = new Properties();
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        config.put(ConsumerConfig.CLIENT_ID_CONFIG, InetAddress.getLocalHost().getHostName());
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "consumer-loggr");

        InfluxDBRepository repository = new InfluxDBRepository("http://localhost:8086", "kafka", "root", "root");
        repository.open();

        ExecutorService service = Executors.newSingleThreadExecutor();

        MetricSystemConsumer consumer = new MetricSystemConsumer(config, Lists.newArrayList("metrics-system"), repository);

        service.submit(consumer);

        service.awaitTermination(5, TimeUnit.MINUTES);

        consumer.shutdown();
        service.shutdownNow();

        LOG.info("Shutdown metric consumer");
    }
}
