package org.egov.sunbirdrc.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.egov.sunbirdrc.service.CredentialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@Slf4j
public class Consumer {

    @Autowired
    private CredentialService credentialService;
    ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = {"save-tl-tradelicense", "update-tl-tradelicense"})
    public void listen(String message, ConsumerRecord<String, String> record) {
        try {
            credentialService.processMessageFromKafka(message);
        } catch (Exception e) {
            e.printStackTrace();
            // Handle parsing error
        }

    }

}
