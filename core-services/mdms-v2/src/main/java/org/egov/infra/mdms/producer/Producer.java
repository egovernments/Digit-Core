package org.egov.infra.mdms.producer;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.tracer.kafka.CustomKafkaTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Producer {

    private final CustomKafkaTemplate<String, Object> kafkaTemplate;

    private final MultiStateInstanceUtil multiStateInstanceUtil;

    @Autowired
    public Producer(CustomKafkaTemplate<String, Object> kafkaTemplate, MultiStateInstanceUtil multiStateInstanceUtil) {
        this.kafkaTemplate = kafkaTemplate;
        this.multiStateInstanceUtil = multiStateInstanceUtil;
    }


    /**
     * Publishes a message to a Kafka topic after building a tenant-specific topic name.
     *
     * @param tenantId The unique identifier of the tenant used to determine the tenant-specific topic name.
     * @param topic The base topic name to which the message will be published.
     * @param value The message value to be sent to the Kafka topic.
     */
    public void push(String tenantId, String topic, Object value) {
        String updatedTopic = multiStateInstanceUtil.getStateSpecificTopicName(tenantId, topic);
        log.info("The Kafka topic for the tenantId : {} is : {}", tenantId, updatedTopic);
        this.kafkaTemplate.send(updatedTopic, value);
    }

}