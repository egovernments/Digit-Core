package org.egov.user.kafka;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.tracer.kafka.CustomKafkaTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Generic Kafka producer for egov-user. Supports tenant-aware topic names
 * (central instance: state-topicname) and flat topic names when tenantId is null or empty.
 */
@Slf4j
@Service("userKafkaProducer")
public class Producer {

    private final CustomKafkaTemplate<String, Object> kafkaTemplate;
    private final MultiStateInstanceUtil centralInstanceUtil;

    @Autowired
    public Producer(CustomKafkaTemplate<String, Object> kafkaTemplate, 
                   MultiStateInstanceUtil centralInstanceUtil) {
        this.kafkaTemplate = kafkaTemplate;
        this.centralInstanceUtil = centralInstanceUtil;
    }

    /**
     * Pushes a message to the given topic. If tenantId is non-null and non-empty and
     * the environment is central instance, the topic is prefixed with the state part
     * (e.g. statea-egov.hrms.employee.create.error.dlq). Otherwise the topic is used as-is.
     *
     * @param tenantId optional tenant ID for tenant-aware topic naming; null or empty for flat topic
     * @param topic    base topic name (e.g. from application.properties)
     * @param value    payload to send
     */
    public void push(String tenantId, String topic, Object value) {
        String resolvedTopic = topic;
        if (tenantId != null && !tenantId.trim().isEmpty()) {
            resolvedTopic = centralInstanceUtil.getStateSpecificTopicName(tenantId, topic);
            log.debug("Kafka topic for tenantId {} resolved to {}", tenantId, resolvedTopic);
        }
        
        try {
            kafkaTemplate.send(resolvedTopic, value);
            log.info("Message sent successfully to topic: {}", resolvedTopic);
        } catch (Exception e) {
            log.error("Failed to send message to topic: " + resolvedTopic, e);
            throw e;
        }
    }
}
