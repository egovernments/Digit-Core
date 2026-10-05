package digit.kafka;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.tracer.kafka.CustomKafkaTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// NOTE: If tracer is disabled change CustomKafkaTemplate to KafkaTemplate in autowiring

@Service
@Slf4j
public class Producer {

    @Autowired
    private CustomKafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private MultiStateInstanceUtil multiStateInstanceUtil;

    public void push(String tenantId, String topic, Object value) {
        String updatedTopic = multiStateInstanceUtil.getStateSpecificTopicName(tenantId, topic);
        log.info("The Kafka topic for the tenantId : {} is : {}", tenantId, updatedTopic);
        kafkaTemplate.send(updatedTopic, value);
    }

    /**
     * Keyed publish: routes the message to a partition by {@code key} so all messages sharing a key
     * are ordered on the same partition. Used by the bulk path to key a batch by its parent code, so
     * batches of siblings under the same parent keep a deterministic per-parent order. A null key
     * falls back to the keyless (default-partitioner) behaviour of {@link #push(String, String, Object)}.
     */
    public void push(String tenantId, String topic, String key, Object value) {
        String updatedTopic = multiStateInstanceUtil.getStateSpecificTopicName(tenantId, topic);
        log.info("The Kafka topic for the tenantId : {} is : {}", tenantId, updatedTopic);
        if (key == null) {
            kafkaTemplate.send(updatedTopic, value);
        } else {
            kafkaTemplate.send(updatedTopic, key, value);
        }
    }
}
