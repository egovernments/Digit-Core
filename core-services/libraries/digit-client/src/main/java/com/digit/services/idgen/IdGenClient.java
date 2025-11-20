package com.digit.services.idgen;

import com.digit.config.ApiProperties;
import com.digit.exception.DigitClientException;
import com.digit.services.idgen.model.IdGenGenerateRequest;
import com.digit.services.idgen.model.GenerateIDResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Service client for IdGen API operations.
 * Provides methods to interact with the IdGen service.
 */
@Slf4j
@Getter
@Setter
public class IdGenClient {

    private final RestTemplate restTemplate;
    private final ApiProperties apiProperties;

    /**
     * Constructor for IdGenClient.
     *
     * @param restTemplate the RestTemplate for HTTP operations
     * @param apiProperties the API configuration properties
     */
    public IdGenClient(RestTemplate restTemplate, ApiProperties apiProperties) {
        this.restTemplate = restTemplate;
        this.apiProperties = apiProperties;

        // DEBUG: Check which RestTemplate we're using
        System.out.println("🔍 IdGenClient created with RestTemplate: " + restTemplate.getClass().getSimpleName());
        System.out.println("🔍 RestTemplate interceptors: " + restTemplate.getInterceptors().size());
        restTemplate.getInterceptors().forEach(interceptor -> {
            System.out.println("  - " + interceptor.getClass().getSimpleName());
        });
    }

    /**
     * Generates an ID using the specified template and variables.
     *
     * @param generateRequest the ID generation request
     * @return the generated ID as a String
     * @throws DigitClientException if generation fails
     */
    public String generateId(IdGenGenerateRequest generateRequest) {
        if (generateRequest == null) {
            throw new DigitClientException("IdGenGenerateRequest cannot be null");
        }

        if (generateRequest.getTemplateCode() == null || generateRequest.getTemplateCode().trim().isEmpty()) {
            throw new DigitClientException("Template code cannot be null or empty");
        }

        try {
            log.debug("Generating ID for templateCode: {}", generateRequest.getTemplateCode());
            
            String url = apiProperties.getIdgenServiceUrl() + "/idgen/v1/generate";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            
            HttpEntity<IdGenGenerateRequest> entity = new HttpEntity<>(generateRequest, headers);
            
            ResponseEntity<GenerateIDResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, GenerateIDResponse.class);
            
            GenerateIDResponse idResponse = response.getBody();
            String generatedId = idResponse != null ? idResponse.getId() : null;
            
            if (generatedId == null || generatedId.trim().isEmpty()) {
                throw new DigitClientException("Generated ID is null or empty");
            }
            
            log.debug("Successfully generated ID: {}", generatedId);
            
            return generatedId;
            
        } catch (Exception e) {
            log.error("Failed to generate ID for templateCode: {}", generateRequest.getTemplateCode(), e);
            if (e instanceof DigitClientException) {
                throw e;
            }
            throw new DigitClientException("Failed to generate ID: " + e.getMessage(), e);
        }
    }

    /**
     * Generates an ID with simplified parameters.
     *
     * @param templateCode the template code
     * @param variables the variables map
     * @return the generated ID as a String
     * @throws DigitClientException if generation fails
     */
    public String generateId(String templateCode, Map<String, String> variables) {
        IdGenGenerateRequest request = IdGenGenerateRequest.builder()
                .templateCode(templateCode)
                .variables(variables)
                .build();
        
        return generateId(request);
    }

    /**
     * Generates an ID with just template code (no variables).
     *
     * @param templateCode the template code
     * @return the generated ID as a String
     * @throws DigitClientException if generation fails
     */
    public String generateId(String templateCode) {
        IdGenGenerateRequest request = IdGenGenerateRequest.builder()
                .templateCode(templateCode)
                .build();
        
        return generateId(request);
    }
}
