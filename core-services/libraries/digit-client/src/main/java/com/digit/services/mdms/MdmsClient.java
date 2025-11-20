package com.digit.services.mdms;

import com.digit.config.ApiProperties;
import com.digit.exception.DigitClientException;
import com.digit.services.mdms.model.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Set;

/**
 * Service client for MDMS API operations.
 * Provides methods to interact with the MDMS service.
 */
@Slf4j
@Getter
@Setter
public class MdmsClient {

    private final RestTemplate restTemplate;
    private final ApiProperties apiProperties;

    /**
     * Constructor for MdmsClient.
     *
     * @param restTemplate the RestTemplate for HTTP operations
     * @param apiProperties the API configuration properties
     */
    public MdmsClient(RestTemplate restTemplate, ApiProperties apiProperties) {
        this.restTemplate = restTemplate;
        this.apiProperties = apiProperties;

        // DEBUG: Check which RestTemplate we're using
        System.out.println("🔍 MdmsClient created with RestTemplate: " + restTemplate.getClass().getSimpleName());
        System.out.println("🔍 RestTemplate interceptors: " + restTemplate.getInterceptors().size());
        restTemplate.getInterceptors().forEach(interceptor -> {
            System.out.println("  - " + interceptor.getClass().getSimpleName());
        });
    }

    /**
     * Validates whether all provided unique identifiers exist for the given schema code.
     * Returns true if all unique identifiers are found, false if any are missing.
     * Tenant ID is automatically propagated via X-Tenant-ID header.
     *
     * @param schemaCode the schema code to validate against
     * @param uniqueIdentifiers the set of unique identifiers to validate
     * @return true if all unique identifiers are valid (found), false otherwise
     * @throws DigitClientException if request fails or input is invalid
     */
    public boolean isMdmsDataValid(String schemaCode, Set<String> uniqueIdentifiers) {
        if (schemaCode == null || schemaCode.trim().isEmpty()) {
            throw new DigitClientException("Schema code cannot be null or empty");
        }
        if (uniqueIdentifiers == null || uniqueIdentifiers.isEmpty()) {
            throw new DigitClientException("Unique identifiers set cannot be null or empty");
        }

        try {
            log.debug("Validating MDMS data for schemaCode: {}, uniqueIdentifiers: {} (tenant from X-Tenant-ID header)", 
                     schemaCode, uniqueIdentifiers);

            // Build URL with query parameters
            UriComponentsBuilder uriBuilder = UriComponentsBuilder
                    .fromHttpUrl(apiProperties.getMdmsServiceUrl() + "/mdms-v2/v2")
                    .queryParam("schemaCode", schemaCode);

            // Add each unique identifier as a separate query parameter
            for (String uniqueIdentifier : uniqueIdentifiers) {
                uriBuilder.queryParam("uniqueIdentifiers", uniqueIdentifier);
            }

            String url = uriBuilder.toUriString();
            log.debug("MDMS validation URL: {}", url);

            HttpHeaders headers = new HttpHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<MdmsResponseV2> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, MdmsResponseV2.class);

            List<Mdms> mdmsList = response.getBody() != null ? response.getBody().getMdms() : null;
            int foundCount = mdmsList != null ? mdmsList.size() : 0;

            boolean allValid = foundCount == uniqueIdentifiers.size();
            log.debug("MDMS validation result: {} ({} out of {} found)", 
                      allValid ? "valid" : "invalid", foundCount, uniqueIdentifiers.size());

            if (log.isDebugEnabled() && mdmsList != null) {
                log.debug("Found MDMS entries:");
                mdmsList.forEach(mdms -> log.debug("  - ID: {}, UniqueIdentifier: {}", 
                                                  mdms.getId(), mdms.getUniqueIdentifier()));
            }

            return allValid;

        } catch (Exception e) {
            log.error("Failed to validate MDMS data for schemaCode: {}, uniqueIdentifiers: {} (tenant from X-Tenant-ID header)", 
                     schemaCode, uniqueIdentifiers, e);
            if (e instanceof DigitClientException) {
                throw e;
            }
            throw new DigitClientException("Failed to validate MDMS data: " + e.getMessage(), e);
        }
    }

    /**
     * Searches MDMS data by schema code and unique identifiers.
     * Tenant ID is automatically propagated via X-Tenant-ID header.
     *
     * @param schemaCode the schema code
     * @param uniqueIdentifiers the set of unique identifiers to search for
     * @return the list of Mdms objects
     * @throws DigitClientException if search fails
     */
    public List<Mdms> searchMdmsData(String schemaCode, Set<String> uniqueIdentifiers) {
        if (schemaCode == null || schemaCode.trim().isEmpty()) {
            throw new DigitClientException("Schema code cannot be null or empty");
        }
        if (uniqueIdentifiers == null || uniqueIdentifiers.isEmpty()) {
            throw new DigitClientException("Unique identifiers set cannot be null or empty");
        }

        try {
            log.debug("Searching MDMS data for schemaCode: {}, uniqueIdentifiers: {} (tenant from X-Tenant-ID header)", 
                     schemaCode, uniqueIdentifiers);

            // Build URL with query parameters
            UriComponentsBuilder uriBuilder = UriComponentsBuilder
                    .fromHttpUrl(apiProperties.getMdmsServiceUrl() + "/mdms-v2/v2")
                    .queryParam("schemaCode", schemaCode);

            // Add each unique identifier as a separate query parameter
            for (String uniqueIdentifier : uniqueIdentifiers) {
                uriBuilder.queryParam("uniqueIdentifiers", uniqueIdentifier);
            }

            String url = uriBuilder.toUriString();

            HttpHeaders headers = new HttpHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<MdmsResponseV2> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, MdmsResponseV2.class);

            List<Mdms> mdmsList = response.getBody() != null ? response.getBody().getMdms() : null;
            log.debug("Successfully retrieved {} MDMS entries", mdmsList != null ? mdmsList.size() : 0);

            return mdmsList;

        } catch (Exception e) {
            log.error("Failed to search MDMS data for schemaCode: {}, uniqueIdentifiers: {} (tenant from X-Tenant-ID header)", 
                     schemaCode, uniqueIdentifiers, e);
            if (e instanceof DigitClientException) {
                throw e;
            }
            throw new DigitClientException("Failed to search MDMS data: " + e.getMessage(), e);
        }
    }
}
