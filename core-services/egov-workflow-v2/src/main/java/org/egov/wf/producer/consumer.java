

package org.egov.wf.producer;

        import com.fasterxml.jackson.databind.ObjectMapper;
        import lombok.extern.slf4j.Slf4j;
        import org.springframework.beans.factory.annotation.Autowired;
        import org.springframework.kafka.annotation.KafkaListener;
        import org.springframework.kafka.support.KafkaHeaders;
        import org.springframework.messaging.handler.annotation.Header;
        import org.springframework.stereotype.Service;

        import java.util.HashMap;

        import static org.apache.kafka.common.requests.FetchMetadata.log;

@Service
@Slf4j
public class consumer {


    @Autowired
    private ObjectMapper mapper;


    /**
     * Consumes the water connection record and send notification
     *
     * @param record
     * @param topic
     */

    @KafkaListener(topics = { "${persister.save.businessservice.wf.topic}"})
    public void listen(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            log.info(mapper.writeValueAsString(record));

        } catch (Exception ex) {
            StringBuilder builder = new StringBuilder("Error while listening to value: ").append(record)
                    .append("on topic: ").append(topic);
            log.error(builder.toString(), ex);
        }
    }
}

