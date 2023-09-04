package org.egov.elasticrequestcrypt.utils;

import org.egov.elasticrequestcrypt.config.ApplicationProperties;
import org.egov.elasticrequestcrypt.repository.ServiceRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.egov.elasticrequestcrypt.constants.CorrelatorConstants.SLASH_SEPARATOR;

@Component
public class IndexingUtil {

    private ApplicationProperties applicationProperties;

    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    public IndexingUtil(ApplicationProperties applicationProperties, ServiceRequestRepository serviceRequestRepository) {
        this.applicationProperties = applicationProperties;
        this.serviceRequestRepository = serviceRequestRepository;
    }

    /**
     * This method takes indexName, type and data to index it on ElasticSearch.
     * @param indexName
     * @param type
     * @param dataToBeIndexed
     * @return
     */
    public Object indexDataOnEs(String indexName, String type, Map<String, Object> dataToBeIndexed) {
        return serviceRequestRepository.fetchResult(getEsPostUri(indexName, type), dataToBeIndexed);
    }

    /**
     * This method takes indexName and type and returns the endpoint for indexing data on the provided index.
     * @param indexName
     * @param type
     * @return
     */
    private StringBuilder getEsPostUri(String indexName, String type) {
        return new StringBuilder(applicationProperties.getEsHost()).append(indexName).append(SLASH_SEPARATOR).append(type);
    }

}
