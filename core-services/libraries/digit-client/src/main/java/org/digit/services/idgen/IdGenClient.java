package org.digit.services.idgen;

import org.digit.config.ApiProperties;
import org.digit.exception.DigitClientException;
import org.digit.services.idgen.model.BulkGenerateRequest;
import org.digit.services.idgen.model.BulkGenerateResponse;
import org.digit.services.idgen.model.GenerateIDResponse;
import org.digit.services.idgen.model.IdGenTemplate;
import org.digit.services.idgen.model.IdGenGenerateRequest;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class IdGenClient {
    /** The service's own ceiling on a single bulk request. */
    private static final int MAX_BULK_COUNT = 1000;
    private static final ParameterizedTypeReference<List<IdGenTemplate>> TEMPLATE_LIST =
            new ParameterizedTypeReference<List<IdGenTemplate>>() {};
    private static final ParameterizedTypeReference<Map<String, Object>> DELETED_RESULT =
            new ParameterizedTypeReference<Map<String, Object>>() {};

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
            String url = this.apiProperties.getIdgenServiceUrl() + "/idgen/v3/generate";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            ResponseEntity<GenerateIDResponse> response = this.restTemplate.exchange(
                    url, HttpMethod.POST, new HttpEntity<>(request, headers), GenerateIDResponse.class);
            GenerateIDResponse idResponse = response.getBody();
            String generatedId = idResponse != null ? idResponse.getId() : null;
            if (generatedId == null || generatedId.trim().isEmpty()) {
                throw new DigitClientException("Generated ID is null or empty");
            }
            return generatedId;
        }
        catch (Exception e) {
            throw DigitClientException.wrap("Failed to generate ID", e);
        }
    }

    public String generateId(String templateCode, Map<String, String> variables) {
        return this.generateId(IdGenGenerateRequest.builder().templateCode(templateCode).variables(variables).build());
    }

    public String generateId(String templateCode) {
        return this.generateId(IdGenGenerateRequest.builder().templateCode(templateCode).build());
    }

    /**
     * Allocates {@code count} ids in one call, which is both faster and safer than looping over
     * {@link #generateId} — the service reserves the whole block against the sequence at once.
     *
     * @param count 1..1000
     */
    public List<String> generateIds(String templateCode, int count, Map<String, String> variables) {
        requireText(templateCode, "Template code cannot be null or empty");
        if (count < 1 || count > MAX_BULK_COUNT) {
            throw new DigitClientException("count must be between 1 and " + MAX_BULK_COUNT);
        }
        BulkGenerateRequest request = BulkGenerateRequest.builder()
                .templateCode(templateCode).count(count).variables(variables).build();
        ResponseEntity<BulkGenerateResponse> response = this.restTemplate.exchange(
                templateUrl("/generate/bulk"), HttpMethod.POST, new HttpEntity<>(request), BulkGenerateResponse.class);
        BulkGenerateResponse body = response.getBody();
        if (body == null || body.getIds() == null || body.getIds().isEmpty()) {
            throw new DigitClientException("idgen returned no ids");
        }
        return body.getIds();
    }

    public List<String> generateIds(String templateCode, int count) {
        return generateIds(templateCode, count, null);
    }

    // ── Templates ────────────────────────────────────────────────────────────

    /** Registers a template as version {@code v1}. */
    public IdGenTemplate createTemplate(IdGenTemplate template) {
        requireTemplate(template);
        ResponseEntity<IdGenTemplate> response = this.restTemplate.postForEntity(
                templateUrl("/template"), template, IdGenTemplate.class);
        return response.getBody();
    }

    /**
     * Publishes a new version of a template.
     *
     * <p>Versions are immutable, so this adds {@code v(n+1)} rather than modifying the existing one,
     * and leaves the sequence counter untouched — ids carry on from where they were.
     */
    public IdGenTemplate updateTemplate(IdGenTemplate template) {
        requireTemplate(template);
        ResponseEntity<IdGenTemplate> response = this.restTemplate.exchange(
                templateUrl("/template"), HttpMethod.PUT, new HttpEntity<>(template), IdGenTemplate.class);
        return response.getBody();
    }

    /** Templates matching the filters; an unmatched search is an empty list, not an error. */
    public List<IdGenTemplate> searchTemplates(String templateCode, String version, List<String> ids,
                                               Integer limit, Integer offset) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(templateUrl("/template"));
        if (templateCode != null && !templateCode.isBlank()) {
            builder.queryParam("templateCode", templateCode);
        }
        if (version != null && !version.isBlank()) {
            // The service rejects a version without a code to scope it to.
            requireText(templateCode, "templateCode is required when version is given");
            builder.queryParam("version", version);
        }
        if (ids != null && !ids.isEmpty()) {
            builder.queryParam("ids", String.join(",", ids));
        }
        if (limit != null && limit > 0) {
            builder.queryParam("limit", limit);
        }
        if (offset != null && offset > 0) {
            builder.queryParam("offset", offset);
        }
        ResponseEntity<List<IdGenTemplate>> response = this.restTemplate.exchange(
                builder.toUriString(), HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), TEMPLATE_LIST);
        List<IdGenTemplate> found = response.getBody();
        return found == null ? List.of() : found;
    }

    /** The latest version of a template, or null when the code is unknown. */
    public IdGenTemplate getTemplate(String templateCode) {
        requireText(templateCode, "Template code cannot be null or empty");
        List<IdGenTemplate> found = searchTemplates(templateCode, null, null, null, null);
        return found.isEmpty() ? null : found.get(found.size() - 1);
    }

    /** Whether a template code is registered. */
    public boolean templateExists(String templateCode) {
        return !searchTemplates(templateCode, null, null, null, null).isEmpty();
    }

    /** Removes exactly one version of a template. Both arguments are required. */
    public boolean deleteTemplate(String templateCode, String version) {
        requireText(templateCode, "Template code cannot be null or empty");
        requireText(version, "version is required — delete removes a single version");
        String url = UriComponentsBuilder.fromUriString(templateUrl("/template"))
                .queryParam("templateCode", templateCode)
                .queryParam("version", version)
                .toUriString();
        ResponseEntity<Map<String, Object>> response = this.restTemplate.exchange(
                url, HttpMethod.DELETE, new HttpEntity<>(new HttpHeaders()), DELETED_RESULT);
        return response.getBody() != null && Boolean.TRUE.equals(response.getBody().get("deleted"));
    }

    // ── Internals ────────────────────────────────────────────────────────────

    private String templateUrl(String path) {
        return this.apiProperties.getIdgenServiceUrl() + "/idgen/v3" + path;
    }

    private static void requireTemplate(IdGenTemplate template) {
        if (template == null || template.getTemplateCode() == null || template.getTemplateCode().isBlank()) {
            throw new DigitClientException("templateCode is required");
        }
        if (template.getConfig() == null || template.getConfig().getTemplate() == null
                || template.getConfig().getTemplate().isBlank()) {
            throw new DigitClientException("config.template is required");
        }
    }

    private static void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new DigitClientException(message);
        }
    }
}