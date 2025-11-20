package com.digit.services.individual;

import com.digit.config.ApiProperties;
import com.digit.exception.DigitClientException;
import com.digit.services.individual.model.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


/**
 * Service client for Individual API operations.
 * Provides methods to interact with the Individual service.
 */
@Slf4j
@Getter
@Setter
public class IndividualClient {

    private final RestTemplate restTemplate;
    private final ApiProperties apiProperties;

    // Default values for pagination
    private static final int DEFAULT_LIMIT = 10;
    private static final int DEFAULT_OFFSET = 0;

    /**
     * Constructor for IndividualClient.
     *
     * @param restTemplate the RestTemplate for HTTP operations
     * @param apiProperties the API configuration properties
     */
    public IndividualClient(RestTemplate restTemplate, ApiProperties apiProperties) {
        this.restTemplate = restTemplate;
        this.apiProperties = apiProperties;

        log.debug("IndividualClient created with RestTemplate: {}", restTemplate.getClass().getSimpleName());
    }

    /**
     * Creates a new individual.
     *
     * @param individual the individual to create
     * @return the created Individual object
     * @throws DigitClientException if creation fails
     */
    public Individual createIndividual(Individual individual) {
        if (individual == null) {
            throw new DigitClientException("Individual cannot be null");
        }

        try {
            log.debug("Creating individual: {}", individual.getGivenName());
            String url = apiProperties.getIndividualServiceUrl() + "/individual/v1";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            
            IndividualRequest request = IndividualRequest.builder()
                    .individual(individual)
                    .build();
            HttpEntity<IndividualRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<IndividualResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, IndividualResponse.class);
            
            Individual createdIndividual = response.getBody() != null ? response.getBody().getIndividual() : null;
            log.debug("Successfully created individual with ID: {}", 
                    createdIndividual != null ? createdIndividual.getIndividualId() : "null");
            return createdIndividual;
            
        } catch (Exception e) {
            log.error("Failed to create individual", e);
            if (e instanceof DigitClientException) {
                throw e;
            }
            throw new DigitClientException("Failed to create individual: " + e.getMessage(), e);
        }
    }

    /**
     * Gets an individual by ID.
     *
     * @param individualId the ID of the individual to retrieve
     * @return the Individual object
     * @throws DigitClientException if retrieval fails
     */
    public Individual getIndividualById(String individualId) {
        if (individualId == null || individualId.trim().isEmpty()) {
            throw new DigitClientException("Individual ID cannot be null or empty");
        }

        try {
            log.debug("Getting individual with ID: {}", individualId);
            String url = apiProperties.getIndividualServiceUrl() + "/individual/v1/" + individualId;
            
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<IndividualResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, IndividualResponse.class);
            
            Individual individual = response.getBody() != null ? response.getBody().getIndividual() : null;
            log.debug("Successfully retrieved individual: {}", individualId);
            return individual;
            
        } catch (Exception e) {
            log.error("Failed to get individual with ID: {}", individualId, e);
            if (e instanceof DigitClientException) {
                throw e;
            }
            throw new DigitClientException("Failed to get individual: " + e.getMessage(), e);
        }
    }

    /**
     * Searches individuals by name with default pagination.
     *
     * @param individualName the name to search for
     * @return the IndividualSearchResponse containing list of individuals and total count
     * @throws DigitClientException if search fails
     */
    public IndividualSearchResponse searchIndividualsByName(String individualName) {
        return searchIndividualsByName(individualName, DEFAULT_LIMIT, DEFAULT_OFFSET);
    }

    /**
     * Searches individuals by name with custom pagination.
     *
     * @param individualName the name to search for
     * @param limit the maximum number of results to return
     * @param offset the offset for pagination
     * @return the IndividualSearchResponse containing list of individuals and total count
     * @throws DigitClientException if search fails
     */
    public IndividualSearchResponse searchIndividualsByName(String individualName, Integer limit, Integer offset) {
        try {
            log.debug("Searching individuals with name: {}, limit: {}, offset: {}", 
                    individualName, limit, offset);
            
            // Use default values if not provided
            int finalLimit = (limit != null && limit > 0) ? limit : DEFAULT_LIMIT;
            int finalOffset = (offset != null && offset >= 0) ? offset : DEFAULT_OFFSET;
            
            StringBuilder urlBuilder = new StringBuilder(apiProperties.getIndividualServiceUrl() + "/individual/v1?");
            
            if (individualName != null && !individualName.trim().isEmpty()) {
                urlBuilder.append("individualName=").append(individualName).append("&");
            }
            urlBuilder.append("limit=").append(finalLimit).append("&");
            urlBuilder.append("offset=").append(finalOffset);
            
            String url = urlBuilder.toString();
            
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<IndividualSearchResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, IndividualSearchResponse.class);
            
            IndividualSearchResponse searchResponse = response.getBody();
            log.debug("Successfully retrieved {} individuals", 
                    searchResponse != null && searchResponse.getIndividuals() != null ? 
                            searchResponse.getIndividuals().size() : 0);
            return searchResponse;
            
        } catch (Exception e) {
            log.error("Failed to search individuals with name: {}", individualName, e);
            if (e instanceof DigitClientException) {
                throw e;
            }
            throw new DigitClientException("Failed to search individuals: " + e.getMessage(), e);
        }
    }

    /**
     * Searches all individuals with default pagination.
     *
     * @return the IndividualSearchResponse containing list of individuals and total count
     * @throws DigitClientException if search fails
     */
    public IndividualSearchResponse searchAllIndividuals() {
        return searchIndividualsByName(null, DEFAULT_LIMIT, DEFAULT_OFFSET);
    }

    /**
     * Searches all individuals with custom pagination.
     *
     * @param limit the maximum number of results to return
     * @param offset the offset for pagination
     * @return the IndividualSearchResponse containing list of individuals and total count
     * @throws DigitClientException if search fails
     */
    public IndividualSearchResponse searchAllIndividuals(Integer limit, Integer offset) {
        return searchIndividualsByName(null, limit, offset);
    }

    /**
     * Checks if an individual exists with the given ID.
     *
     * @param individualId the ID to check
     * @return true if individual exists, false otherwise
     * @throws DigitClientException if search fails
     */
    public boolean isIndividualExist(String individualId) {
        return isIndividualExistsById(individualId, DEFAULT_LIMIT, DEFAULT_OFFSET);
    }

    /**
     * Checks if an individual exists with the given ID with custom pagination.
     *
     * @param individualId the ID to check
     * @param limit the maximum number of results to return
     * @param offset the offset for pagination
     * @return true if individual exists, false otherwise
     * @throws DigitClientException if search fails
     */
    public boolean isIndividualExistsById(String individualId, Integer limit, Integer offset) {
        try {
            log.debug("Searching individuals with name: {}, limit: {}, offset: {}",
                    individualId, limit, offset);

            // Use default values if not provided
            int finalLimit = (limit != null && limit > 0) ? limit : DEFAULT_LIMIT;
            int finalOffset = (offset != null && offset >= 0) ? offset : DEFAULT_OFFSET;

            StringBuilder urlBuilder = new StringBuilder(apiProperties.getIndividualServiceUrl() + "/individual/v1?");

            if (individualId != null && !individualId.trim().isEmpty()) {
                urlBuilder.append("individualId=").append(individualId).append("&");
            }
            urlBuilder.append("limit=").append(finalLimit).append("&");
            urlBuilder.append("offset=").append(finalOffset);

            String url = urlBuilder.toString();

            HttpHeaders headers = new HttpHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<IndividualSearchResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, IndividualSearchResponse.class);

            IndividualSearchResponse searchResponse = response.getBody();
            boolean exists = searchResponse != null && searchResponse.getTotalCount() != null && searchResponse.getTotalCount() > 0;
            log.debug("Individual with ID {} {}", individualId, exists ? "exists" : "does not exist");
            return exists;

        } catch (Exception e) {
            log.error("Failed to check if individual exists with ID: {}", individualId, e);
            if (e instanceof DigitClientException) {
                throw e;
            }
            throw new DigitClientException("Failed to check if individual exists: " + e.getMessage(), e);
        }
    }
}
