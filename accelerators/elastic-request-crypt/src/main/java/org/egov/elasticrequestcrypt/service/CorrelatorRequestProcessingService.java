package org.egov.elasticrequestcrypt.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.elasticrequestcrypt.config.ApplicationProperties;
import org.egov.elasticrequestcrypt.models.HttpRequestLog;
import org.egov.elasticrequestcrypt.utils.EncryptionDecryptionInvocationUtil;
import org.egov.elasticrequestcrypt.utils.IndexingInvocationUtil;
import org.egov.encryption.web.contract.EncReqObject;
import org.egov.encryption.web.contract.EncryptionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

import static org.egov.elasticrequestcrypt.constants.CorrelatorConstants.ENCRYPTION_TYPE_NORMAL;

@Slf4j
@Service
public class CorrelatorRequestProcessingService {

    private EncryptionDecryptionInvocationUtil encryptionDecryptionInvocationUtil;

    private IndexingInvocationUtil indexingInvocationUtil;

    private ObjectMapper objectMapper;

    private ApplicationProperties applicationProperties;

    @Autowired
    public CorrelatorRequestProcessingService(EncryptionDecryptionInvocationUtil encryptionDecryptionInvocationUtil, IndexingInvocationUtil indexingInvocationUtil, ObjectMapper objectMapper, ApplicationProperties applicationProperties) {
        this.encryptionDecryptionInvocationUtil = encryptionDecryptionInvocationUtil;
        this.indexingInvocationUtil = indexingInvocationUtil;
        this.objectMapper = objectMapper;
        this.applicationProperties = applicationProperties;
    }

    /**
     * This method takes httpRequestLog object as param, encrypts request body and request headers
     * invokes indexing of this data on ElasticSearch for logging requests with their correlationId.
     * @param httpRequestLog
     */
    public void processHttpLogRequest(HttpRequestLog httpRequestLog) {
        // Create list of encryption request objects for encrypting request body and headers
        List<EncReqObject> encReqObjects = new ArrayList<>();
        encReqObjects.add(encryptionDecryptionInvocationUtil.getEncReqObject(applicationProperties.getEncRootTenantId(), ENCRYPTION_TYPE_NORMAL, httpRequestLog.getBody()));
        encReqObjects.add(encryptionDecryptionInvocationUtil.getEncReqObject(applicationProperties.getEncRootTenantId(), ENCRYPTION_TYPE_NORMAL, httpRequestLog.getHeaders()));

        // Make a call to encryption service to encrypt incoming request
        List<JsonNode> encryptedRequest = encryptionDecryptionInvocationUtil.encryptRequest(EncryptionRequest.builder()
                .encryptionRequests(encReqObjects)
                .build());

        // Set encrypted request body and request headers in the http request log
        httpRequestLog.setBody(encryptedRequest.get(0).toString());
        httpRequestLog.setHeaders(objectMapper.convertValue(encryptedRequest.get(1), Map.class));


        // Prepare encrypted correlator map
        Map<String, Object> encryptedCorrelatorMap = objectMapper.convertValue(httpRequestLog, Map.class);

        // Index encrypted correlator information to ES
        indexingInvocationUtil.indexDataOnEs(applicationProperties.getCorrelationIndexName(), applicationProperties.getCorrelationIndexType(),  encryptedCorrelatorMap);

    }

}
