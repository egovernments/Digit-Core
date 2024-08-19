package digit.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;

import digit.util.UserUtil;
import digit.web.models.Tenant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class TenantConsumer {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private UserUtil userUtil;


    @KafkaListener(topics = { "${kafka.topics.create.tenant}"})
    public void listen(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        Tenant tenant = mapper.convertValue(record, Tenant.class);
        userUtil.createUser(tenant);
    }

}
