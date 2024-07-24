package org.egov.sunbirdrc.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.egov.sunbirdrc.service.CredentialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Consumer {

    @Autowired
    private CredentialService credentialService;


    @KafkaListener(topics = {"create-vc","recreate-vc"})
    public void createVc(String message, ConsumerRecord<String, String> record) {
            credentialService.processPayloadAndPersistCredential(message,record.topic());
    }

}
