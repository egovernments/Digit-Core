package com.example.demo.listner;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.apache.kafka.common.TopicPartition;
import java.util.function.BiFunction;
import org.springframework.kafka.listener.DefaultErrorHandler;

@Configuration
public class DeadLetterQueueConfiguration {



    @Bean
    @ConditionalOnProperty(name = "feature.enabled", havingValue = "true")
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(KafkaOperations<Object, Object> kafkaTemplate) {
        BiFunction<ConsumerRecord<?, ?>, Exception, TopicPartition> destinationResolver = (cr, e) ->
                new TopicPartition(cr.topic() + ".dlt", cr.partition());

        return new DeadLetterPublishingRecoverer(kafkaTemplate, destinationResolver);
    }

    @Bean
    @ConditionalOnProperty(name = "feature.enabled", havingValue = "true")
    public DefaultErrorHandler errorHandler(DeadLetterPublishingRecoverer recoverer) {
        return new DefaultErrorHandler(recoverer);
    }



}
