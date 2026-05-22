package org.egov.payment.clients.individual;

import lombok.extern.slf4j.Slf4j;
import org.egov.payment.clients.individual.models.*;
import org.egov.payment.config.AppProperties;
import org.egov.tracer.model.CustomException;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class IndividualClientImpl implements IndividualClient {

    private final RestClient restClient;
    private final AppProperties appProperties;

    public IndividualClientImpl(RestClient restClient, AppProperties appProperties) {
        this.restClient = restClient;
        this.appProperties = appProperties;
    }

    @Override
    public Individual create(String tenantId, String clientId, Individual individual) {
        IndividualRequest requestBody = IndividualRequest.builder()
                .individual(individual)
                .build();

        try {
            IndividualResponse response = restClient.post()
                    .uri(appProperties.getIndividualHost() + appProperties.getIndividualCreatePath())
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header("X-Tenant-ID", tenantId)
                    .header("X-Client-ID", clientId)
                    .body(requestBody)
                    .retrieve()
                    .body(IndividualResponse.class);

            if (response == null || response.getIndividual() == null) {
                throw new CustomException("INDIVIDUAL_CREATE_ERROR", "Individual creation returned empty response");
            }

            log.info("Individual created with id: {}", response.getIndividual().getId());
            return response.getIndividual();

        } catch (HttpClientErrorException e) {
            log.error("Individual client error creating individual: status={}", e.getStatusCode(), e);
            throw new ServiceCallException(e.getResponseBodyAsString());
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Individual unknown error creating individual", e);
            throw new CustomException("INDIVIDUAL_ERROR", "Failed to create individual: " + e.getMessage());
        }
    }

    @Override
    public List<Individual> search(String tenantId, String clientId, IndividualSearchCriteria params) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromHttpUrl(appProperties.getIndividualHost())
                .path(appProperties.getIndividualSearchPath());

        if (!CollectionUtils.isEmpty(params.getId()))
            params.getId().forEach(id -> uriBuilder.queryParam("id", id));

        if (!CollectionUtils.isEmpty(params.getIndividualId()))
            params.getIndividualId().forEach(id -> uriBuilder.queryParam("individualId", id));

        if (params.getGivenName() != null)
            uriBuilder.queryParam("givenName", params.getGivenName());

        if (!CollectionUtils.isEmpty(params.getMobileNumber()))
            params.getMobileNumber().forEach(mn -> uriBuilder.queryParam("mobileNumber", mn));

        if (params.getGender() != null)
            uriBuilder.queryParam("gender", params.getGender());

        if (params.getDateOfBirth() != null)
            uriBuilder.queryParam("dateOfBirth", params.getDateOfBirth());

        if (params.getLimit() != null)
            uriBuilder.queryParam("limit", params.getLimit());

        if (params.getOffset() != null)
            uriBuilder.queryParam("offset", params.getOffset());

        if (params.getIncludeDeleted() != null)
            uriBuilder.queryParam("includeDeleted", params.getIncludeDeleted());

        String uri = uriBuilder.build().toUriString();

        try {
            IndividualSearchResponse response = restClient.get()
                    .uri(uri)
                    .header("X-Tenant-ID", tenantId)
                    .header("X-Client-ID", clientId)
                    .retrieve()
                    .body(IndividualSearchResponse.class);

            if (response == null || response.getIndividuals() == null) {
                return Collections.emptyList();
            }

            log.info("Individual search returned {} records", response.getIndividuals().size());
            return response.getIndividuals();

        } catch (HttpClientErrorException e) {
            log.error("Individual client error searching: status={}", e.getStatusCode(), e);
            throw new ServiceCallException(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Individual unknown error searching", e);
            throw new CustomException("INDIVIDUAL_ERROR", "Failed to search individuals: " + e.getMessage());
        }
    }
}
