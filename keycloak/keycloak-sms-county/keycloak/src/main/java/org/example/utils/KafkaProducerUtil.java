package org.example.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class KafkaProducerUtil {

    private static KafkaProducerUtil instance;
    private final Producer<String, Object> producer;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String smsTopic;

    // Private constructor to enforce singleton pattern
    private KafkaProducerUtil() {
        Properties props = new Properties();

        // Read configuration from environment variables
        props.put("bootstrap.servers", getEnv("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092"));
        props.put("key.serializer", getEnv("KAFKA_KEY_SERIALIZER", "org.apache.kafka.common.serialization.StringSerializer"));
        props.put("value.serializer", getEnv("KAFKA_VALUE_SERIALIZER", "org.apache.kafka.common.serialization.StringSerializer"));
        props.put("retries", getEnv("KAFKA_RETRIES_CONFIG", "0"));
        props.put("batch.size", getEnv("KAFKA_BATCH_SIZE_CONFIG", "16384"));
        props.put("linger.ms", getEnv("KAFKA_LINGER_MS_CONFIG", "1"));
        props.put("buffer.memory", getEnv("KAFKA_BUFFER_MEMORY_CONFIG", "33554432"));

        this.producer = new KafkaProducer<>(props);
        this.smsTopic = getEnv("KAFKA_SMS_TOPIC", "egov.core.notification.sms");
    }

    // Singleton accessor
    public static synchronized KafkaProducerUtil getInstance() {
        if (instance == null) {
            instance = new KafkaProducerUtil();
        }
        return instance;
    }

    // Get environment variable or default value
    private static String getEnv(String name, String defaultValue) {
        String value = System.getenv(name);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }

    // Send a message to Kafka
    public void sendSmsMessage(String key, Object value) {
        try {
            String jsonValue = objectMapper.writeValueAsString(value);
            ProducerRecord<String, Object> record = new ProducerRecord<>(smsTopic, key, jsonValue);
            producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    exception.printStackTrace();
                } else {
                    System.out.println("Message sent to topic: " + metadata.topic() + " with offset: " + metadata.offset());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Close the producer
    public void close() {
        if (producer != null) {
            producer.close();
            System.out.println("Kafka Producer closed");
        }
    }
}
