package com.digit.services.filestore;

import com.digit.config.ApiProperties;
import com.digit.exception.DigitClientException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Service client for Filestore API operations.
 * Provides methods to interact with the Filestore service.
 */
@Slf4j
@Getter
@Setter
public class FilestoreClient {

    private final RestTemplate restTemplate;
    private final ApiProperties apiProperties;

    /**
     * Constructor for FilestoreClient.
     *
     * @param restTemplate the RestTemplate for HTTP operations
     * @param apiProperties the API configuration properties
     */
    public FilestoreClient(RestTemplate restTemplate, ApiProperties apiProperties) {
        this.restTemplate = restTemplate;
        this.apiProperties = apiProperties;

        log.debug("FilestoreClient created with RestTemplate: {}", restTemplate.getClass().getSimpleName());
    }

    /**
     * Gets a file by ID and tenant ID.
     *
     * @param fileId the ID of the file to retrieve
     * @param tenantId the tenant ID
     * @return true if file exists (200 OK), false otherwise
     * @throws DigitClientException if retrieval fails due to client error
     */
    public boolean isFileAvailable(String fileId, String tenantId) {
        if (fileId == null || fileId.trim().isEmpty()) {
            throw new DigitClientException("File ID cannot be null or empty");
        }
        if (tenantId == null || tenantId.trim().isEmpty()) {
            throw new DigitClientException("Tenant ID cannot be null or empty");
        }

        try {
            log.debug("Getting file with ID: {} for tenant: {}", fileId, tenantId);
            String url = apiProperties.getFilestoreServiceUrl() + "/filestore/v1/files/" + fileId + "?tenantId=" + tenantId;
            
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class);
            
            boolean success = response.getStatusCode() == HttpStatus.OK;
            log.debug("File retrieval for ID: {} returned status: {}", fileId, response.getStatusCode());
            return success;
            
        } catch (Exception e) {
            log.error("Failed to get file with ID: {} for tenant: {}", fileId, tenantId, e);
            return false;
        }
    }

    /**
     * Gets a file by ID and tenant ID with additional validation.
     * This method throws exceptions for client errors but returns false for server errors.
     *
     * @param fileId the ID of the file to retrieve
     * @param tenantId the tenant ID
     * @return true if file exists (200 OK), false for server errors
     * @throws DigitClientException if retrieval fails due to client error (4xx)
     */
    public boolean validateFileAvailability(String fileId, String tenantId) {
        if (fileId == null || fileId.trim().isEmpty()) {
            throw new DigitClientException("File ID cannot be null or empty");
        }
        if (tenantId == null || tenantId.trim().isEmpty()) {
            throw new DigitClientException("Tenant ID cannot be null or empty");
        }

        try {
            log.debug("Getting file with validation - ID: {} for tenant: {}", fileId, tenantId);
            String url = apiProperties.getFilestoreServiceUrl() + "/filestore/v1/files/" + fileId + "?tenantId=" + tenantId;
            
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class);
            
            boolean success = response.getStatusCode() == HttpStatus.OK;
            log.debug("File retrieval with validation for ID: {} returned status: {}", fileId, response.getStatusCode());
            return success;
            
        } catch (Exception e) {
            log.error("Failed to get file with ID: {} for tenant: {}", fileId, tenantId, e);
            if (e.getMessage() != null && (e.getMessage().contains("400") || e.getMessage().contains("401") || 
                e.getMessage().contains("403") || e.getMessage().contains("404"))) {
                throw new DigitClientException("Client error while retrieving file: " + e.getMessage(), e);
            }
            return false;
        }
    }
}
