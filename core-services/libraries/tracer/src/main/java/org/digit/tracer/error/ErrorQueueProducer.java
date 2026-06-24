package org.digit.tracer.error;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.digit.tracer.config.TracerProperties;
import org.digit.tracer.model.ErrorDetailDTO;
import org.digit.tracer.model.ErrorQueueContract;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;

public class ErrorQueueProducer {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ErrorQueueProducer.class);

    @SuppressWarnings("rawtypes")
    private final KafkaTemplate kafkaTemplate;
    private final TracerProperties properties;
    private final ObjectMapper objectMapper;

    @SuppressWarnings("rawtypes")
    public ErrorQueueProducer(KafkaTemplate kafkaTemplate,
                              TracerProperties properties,
                              ObjectMapper objectMapper) {
        this.kafkaTemplate  = kafkaTemplate;
        this.properties     = properties;
        this.objectMapper   = objectMapper;
    }

    public void sendError(ErrorQueueContract contract) {
        if (!properties.errorsPublish()) return;
        try {
            kafkaTemplate.send(properties.errorsTopic(), contract).get();
            log.info("Published error to topic={} id={}", properties.errorsTopic(), contract.id());
        } catch (Exception ex) {
            // The shared application KafkaTemplate may be configured with a serializer that cannot
            // handle a POJO value (e.g. StringSerializer). Fall back to a JSON string we serialize
            // ourselves, which any String/byte serializer accepts.
            if (isSerializationFailure(ex)) {
                sendAsString(properties.errorsTopic(), contract);
                return;
            }
            log.error("Failed to publish error to Kafka topic={}", properties.errorsTopic(), ex);
        }
    }

    public void sendErrorDetails(List<ErrorDetailDTO> details) {
        if (!properties.shouldPublishErrorDetails()) return;
        try {
            kafkaTemplate.send(properties.errorDetailsTopic(), details).get();
            log.info("Published {} error detail(s) to topic={}", details.size(), properties.errorDetailsTopic());
        } catch (Exception ex) {
            if (isSerializationFailure(ex)) {
                sendAsString(properties.errorDetailsTopic(), details);
                return;
            }
            log.error("Failed to publish error details to Kafka topic={}", properties.errorDetailsTopic(), ex);
        }
    }

    // Fallback: serialize to a JSON string and send as plain string when the configured Kafka
    // value serializer cannot handle the object value.
    private void sendAsString(String topic, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(topic, json).get();
            log.info("Published error payload as JSON string to topic={}", topic);
        } catch (Exception ex) {
            log.error("Fallback string publish also failed for topic={}", topic, ex);
        }
    }

    // A value-serializer mismatch surfaces as a Kafka SerializationException (often caused by a
    // ClassCastException), or a Jackson JsonProcessingException — anywhere in the cause chain.
    private static boolean isSerializationFailure(Throwable ex) {
        for (Throwable t = ex; t != null; t = t.getCause()) {
            if (t instanceof SerializationException
                || t instanceof JsonProcessingException
                || t instanceof ClassCastException) {
                return true;
            }
        }
        return false;
    }
}
