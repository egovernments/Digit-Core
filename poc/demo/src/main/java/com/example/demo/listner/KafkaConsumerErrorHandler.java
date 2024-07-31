package com.example.demo.listner;


import com.example.demo.model.ErrorQueueContract;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class KafkaConsumerErrorHandler extends DefaultErrorHandler {

    //@Autowired
    //private ExceptionAdvise exceptionAdvise;

    @Value("${tracer.errorsPublish}")
    private boolean sendErrorsToKafka;

    public KafkaConsumerErrorHandler(DeadLetterPublishingRecoverer deadLetterPublishingRecoverer) {
        super(deadLetterPublishingRecoverer);
    }

    @Override
    public void handleRemaining (Exception thrownException, List<ConsumerRecord<?, ?>> records,
                                Consumer<?, ?> consumer, MessageListenerContainer container) {
        //super.handleOne(thrownException, record, consumer, container);
        System.out.println("ENTERED HANDLEONE");

        ObjectMapper objectMapper = new ObjectMapper();
        String body = null;
        try {
          //  body = objectMapper.writeValueAsString(record.value());
        } catch (Exception ex) {
            ex.printStackTrace();
            //log.error("KafkaConsumerErrorHandler: Error parsing Kafka record to JSON", ex);
        }
        Object requestBody = body;



        StackTraceElement elements[] = thrownException.getStackTrace();

        ErrorQueueContract errorQueueContract = ErrorQueueContract.builder()
                .id(UUID.randomUUID().toString())
                .correlationId(MDC.get("CORRELATION_ID_MDC"))
                .body(requestBody)
                .source("source")
                .ts(new Date().getTime())
                .errorRes(null)
                .exception(Arrays.asList(elements))
                .message(thrownException.getMessage())
                .build();
        System.out.println("ERROR OBJECT CREATED");
    }

    @Override
    public boolean handleOne(Exception thrownException, ConsumerRecord<?, ?> record, Consumer<?, ?> consumer,
                             MessageListenerContainer container) {
        super.handleOne(thrownException, record, consumer, container);
        System.out.println("Entered handleOne");
        return true;
    }
}
