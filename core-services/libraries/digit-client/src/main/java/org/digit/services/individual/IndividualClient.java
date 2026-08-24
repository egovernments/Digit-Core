package org.digit.services.individual;

import org.digit.config.ApiProperties;
import org.digit.exception.DigitClientException;
import org.digit.services.individual.model.Individual;
import org.digit.services.individual.model.IndividualConfig;
import org.digit.services.individual.model.IndividualSearchCriteria;
import org.digit.services.individual.model.IndividualSearchResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.List;
import java.util.Map;
import lombok.Getter;

@Getter
public class IndividualClient {
    private final RestTemplate restTemplate;
    private final ApiProperties apiProperties;
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 20;

    public IndividualClient(RestTemplate restTemplate, ApiProperties apiProperties) {
        this.restTemplate = restTemplate;
        this.apiProperties = apiProperties;
    }

    // ── Individuals ──────────────────────────────────────────────────────────

    public Individual createIndividual(Individual individual) {
        ResponseEntity<Individual> response = this.restTemplate.postForEntity(individualsUrl(), individual, Individual.class);
        return response.getBody();
    }

    public Individual updateIndividual(String id, Individual individual) {
        requireText(id, "individual id is required");
        String url = individualsUrl() + "/" + id;
        ResponseEntity<Individual> response = this.restTemplate.exchange(
                url, HttpMethod.PUT, new HttpEntity<>(individual), Individual.class);
        return response.getBody();
    }

    /** Soft-deletes an individual. The service answers 204 with no body. */
    public void deleteIndividual(String id) {
        requireText(id, "individual id is required");
        this.restTemplate.exchange(individualsUrl() + "/" + id, HttpMethod.DELETE,
                new HttpEntity<>(new HttpHeaders()), Void.class);
    }

    public Individual getIndividualById(String individualId) {
        String url = individualsUrl() + "/" + individualId;
        ResponseEntity<Individual> response = this.restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), Individual.class);
        return response.getBody();
    }

    /** Searches on any combination of the service's filters. */
    public IndividualSearchResponse searchIndividuals(IndividualSearchCriteria criteria) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(individualsUrl());
        if (criteria != null) {
            addEach(builder, "id", criteria.getIds());
            addEach(builder, "individualId", criteria.getIndividualIds());
            addEach(builder, "userId", criteria.getUserIds());
            addIfText(builder, "givenName", criteria.getGivenName());
            addIfText(builder, "mobileNumber", criteria.getMobileNumber());
            addIfText(builder, "gender", criteria.getGender());
            addIfText(builder, "dateOfBirth", criteria.getDateOfBirth());
            if (criteria.getIncludeDeleted() != null) {
                builder.queryParam("includeDeleted", criteria.getIncludeDeleted());
            }
        }
        int page = criteria != null && criteria.getPage() != null && criteria.getPage() > 0 ? criteria.getPage() : DEFAULT_PAGE;
        int size = criteria != null && criteria.getSize() != null && criteria.getSize() > 0 ? criteria.getSize() : DEFAULT_SIZE;
        builder.queryParam("page", page).queryParam("size", size);

        ResponseEntity<IndividualSearchResponse> response = this.restTemplate.exchange(
                builder.toUriString(), HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), IndividualSearchResponse.class);
        return response.getBody();
    }

    public IndividualSearchResponse searchIndividualsByUserIds(List<String> userIds, Integer page, Integer size) {
        return searchIndividuals(IndividualSearchCriteria.builder().userIds(userIds).page(page).size(size).build());
    }

    public IndividualSearchResponse searchIndividualsByMobileNumber(String mobileNumber, Integer page, Integer size) {
        return searchIndividuals(IndividualSearchCriteria.builder().mobileNumber(mobileNumber).page(page).size(size).build());
    }

    public IndividualSearchResponse searchIndividualsByIds(List<String> ids, Integer page, Integer size) {
        return searchIndividuals(IndividualSearchCriteria.builder().ids(ids).page(page).size(size).build());
    }

    public IndividualSearchResponse searchIndividualsByName(String individualName) {
        return this.searchIndividualsByName(individualName, DEFAULT_PAGE, DEFAULT_SIZE);
    }

    public IndividualSearchResponse searchIndividualsByName(String individualName, Integer page, Integer size) {
        return searchIndividuals(IndividualSearchCriteria.builder()
                .givenName(individualName).page(page).size(size).build());
    }

    public IndividualSearchResponse searchAllIndividuals() {
        return this.searchIndividuals(null);
    }

    public IndividualSearchResponse searchAllIndividuals(Integer page, Integer size) {
        return searchIndividuals(IndividualSearchCriteria.builder().page(page).size(size).build());
    }

    // ── Existence ────────────────────────────────────────────────────────────

    /**
     * Whether any individual matches. The service requires at least one filter and takes each of
     * them single-valued on this endpoint, unlike search.
     */
    public boolean individualExists(IndividualSearchCriteria criteria) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(individualsUrl() + "/exists");
        boolean any = false;
        any |= addFirst(builder, "id", criteria == null ? null : criteria.getIds());
        any |= addFirst(builder, "individualId", criteria == null ? null : criteria.getIndividualIds());
        any |= addFirst(builder, "userId", criteria == null ? null : criteria.getUserIds());
        any |= addIfText(builder, "givenName", criteria == null ? null : criteria.getGivenName());
        any |= addIfText(builder, "mobileNumber", criteria == null ? null : criteria.getMobileNumber());
        any |= addIfText(builder, "gender", criteria == null ? null : criteria.getGender());
        any |= addIfText(builder, "dateOfBirth", criteria == null ? null : criteria.getDateOfBirth());
        if (!any) {
            throw new DigitClientException("at least one filter is required to check existence");
        }
        try {
            ResponseEntity<Map<String, Boolean>> response = this.restTemplate.exchange(
                    builder.toUriString(), HttpMethod.GET, new HttpEntity<>(new HttpHeaders()),
                    new ParameterizedTypeReference<Map<String, Boolean>>(){});
            Map<String, Boolean> body = response.getBody();
            return body != null && Boolean.TRUE.equals(body.get("exists"));
        }
        catch (HttpClientErrorException.NotFound e) {
            return false;
        }
    }

    public boolean existsById(String id) {
        return individualExists(IndividualSearchCriteria.builder().ids(List.of(id)).build());
    }

    public boolean existsByUserId(String userId) {
        return individualExists(IndividualSearchCriteria.builder().userIds(List.of(userId)).build());
    }

    public boolean existsByIndividualId(String individualId) {
        return individualExists(IndividualSearchCriteria.builder().individualIds(List.of(individualId)).build());
    }

    public boolean existsByMobileNumber(String mobileNumber) {
        return individualExists(IndividualSearchCriteria.builder().mobileNumber(mobileNumber).build());
    }

    /**
     * Whether an individual with this {@code individualId} exists.
     *
     * <p>Searches by {@code individualId} — the IND-prefixed business key — and not by {@code id},
     * the UUID primary key, despite having previously delegated to {@link #existsById}. The service
     * validates {@code id} as a UUID, so passing the very value this parameter is named after failed
     * with {@code field 'id' failed 'uuid' validation}. Use {@link #existsById} for the UUID.
     */
    public boolean isIndividualExist(String individualId) {
        return existsByIndividualId(individualId);
    }

    // ── Tenant config ────────────────────────────────────────────────────────

    /** Creates or replaces the tenant's config; the service answers 201 on create and 200 on update. */
    public IndividualConfig upsertIndividualConfig(IndividualConfig config) {
        if (config == null
                || (isBlank(config.getMobileRegex()) && isBlank(config.getNameRegex())
                    && (config.getUniquenessCriteria() == null || config.getUniquenessCriteria().isEmpty()))) {
            throw new DigitClientException(
                    "at least one of mobileRegex, nameRegex or uniquenessCriteria must be set");
        }
        String url = this.apiProperties.getIndividualServiceUrl() + "/individuals/v3/configs";
        ResponseEntity<IndividualConfig> response = this.restTemplate.exchange(
                url, HttpMethod.POST, new HttpEntity<>(config), IndividualConfig.class);
        return response.getBody();
    }

    /** The tenant's config, or null when none has been set. */
    public IndividualConfig getIndividualConfig() {
        String url = this.apiProperties.getIndividualServiceUrl() + "/individuals/v3/configs";
        try {
            ResponseEntity<IndividualConfig> response = this.restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), IndividualConfig.class);
            return response.getBody();
        }
        catch (HttpClientErrorException.NotFound e) {
            return null;
        }
        catch (DigitClientException e) {
            // The library's error handler maps 404 before Spring's own exception surfaces.
            if (e.getHttpStatus() != null && e.getHttpStatus().value() == HttpStatus.NOT_FOUND.value()) {
                return null;
            }
            throw e;
        }
    }

    // ── Internals ────────────────────────────────────────────────────────────

    private String individualsUrl() {
        return this.apiProperties.getIndividualServiceUrl() + "/individuals/v3/individuals";
    }

    private static void addEach(UriComponentsBuilder builder, String name, List<String> values) {
        if (values == null) {
            return;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                builder.queryParam(name, value);
            }
        }
    }

    private static boolean addFirst(UriComponentsBuilder builder, String name, List<String> values) {
        if (values == null || values.isEmpty()) {
            return false;
        }
        return addIfText(builder, name, values.get(0));
    }

    private static boolean addIfText(UriComponentsBuilder builder, String name, String value) {
        if (isBlank(value)) {
            return false;
        }
        builder.queryParam(name, value);
        return true;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static void requireText(String value, String message) {
        if (isBlank(value)) {
            throw new DigitClientException(message);
        }
    }
}
