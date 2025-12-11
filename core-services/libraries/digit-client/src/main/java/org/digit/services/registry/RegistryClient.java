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
     * @param schemaCode the schema code (used as path parameter)
     * @param registryData the RegistryData object to be created
     * @return the response from registry service
     * @throws DigitClientException if creation fails
     */
    public RegistryDataResponse createRegistryData(String schemaCode, RegistryData registryData) {
        if (registryData == null) {
            throw new DigitClientException("Registry data cannot be null");
        }
        if (schemaCode == null || schemaCode.trim().isEmpty()) {
            throw new DigitClientException("Schema code cannot be null or empty");
        }
        if (registryData.getData() == null) {
            throw new DigitClientException("Data cannot be null");
        }

        try {
            log.debug("Creating registry data with schema code: {}", schemaCode);
            String url = apiProperties.getRegistryServiceUrl() + "/registry/v1/schema/" + schemaCode + "/data";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            
            HttpEntity<RegistryData> entity = new HttpEntity<>(registryData, headers);
            
            ResponseEntity<RegistryDataResponse> response = restTemplate.postForEntity(url, entity, RegistryDataResponse.class);
            
            log.debug("Successfully created registry data with schema code: {}", schemaCode);
            return response.getBody();
            
        } catch (Exception e) {
            log.error("Failed to create registry data with schema code: {}", schemaCode, e);
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
     * @param history whether to include history in the search
     * @return the response from registry service
     * @throws DigitClientException if the data is not found or an error occurs
     */
    public RegistryDataResponse searchRegistryData(String schemaCode, String registryId, boolean history) {
        if (schemaCode == null || schemaCode.trim().isEmpty()) {
            throw new DigitClientException("Schema code cannot be null or empty");
        }
        if (registryId == null || registryId.trim().isEmpty()) {
            throw new DigitClientException("Registry ID cannot be null or empty");
        }

        try {
            log.debug("Searching registry data with schema code: {}, registry ID: {}, and history: {}", schemaCode, registryId, history);
            String url = apiProperties.getRegistryServiceUrl() + "/registry/v1/schema/" + schemaCode + "/data/_registry?registryId=" + registryId + "&history=" + history;
            
            HttpHeaders headers = new HttpHeaders();
            
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<RegistryDataResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, RegistryDataResponse.class);
            
            log.debug("Successfully retrieved registry data with schema code: {}, registry ID: {}, and history: {}", schemaCode, registryId, history);
            return response.getBody();
            
        } catch (Exception e) {
            log.error("Failed to retrieve registry data with schema code: {}, registry ID: {}, and history: {}", schemaCode, registryId, history, e);
            if (e instanceof DigitClientException) {
                throw e;
            }
            throw new DigitClientException("Failed to retrieve registry data: " + e.getMessage(), e);
        }
    }

    /**
     * Searches for registry data by schema code and registry ID with history enabled by default.
     *
     * @param schemaCode the schema code for the registry
     * @param registryId the registry ID to search for
     * @return the response from registry service
     * @throws DigitClientException if the data is not found or an error occurs
     */
    public RegistryDataResponse searchRegistryData(String schemaCode, String registryId) {
        return searchRegistryData(schemaCode, registryId, true);
    }
}