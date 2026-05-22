package org.egov.payment.config;

import lombok.extern.slf4j.Slf4j;
import org.egov.payment.messaging.producer.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * Resolves the Producer bean based on message.broker.enabled and message.broker.type.
 * Default when disabled: NoOpMessageProducer.
 */
@Configuration
@Slf4j
public class MessagingConfig {

    private final AppProperties appProperties;

    @Autowired
    public MessagingConfig(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Bean
    public Producer producer(
            @Autowired(required = false) KafkaTemplate<String, Object> kafkaTemplate,
            @Autowired(required = false) RedisTemplate<String, Object> redisTemplate) {

        if (!Boolean.TRUE.equals(appProperties.getMessageBrokerEnabled())) {
            log.info("Message broker disabled. Using NoOpMessageProducer.");
            return new NoOpMessageProducer();
        }

        String brokerType = appProperties.getMessageBrokerType();
        if ("KAFKA".equalsIgnoreCase(brokerType)) {
            if (kafkaTemplate == null) {
                throw new IllegalStateException("message.broker.type=KAFKA but KafkaTemplate is not configured");
            }
            log.info("Message broker enabled with type: KAFKA");
            return new KafkaProducer(kafkaTemplate);
        } else if ("REDIS".equalsIgnoreCase(brokerType)) {
            if (redisTemplate == null) {
                throw new IllegalStateException("message.broker.type=REDIS but RedisTemplate is not configured");
            }
            log.info("Message broker enabled with type: REDIS");
            return new RedisProducer(redisTemplate);
        }

        log.warn("Unknown message.broker.type: {}. Falling back to NoOpMessageProducer.", brokerType);
        return new NoOpMessageProducer();
    }
}
