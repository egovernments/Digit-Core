/*
package org.egov.tracer.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;

import java.util.function.BiFunction;

@Configuration
public class DeadLetterQueueConfiguration {



    @Bean
    @ConditionalOnProperty(name = "tracer.errorsPublish", havingValue = "true")
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(KafkaOperations<Object, Object> kafkaTemplate) {
        BiFunction<ConsumerRecord<?, ?>, Exception, TopicPartition> destinationResolver = (cr, e) ->
                new TopicPartition(cr.topic() + ".dlt", cr.partition());

        return new DeadLetterPublishingRecoverer(kafkaTemplate, destinationResolver);
    }

    @Bean
    @ConditionalOnProperty(name = "tracer.errorsPublish", havingValue = "true")
    public DefaultErrorHandler errorHandler(DeadLetterPublishingRecoverer recoverer) {
        return new DefaultErrorHandler(recoverer);
    }



}
*/
