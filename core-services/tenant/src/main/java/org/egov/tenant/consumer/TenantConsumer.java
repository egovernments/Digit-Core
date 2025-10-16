package org.egov.tenant.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.tenant.utility.UserUtility;
import org.egov.tenant.web.contract.Tenant;
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
    private UserUtility userUtility;


    @KafkaListener(topics = { "${tenant.create.topic}"})
    public void listen(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        Tenant tenant = mapper.convertValue(record, Tenant.class);
        userUtility.createUser(tenant);
    }

}
