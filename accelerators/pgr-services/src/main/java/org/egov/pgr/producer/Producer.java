package org.egov.pgr.producer;

import lombok.extern.slf4j.Slf4j;
import org.egov.pgr.config.PGRConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class Producer {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    public void push(String tenantId, String topic, Object value) {

       // String updatedTopic = centralInstanceUtil.getStateSpecificTopicName(tenantId, topic);
        log.info("The Kafka topic for the tenantId : " + tenantId + " is : " + topic);
        kafkaTemplate.send(topic, value);
    }
}
