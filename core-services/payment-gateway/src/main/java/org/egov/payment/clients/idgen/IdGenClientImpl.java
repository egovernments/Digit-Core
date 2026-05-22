package org.egov.payment.clients.idgen;

import lombok.extern.slf4j.Slf4j;
import org.egov.payment.clients.idgen.models.GenerateIdRequest;
import org.egov.payment.clients.idgen.models.GenerateIdResponse;
import org.egov.payment.config.AppProperties;
import org.egov.tracer.model.CustomException;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Component
public class IdGenClientImpl implements IdGenClient {

    private final RestClient restClient;
    private final AppProperties appProperties;

    public IdGenClientImpl(RestClient restClient, AppProperties appProperties) {
        this.restClient = restClient;
        this.appProperties = appProperties;
    }

    @Override
    public String generateId(String tenantId, String templateCode, Map<String, String> variables) {
        GenerateIdRequest requestBody = GenerateIdRequest.builder()
                .templateCode(templateCode)
                .variables(variables)
                .build();

        try {
            GenerateIdResponse response = restClient.post()
                    .uri(appProperties.getIdGenHost() + appProperties.getIdGenGenerateIdPath())
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header("X-Tenant-ID", tenantId)
                    .body(requestBody)
                    .retrieve()
                    .body(GenerateIdResponse.class);

            if (response == null || response.getId() == null || response.getId().isBlank()) {
                throw new CustomException("IDGEN_EMPTY_ID", "IDGen returned empty or null ID");
            }

            log.info("Generated ID: {} for templateCode: {}", response.getId(), templateCode);
            return response.getId();

        } catch (HttpClientErrorException e) {
            log.error("IDGen client error: status={}", e.getStatusCode(), e);
            throw new ServiceCallException(e.getResponseBodyAsString());
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("IDGen unknown error", e);
            throw new CustomException("IDGEN_ERROR", "Failed to generate ID: " + e.getMessage());
        }
    }
}
