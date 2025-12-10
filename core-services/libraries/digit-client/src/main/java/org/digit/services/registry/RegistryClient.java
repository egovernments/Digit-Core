package org.digit.services.registry;

import org.digit.config.ApiProperties;
import org.digit.exception.DigitClientException;
import org.digit.services.registry.model.RegistryData;
import org.digit.services.registry.model.RegistryDataResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Service client for Registry API operations.
 * Provides methods to interact with the Registry service.
 */
@Slf4j
@Getter
@Setter
public class RegistryClient {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RegistryClient.class);

    private final RestTemplate restTemplate;
    private final ApiProperties apiProperties;

    /**
     * Constructor for RegistryClient.
     *
     * @param restTemplate the RestTemplate for HTTP operations
     * @param apiProperties the API configuration properties
     */
    public RegistryClient(RestTemplate restTemplate, ApiProperties apiProperties) {
        this.restTemplate = restTemplate;
        this.apiProperties = apiProperties;
    }

    /**
     * Creates registry data with the specified registry data object.
     *
     * @param registryData the RegistryData object to be created
     * @return the created RegistryData object
     * @throws DigitClientException if creation fails
     */
    public RegistryData createRegistryData(RegistryData registryData) {
        if (registryData == null) {
            throw new DigitClientException("Registry data cannot be null");
        }
        if (registryData.getSchemaCode() == null || registryData.getSchemaCode().trim().isEmpty()) {
            throw new DigitClientException("Schema code cannot be null or empty");
        }
        if (registryData.getData() == null) {
            throw new DigitClientException("Data cannot be null");
        }

        try {
            log.debug("Creating registry data with schema code: {}", registryData.getSchemaCode());
            String url = apiProperties.getRegistryServiceUrl() + "/registry/v1/schema/" + registryData.getSchemaCode() + "/data";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            
            HttpEntity<RegistryData> entity = new HttpEntity<>(registryData, headers);
            
            ResponseEntity<RegistryDataResponse> response = restTemplate.postForEntity(url, entity, RegistryDataResponse.class);
            RegistryData createdData = null;
            if (response.getBody() != null && response.getBody().getRegistryData() != null && !response.getBody().getRegistryData().isEmpty()) {
                createdData = response.getBody().getRegistryData().get(0);
            }
            
            log.debug("Successfully created registry data with schema code: {}", registryData.getSchemaCode());
            return createdData;
            
        } catch (Exception e) {
            log.error("Failed to create registry data with schema code: {}", registryData.getSchemaCode(), e);
            if (e instanceof DigitClientException) {
                throw e;
            }
            throw new DigitClientException("Failed to create registry data: " + e.getMessage(), e);
        }
    }

    /**
     * Searches for registry data by schema code and registry ID.
     *
     * @param schemaCode the schema code for the registry
     * @param registryId the registry ID to search for
     * @return the RegistryData object if found
     * @throws DigitClientException if the data is not found or an error occurs
     */
    public RegistryData searchRegistryData(String schemaCode, String registryId) {
        if (schemaCode == null || schemaCode.trim().isEmpty()) {
            throw new DigitClientException("Schema code cannot be null or empty");
        }
        if (registryId == null || registryId.trim().isEmpty()) {
            throw new DigitClientException("Registry ID cannot be null or empty");
        }

        try {
            log.debug("Searching registry data with schema code: {} and registry ID: {}", schemaCode, registryId);
            String url = apiProperties.getRegistryServiceUrl() + "/registry/v1/schema/" + schemaCode + "/data/_registry?registryId=" + registryId + "&history=true";
            
            HttpHeaders headers = new HttpHeaders();
            
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<RegistryDataResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, RegistryDataResponse.class);
            RegistryData registryData = null;
            if (response.getBody() != null && response.getBody().getRegistryData() != null && !response.getBody().getRegistryData().isEmpty()) {
                registryData = response.getBody().getRegistryData().get(0);
            }
            
            log.debug("Successfully retrieved registry data with schema code: {} and registry ID: {}", schemaCode, registryId);
            return registryData;
            
        } catch (Exception e) {
            log.error("Failed to retrieve registry data with schema code: {} and registry ID: {}", schemaCode, registryId, e);
            if (e instanceof DigitClientException) {
                throw e;
            }
            throw new DigitClientException("Failed to retrieve registry data: " + e.getMessage(), e);
        }
    }
}