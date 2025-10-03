package org.egov.infra.indexer.util;

import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.kafka.ErrorQueueProducer;
import org.egov.tracer.model.ErrorQueueContract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

@Component
@Slf4j
public class DLQHandler {

    @Autowired
    private ErrorQueueProducer errorQueueProducer;

    @Value("${tracer.errorsTopic}")
    private String errorTopic;

    @Value("${tracer.errorsPublish}")
    private boolean publishErrors;

    /**
     * Complete error handling logic for all message listeners
     * @param messageBody The original kafka message
     * @param exception The exception that occurred
     * @param source The source/listener name
     */
    public void handleError(String messageBody, Exception exception, String source) {
        // Always log the error first
        log.error("Error while processing in {}: ", source, exception);
        
        // Send directly to DLQ using tracer's ErrorQueueProducer
        if (publishErrors) {
            try {
                String correlationId = extractCorrelationId(messageBody);
                sendToDLQ(messageBody, exception, correlationId, source);
                log.info("Successfully sent failed message to DLQ topic: {} from {}", errorTopic, source);
            } catch (Exception dlqException) {
                log.error("Failed to send message to DLQ: ", dlqException);
                // Log both the original exception and DLQ failure
                log.error("Original processing error: ", exception);
            }
        }
        
        // Always re-throw to maintain consistent error handling behavior
        // The tracer at container level will handle retry/DLQ based on configuration
        throw new RuntimeException("Failed to process message in " + source, exception);
    }

    /**
     * Send failed message to DLQ using tracer's ErrorQueueProducer
     */
    private void sendToDLQ(String messageBody, Exception exception, String correlationId, String source) {
        try {
            // Create ErrorQueueContract using the correct field names from tracer library
            StackTraceElement[] elements = exception.getStackTrace();
            
            ErrorQueueContract errorContract = ErrorQueueContract.builder()
                    .id(UUID.randomUUID().toString())
                    .correlationId(correlationId)
                    .body(messageBody)
                    .source(source)
                    .ts(new Date().getTime())
                    .exception(Arrays.asList(elements))
                    .message(exception.getMessage())
                    .build();
            
            // Send to DLQ using tracer's ErrorQueueProducer
            errorQueueProducer.sendMessage(errorContract);
        } catch (Exception e) {
            log.error("Failed to create or send ErrorQueueContract to DLQ", e);
            throw e;
        }
    }

    /**
     * Extract correlation ID from Kafka message body
     */
    private String extractCorrelationId(String messageBody) {
        try {
            // Try to extract correlation ID from RequestInfo
            return JsonPath.read(messageBody, "$.RequestInfo.correlationId");
        } catch (Exception e) {
            try {
                // Fallback: try to extract from different path
                return JsonPath.read(messageBody, "$.correlationId");
            } catch (Exception ex) {
                log.debug("Could not extract correlation ID from message: {}", ex.getMessage());
                return null;
            }
        }
    }
}