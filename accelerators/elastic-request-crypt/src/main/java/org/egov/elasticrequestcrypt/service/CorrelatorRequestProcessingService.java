package org.egov.elasticrequestcrypt.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.elasticrequestcrypt.config.ApplicationProperties;
import org.egov.elasticrequestcrypt.models.PlainCorrelator;
import org.egov.elasticrequestcrypt.utils.EncryptionDecryptionUtil;
import org.egov.elasticrequestcrypt.utils.IndexingUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import static org.egov.elasticrequestcrypt.constants.CorrelatorConstants.*;

@Service
public class CorrelatorRequestProcessingService {

    private EncryptionDecryptionUtil encryptionDecryptionUtil;

    private IndexingUtil indexingUtil;

    private ObjectMapper objectMapper;

    private ApplicationProperties applicationProperties;

    @Autowired
    public CorrelatorRequestProcessingService(EncryptionDecryptionUtil encryptionDecryptionUtil, IndexingUtil indexingUtil, ObjectMapper objectMapper, ApplicationProperties applicationProperties) {
        this.encryptionDecryptionUtil = encryptionDecryptionUtil;
        this.indexingUtil = indexingUtil;
        this.objectMapper = objectMapper;
        this.applicationProperties = applicationProperties;
    }

    /**
     * This method takes plain correlator object as param, converts it into encrypted correlator object and
     * invokes indexing of this data on ElasticSearch for logging requests against their correlationId.
     * @param plainCorrelator
     */
    public void processEncryptedCorrelatorRequest(PlainCorrelator plainCorrelator) {
        // Make a call to encryption service to encrypt incoming request
        Object encryptedRequest = encryptionDecryptionUtil.encryptRequest(plainCorrelator.getEncryptionRequest());

        // Prepare encrypted correlator map
        Map<String, Object> encryptedCorrelatorMap = new HashMap<>();
        encryptedCorrelatorMap.put(CORRELATION_ID, plainCorrelator.getCorrelationId());
        encryptedCorrelatorMap.put(ENCRYPTED_REQUEST, encryptedRequest);

        // Index encrypted correlator information to ES
        indexingUtil.indexDataOnEs(applicationProperties.getCorrelationIndexName(), applicationProperties.getCorrelationIndexType(),  encryptedCorrelatorMap);

    }


}
