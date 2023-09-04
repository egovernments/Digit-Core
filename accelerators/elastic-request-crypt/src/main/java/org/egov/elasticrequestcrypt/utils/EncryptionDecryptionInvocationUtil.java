package org.egov.elasticrequestcrypt.utils;

import org.egov.elasticrequestcrypt.config.ApplicationProperties;
import org.egov.elasticrequestcrypt.repository.ServiceRequestRepository;
import org.egov.encryption.web.contract.EncryptionRequest;
import org.springframework.stereotype.Component;

@Component
public class EncryptionDecryptionInvocationUtil {

    private ServiceRequestRepository serviceRequestRepository;

    private ApplicationProperties applicationProperties;

    public EncryptionDecryptionInvocationUtil(ServiceRequestRepository serviceRequestRepository, ApplicationProperties applicationProperties) {
        this.serviceRequestRepository = serviceRequestRepository;
        this.applicationProperties = applicationProperties;
    }

    /**
     * This method takes encryption request as param and calls encryption service to encrypt the incoming payload.
     * @param encryptionRequest
     * @return
     */
    public Object encryptRequest(EncryptionRequest encryptionRequest) {
       return serviceRequestRepository.fetchResult(getEncUri(), encryptionRequest);
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
}
