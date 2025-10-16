package org.egov.encryption;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.encryption.config.EncProperties;
import org.egov.encryption.config.ErrorConstants;
import org.egov.encryption.web.contract.EncReqObject;
import org.egov.encryption.web.contract.EncryptionRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
class EncryptionServiceRestConnection {

    @Autowired
    private EncProperties encProperties;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ObjectMapper objectMapper;


    Object callEncrypt(String tenantId, String type, Object value) throws IOException {
        EncReqObject encReqObject = new EncReqObject(tenantId, type, value);
        EncryptionRequest encryptionRequest = new EncryptionRequest();
        encryptionRequest.setEncryptionRequests(new ArrayList<>(Collections.singleton(encReqObject)));

        try {
            Map<String, String> tenantNamespaceMap = Arrays.stream(encProperties.getEncSeparateTenantsNamespace().split(","))
                    .map(entry -> entry.split(":"))
                    .collect(Collectors.toMap(e -> e[0].trim(), e -> e[1].trim()));

            List<String> separateTenants = Arrays.asList(encProperties.getEncSeparateTenants().split(","));
            String encServiceUrl;
            if (separateTenants.contains(tenantId) && tenantNamespaceMap.containsKey(tenantId)) {
                String namespace = tenantNamespaceMap.get(tenantId);
                encServiceUrl = encProperties.getEncServiceUrlPattern().replace("{namespace}", namespace);
            } else {
                encServiceUrl = encProperties.getEgovEncHost();
            }
            ResponseEntity<String> response = restTemplate.postForEntity(encServiceUrl + encProperties.getEgovEncEncryptPath(),
                    encryptionRequest, String.class);
            return objectMapper.readTree(response.getBody()).get(0);
        } catch (Exception e) {
            log.error(ErrorConstants.ENCRYPTION_SERVICE_ERROR_MESSAGE, e);
            throw new CustomException(ErrorConstants.ENCRYPTION_SERVICE_ERROR, ErrorConstants.ENCRYPTION_SERVICE_ERROR_MESSAGE);
        }
    }

    JsonNode callDecrypt(Object ciphertext, String tenantId) {
        try {
            Map<String, String> tenantNamespaceMap = Arrays.stream(encProperties.getEncSeparateTenantsNamespace().split(","))
                    .map(entry -> entry.split(":"))
                    .collect(Collectors.toMap(e -> e[0].trim(), e -> e[1].trim()));

            List<String> separateTenants = Arrays.asList(encProperties.getEncSeparateTenants().split(","));
            String encServiceUrl;
            if (separateTenants.contains(tenantId) && tenantNamespaceMap.containsKey(tenantId)) {
                String namespace = tenantNamespaceMap.get(tenantId);
                encServiceUrl = encProperties.getEncServiceUrlPattern().replace("{namespace}", namespace);
            } else {
                encServiceUrl = encProperties.getEgovEncHost();
            }
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                    encServiceUrl + encProperties.getEgovEncDecryptPath(), ciphertext, JsonNode.class);
            return response.getBody();
        } catch (Exception e) {
            throw new CustomException(ErrorConstants.ENCRYPTION_SERVICE_ERROR, ErrorConstants.ENCRYPTION_SERVICE_ERROR_MESSAGE);
        }
    }

}