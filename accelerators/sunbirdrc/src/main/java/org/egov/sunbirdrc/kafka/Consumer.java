package org.egov.sunbirdrc.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.egov.sunbirdrc.service.CredentialService;
import org.egov.sunbirdrc.service.DidGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Consumer {

    @Autowired
    private CredentialService credentialService;

    @Autowired
    private DidGenerationService didGenerationService;


    @KafkaListener(topics = {"create-vc"})
    public void listen(String message, ConsumerRecord<String, String> record) throws JsonProcessingException {
            credentialService.processTradeLicensePayload(message);
    }

}
