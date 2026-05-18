package org.egov.user.kafka;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.tracer.kafka.CustomKafkaTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserProducer {

    @Autowired
    private CustomKafkaTemplate<String, Object> kafkaTemplate;


    public void push(String topic,String key,Object value) {
        log.info("message push to the topic : " + topic);
        kafkaTemplate.send(topic, key, value);
    }
}