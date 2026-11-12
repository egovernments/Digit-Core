package digit.kafka;

import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.kafka.CustomKafkaTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// NOTE: If tracer is disabled change CustomKafkaTemplate to KafkaTemplate in autowiring

@Service
@Slf4j
public class Producer {

    @Autowired
    private CustomKafkaTemplate<String, Object> kafkaTemplate;

    public void push(String topic, Object value) {
        kafkaTemplate.send(topic, value);
    }

    /**
     * Keyed publish: routes the message to a partition by {@code key} so all messages sharing a key
     * are ordered on the same partition. Used by the bulk path to key a batch by its parent code, so
     * batches of siblings under the same parent keep a deterministic per-parent order. A null key
     * falls back to the keyless (default-partitioner) behaviour of {@link #push(String, Object)}.
     */
    public void push(String topic, String key, Object value) {
        if (key == null) {
            kafkaTemplate.send(topic, value);
        } else {
            kafkaTemplate.send(topic, key, value);
        }
    }
}
