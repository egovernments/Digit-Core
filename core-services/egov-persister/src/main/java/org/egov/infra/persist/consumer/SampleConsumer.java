/*
package org.egov.infra.persist.consumer;



import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import org.egov.infra.persist.service.PersistService;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


@Component
@Slf4j
public class SampleConsumer {

    @Autowired
    private PersistService persistService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KafkaTemplate kafkaTemplate;

    @Value("${audit.persist.kafka.topic}")
    private String persistAuditKafkaTopic;

    @Value("${audit.generate.kafka.topic}")
    private String auditGenerateKafkaTopic;
    */
/**
     * Consumes record and send notification
     *
     * @param record
     * @param topic
     *//*


    @KafkaListener(topicPattern = "${pgr.kafka.notification.topic.pattern}")
    public void listen(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        String rcvData = null;

        try {
            rcvData = objectMapper.writeValueAsString(record);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize incoming message", e);
        }
        persistService.persist(topic,rcvData);

        if(!topic.equalsIgnoreCase(persistAuditKafkaTopic)){
            Map<String, Object> producerRecord = new HashMap<>();
            producerRecord.put("topic", topic);
            producerRecord.put("value", record);
            kafkaTemplate.send(auditGenerateKafkaTopic, producerRecord);
        }
    }
}

*/
