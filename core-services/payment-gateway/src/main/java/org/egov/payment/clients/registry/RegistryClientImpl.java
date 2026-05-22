package org.egov.payment.clients.registry;

import lombok.extern.slf4j.Slf4j;
import org.egov.payment.clients.registry.models.DataSearchRequest;
import org.egov.payment.clients.registry.models.DataSearchResponse;
import org.egov.payment.clients.registry.models.RegistryData;
import org.egov.payment.config.AppProperties;
import org.egov.tracer.model.CustomException;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class RegistryClientImpl implements RegistryClient {

    private final RestClient restClient;
    private final AppProperties appProperties;

    public RegistryClientImpl(RestClient restClient, AppProperties appProperties) {
        this.restClient = restClient;
        this.appProperties = appProperties;
    }

    @Override
    public List<RegistryData> search(String tenantId, String clientId, String schemaCode, DataSearchRequest request) {
        String uri = UriComponentsBuilder
                .fromHttpUrl(appProperties.getRegistryHost())
                .path(appProperties.getRegistryDataSearchPath())
                .buildAndExpand(schemaCode)
                .toUriString();

        try {
            DataSearchResponse response = restClient.post()
                    .uri(uri)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header("X-Tenant-ID", tenantId)
                    .header("X-Client-ID", clientId)
                    .body(request)
                    .retrieve()
                    .body(DataSearchResponse.class);

            if (response == null || !Boolean.TRUE.equals(response.getSuccess())) {
                throw new CustomException("REGISTRY_SEARCH_ERROR",
                        "Registry search failed: " + (response != null ? response.getError() : "null response"));
            }

            List<RegistryData> records = response.getData();
            log.info("Registry search returned {} records for schemaCode: {}",
                    records != null ? records.size() : 0, schemaCode);

            return records != null ? records : Collections.emptyList();

        } catch (HttpClientErrorException e) {
            log.error("Registry client error for schemaCode: {}, status={}", schemaCode, e.getStatusCode(), e);
            throw new ServiceCallException(e.getResponseBodyAsString());
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Registry unknown error for schemaCode: {}", schemaCode, e);
            throw new CustomException("REGISTRY_ERROR", "Failed to search registry: " + e.getMessage());
        }
    }
}
