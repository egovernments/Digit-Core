package org.digit.services.idgen;

import org.digit.config.ApiProperties;
import org.digit.exception.DigitClientException;
import org.digit.services.idgen.model.GenerateIDResponse;
import org.digit.services.idgen.model.IdGenGenerateRequest;
import java.util.Map;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class IdGenClient {
    private final RestTemplate restTemplate;
    private final ApiProperties apiProperties;

    public IdGenClient(RestTemplate restTemplate, ApiProperties apiProperties) {
        this.restTemplate = restTemplate;
        this.apiProperties = apiProperties;
    }

    public String generateId(IdGenGenerateRequest request) {
        if (request == null) {
            throw new DigitClientException("IdGenGenerateRequest cannot be null");
        }
        if (request.getTemplateCode() == null || request.getTemplateCode().trim().isEmpty()) {
            throw new DigitClientException("Template code cannot be null or empty");
        }
        try {
            String generatedId;
            String url = this.apiProperties.getIdgenServiceUrl() + "/idgen/v3/generate";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            ResponseEntity response = this.restTemplate.exchange(url, HttpMethod.POST, new HttpEntity((Object)request, headers), GenerateIDResponse.class, new Object[0]);
            GenerateIDResponse idResponse = (GenerateIDResponse)response.getBody();
            String string = generatedId = idResponse != null ? idResponse.getId() : null;
            if (generatedId == null || generatedId.trim().isEmpty()) {
                throw new DigitClientException("Generated ID is null or empty");
            }
            return generatedId;
        }
        catch (Exception e) {
            if (e instanceof DigitClientException) {
                throw e;
            }
            throw new DigitClientException("Failed to generate ID: " + e.getMessage(), e);
        }
    }

    public String generateId(String templateCode, Map<String, String> variables) {
        return this.generateId(IdGenGenerateRequest.builder().templateCode(templateCode).variables(variables).build());
    }

    public String generateId(String templateCode) {
        return this.generateId(IdGenGenerateRequest.builder().templateCode(templateCode).build());
    }
}