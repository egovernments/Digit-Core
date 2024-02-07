package org.egov.tracer.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.SerializationException;
import org.egov.tracer.config.ObjectMapperFactory;
import org.egov.tracer.config.TracerProperties;
import org.egov.tracer.model.ErrorDetailDTO;
import org.egov.tracer.model.ErrorQueueContract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class ErrorQueueProducer {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private TracerProperties tracerProperties;

    @Autowired
    private ObjectMapperFactory objectMapperFactory;

    public void sendMessage(ErrorQueueContract errorQueueContract) {
        try {
            log.info("Sending message to topic - " + tracerProperties.getErrorsTopic());
            kafkaTemplate.send(tracerProperties.getErrorsTopic(), errorQueueContract);
        } catch (SerializationException serializationException) {
            log.info("SerializationException exception occurred while sending exception to error queue");
            try {
                kafkaTemplate.send(tracerProperties.getErrorsTopic(), objectMapperFactory.getObjectMapper().writeValueAsString
                        (errorQueueContract));
            } catch (JsonProcessingException e) {
                log.info("exception occurred while converting ErrorQueueContract to json string");
            }
        } catch (Exception ex) {
            log.error("exception occurred while sending exception to error queue");
        }
    }

    public void sendErrorDetails(List<ErrorDetailDTO> errorDetailList) {
        try {
            log.info("Sending message to topic - " + "error-details-indexer-topic");
            kafkaTemplate.send(tracerProperties.getErrorDetailsTopic(), errorDetailList);
        } catch (SerializationException serializationException) {
            log.info("SerializationException exception occurred while sending exception to error queue");
            try {
                kafkaTemplate.send(tracerProperties.getErrorDetailsTopic(), objectMapperFactory.getObjectMapper().writeValueAsString(errorDetailList));
            } catch (JsonProcessingException e) {
                log.info("exception occurred while converting error details to json string");
            }
        } catch (Exception ex) {
            log.error("exception occurred while sending exception to error queue");
        }
    }

}
