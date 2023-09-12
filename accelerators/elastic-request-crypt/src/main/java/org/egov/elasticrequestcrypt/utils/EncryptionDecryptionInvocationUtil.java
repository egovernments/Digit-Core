package org.egov.elasticrequestcrypt.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.elasticrequestcrypt.config.ApplicationProperties;
import org.egov.elasticrequestcrypt.repository.ServiceRequestRepository;
import org.egov.encryption.web.contract.EncReqObject;
import org.egov.encryption.web.contract.EncryptionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class EncryptionDecryptionInvocationUtil {

    private ServiceRequestRepository serviceRequestRepository;

    private ApplicationProperties applicationProperties;

    private ObjectMapper objectMapper;

    @Autowired
    public EncryptionDecryptionInvocationUtil(ServiceRequestRepository serviceRequestRepository, ApplicationProperties applicationProperties, ObjectMapper objectMapper) {
        this.serviceRequestRepository = serviceRequestRepository;
        this.applicationProperties = applicationProperties;
        this.objectMapper = objectMapper;
    }

    /**
     * This method takes encryption request as param and calls encryption service to encrypt the incoming payload.
     * @param encryptionRequest
     * @return
     */
    public List<JsonNode> encryptRequest(EncryptionRequest encryptionRequest) {
        ResponseEntity<String> responseEntity = serviceRequestRepository.fetchResult(getEncUri(), encryptionRequest);
        List<JsonNode> jsonNodes = new ArrayList<>();
        try {
            // Convert the JSON response string to a List of JsonNode objects
             jsonNodes = objectMapper.readValue(responseEntity.getBody(), new TypeReference<List<JsonNode>>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return jsonNodes;
    }

    /**
     * This method creates encryption service URI to invoke encryption of incoming payload.
     * @return
     */
    private StringBuilder getEncUri() {
        return new StringBuilder(applicationProperties.getEncHost())
                .append(applicationProperties.getEncContextPath())
                .append(applicationProperties.getEncEndpoint());
    }

    /**
     * This method takes tenantId, encryptionType and value to be encrypted as parameters and
     * forges encReqObjects out of these params.
     * @param tenantId
     * @param encryptionType
     * @param value
     * @return
     */
    public EncReqObject getEncReqObject(String tenantId, String encryptionType, Object value) {
        return EncReqObject.builder()
                .tenantId(tenantId)
                .type(encryptionType)
                .value(value)
                .build();
    }

}
