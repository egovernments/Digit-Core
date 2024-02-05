package org.egov.tracer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Component
@Slf4j
public class KafkaConsumerErrorHandler extends DefaultErrorHandler {

    @Autowired
    private ExceptionAdvise exceptionAdvise;

    @Value("${tracer.errorsPublish}")
    private boolean sendErrorsToKafka;

    @Override
    public void handleRemaining (Exception thrownException, List<ConsumerRecord<?, ?>> records,
                                 Consumer<?, ?> consumer, MessageListenerContainer container) {
        if (sendErrorsToKafka) {
            records.forEach(record -> {
                log.error("Error while processing1: " + ObjectUtils.nullSafeToString(record), thrownException);
                ObjectMapper objectMapper = new ObjectMapper();
                String body = null;
                try {
                    body = objectMapper.writeValueAsString(record.value());
                } catch (Exception ex) {
                    log.error("KafkaConsumerErrorHandller Kafka consumer can not parse json data " + ex.getMessage());
                }
                exceptionAdvise.sendErrorMessage(body, thrownException, record.topic(), false);
            });
        }
    }
}
