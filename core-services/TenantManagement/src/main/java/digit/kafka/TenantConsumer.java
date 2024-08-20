package digit.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;

import digit.util.UserUtil;
import digit.web.models.Tenant;
import digit.web.models.TenantRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Objects;

@Component
public class TenantConsumer {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private UserUtil userUtil;

    @KafkaListener(topics = {"${kafka.topics.create.tenant}"})
    public void listen(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        TenantRequest tenantRequest = mapper.convertValue(record, TenantRequest.class);

        // create user only for root tenant
        if (Objects.isNull(tenantRequest.getTenant().getTenantId()))
            userUtil.createUser(tenantRequest);

        // TODO: Trigger email/sms OTP here
    }

}
