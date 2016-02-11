package com.zenika.kafka.demo;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Simple class to report system metrics into kafka topic.
 */
public class MetricSystemProducer {

    private static final Logger LOG = LoggerFactory.getLogger(MetricSystemProducer.class);

    private static final int MB = 1024 * 1024;

    private KafkaProducer<Object, String> producer;

    private final JsonReaderWriter jsonReaderWriter = new JsonReaderWriter(); // not thread-safe

    /**
     * Creates a new {@link MetricSystemProducer} instance.
     */
    public MetricSystemProducer(List<String> bootstrapServers) {
        try {
            Properties config = new Properties();
            config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
            config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
            config.put(ProducerConfig.CLIENT_ID_CONFIG, InetAddress.getLocalHost().getHostName());
            config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, String.join(",", bootstrapServers));
            config.put(ProducerConfig.ACKS_CONFIG, "all");
            this.producer = new KafkaProducer<>(config);
        } catch (UnknownHostException e) {
            throw new RuntimeException("An error occurred while getting local hostname");
        }
    }

    public void send(Metrics metrics, String topic) {

        ProducerRecord<Object, String> record = new ProducerRecord<>(topic, null, jsonReaderWriter.toJson(metrics));

        // All writes are asynchronous by default.
        producer.send(record, (metadata, e) -> {
            if (e != null) LOG.error("Send failed for record {}", record, e);
        });
    }

    public void close() {
        LOG.info("Close producer");
        this.producer.close();
    }

    public static void main(String[] args) throws SigarException, InterruptedException {
        LOG.info("java.library.path={}", System.getProperty("java.library.path"));
        Sigar sigar = new Sigar();

        MetricSystemProducer producer = new MetricSystemProducer(Arrays.asList("localhost:9092"));
        producer.attachShutDownHook();

        while(true) {
            final Mem mem = sigar.getMem();
            final CpuPerc cpu = sigar.getCpuPerc();
            Metrics metric = new Metrics(
                    cpu.getUser() * 100,
                    cpu.getSys() * 100,
                    mem.getTotal() / MB,
                    mem.getActualUsed() / MB ,
                    mem.getUsed() / MB,
                    mem.getFree() / MB
            );
            LOG.info("Send {}", metric);
            producer.send(metric, "metrics-system");

            Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        }
    }

    public void attachShutDownHook(){
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (MetricSystemProducer.this.producer != null)
                    MetricSystemProducer.this.close();
                LOG.info("Shutdown {}", MetricSystemProducer.class.getSimpleName());
            }
        });
    }
}
