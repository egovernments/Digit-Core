package org.digit.services.registry;

import org.digit.config.ApiProperties;
import org.digit.util.DigitJson;
import org.digit.services.registry.model.RegistryRecord;
import org.digit.services.registry.model.RegistrySchema;
import org.digit.services.registry.model.RegistrySchemaRequest;
import org.digit.services.registry.model.RegistrySearchCriteria;
import org.digit.exception.DigitClientException;
import org.digit.services.registry.model.RegistryCacheEntry;
import org.digit.services.registry.model.RegistryData;
import org.digit.services.registry.model.RegistryDataResponse;
import org.digit.util.DigitContextHolder;
import org.digit.util.DigitRequestContext;
import org.digit.util.HeaderStore;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.client.RestTemplate;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class RegistryClient {
    private final RestTemplate restTemplate;
    private final ApiProperties apiProperties;
    private final RedisTemplate<String, String> registryCacheTemplate;
    private static final ObjectMapper CACHE_MAPPER = DigitJson.shared();

    public RegistryClient(RestTemplate restTemplate, ApiProperties apiProperties) {
        this(restTemplate, apiProperties, null);
    }

    public RegistryClient(RestTemplate restTemplate, ApiProperties apiProperties, RedisTemplate<String, String> registryCacheTemplate) {
        this.restTemplate = restTemplate;
        this.apiProperties = apiProperties;
        this.registryCacheTemplate = registryCacheTemplate;
    }

    public RegistryDataResponse createRegistryData(String schemaCode, RegistryData registryData) {
        if (registryData == null) {
            throw new DigitClientException("Registry data cannot be null");
        }
        if (schemaCode == null || schemaCode.trim().isEmpty()) {
            throw new DigitClientException("Schema code cannot be null or empty");
        }
        if (registryData.getData() == null) {
            throw new DigitClientException("Data cannot be null");
        }
        try {
            log.debug("Creating registry data with schema code: {}", schemaCode);
            String url = this.apiProperties.getRegistryServiceUrl() + "/registry/v3/" + schemaCode + "/data";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            ResponseEntity response = this.restTemplate.postForEntity(url, new HttpEntity(registryData, headers), RegistryDataResponse.class);
            log.debug("Successfully created registry data with schema code: {}", schemaCode);
            RegistryDataResponse result = (RegistryDataResponse) response.getBody();
            cacheAllFields(schemaCode, registryData.getData(), result);
            return result;
        }
        catch (Exception e) {
            throw DigitClientException.wrap("Failed to create registry data", e);
        }
    }

    public RegistryDataResponse searchRegistryData(String schemaCode, String registryId, boolean history) {
        if (schemaCode == null || schemaCode.trim().isEmpty()) {
            throw new DigitClientException("Schema code cannot be null or empty");
        }
        if (registryId == null || registryId.trim().isEmpty()) {
            throw new DigitClientException("Registry ID cannot be null or empty");
        }
        try {
            log.debug("Searching registry data with schema code: {}, registry ID: {}, history: {}", schemaCode, registryId, history);
            String url = this.apiProperties.getRegistryServiceUrl() + "/registry/v3/" + schemaCode + "/data/_registry?registryId=" + registryId + "&history=" + history;
            ResponseEntity response = this.restTemplate.exchange(url, HttpMethod.GET, new HttpEntity(new HttpHeaders()), RegistryDataResponse.class);
            log.debug("Successfully retrieved registry data with schema code: {}, registry ID: {}", schemaCode, registryId);
            return (RegistryDataResponse)response.getBody();
        }
        catch (Exception e) {
            throw DigitClientException.wrap("Failed to retrieve registry data", e);
        }
    }

    public RegistryDataResponse searchRegistryData(String schemaCode, String registryId) {
        return this.searchRegistryData(schemaCode, registryId, false);
    }

    public RegistryDataResponse searchRegistryData(String schemaCode, String key, String value) {
        return this.searchRegistryData(schemaCode, key, value, null, null);
    }

    public RegistryDataResponse searchRegistryData(String schemaCode, String key, String value, Integer limit, Integer offset) {
        if (schemaCode == null || schemaCode.trim().isEmpty()) {
            throw new DigitClientException("Schema code cannot be null or empty");
        }
        if (key == null || key.trim().isEmpty()) {
            throw new DigitClientException("Search key cannot be null or empty");
        }
        if (value == null || value.trim().isEmpty()) {
            throw new DigitClientException("Search value cannot be null or empty");
        }
        try {
            int actualLimit = limit != null ? limit : 5;
            int actualOffset = offset != null ? offset : 0;
            log.debug("Searching registry data with schema code: {}, key: {}, value: {}, limit: {}, offset: {}", schemaCode, key, value, actualLimit, actualOffset);
            String url = this.apiProperties.getRegistryServiceUrl() + "/registry/v3/" + schemaCode + "/data/_search";
            Map<String, Object> searchRequest = new HashMap<>();
            searchRequest.put("contains", Map.of(key, value));
            searchRequest.put("limit", actualLimit);
            searchRequest.put("offset", actualOffset);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            ResponseEntity response = this.restTemplate.exchange(url, HttpMethod.POST, new HttpEntity(searchRequest, headers), RegistryDataResponse.class);
            log.debug("Successfully searched registry data with schema code: {}, key: {}, value: {}", schemaCode, key, value);
            return (RegistryDataResponse)response.getBody();
        }
        catch (Exception e) {
            throw DigitClientException.wrap("Failed to search registry data", e);
        }
    }

    public RegistryDataResponse updateRegistryData(String schemaCode, RegistryData registryData, String key, String value) {
        if (schemaCode == null || schemaCode.trim().isEmpty()) {
            throw new DigitClientException("Schema code cannot be null or empty");
        }
        if (registryData == null) {
            throw new DigitClientException("Registry data cannot be null");
        }
        if (key == null || key.trim().isEmpty()) {
            throw new DigitClientException("Search key cannot be null or empty");
        }
        if (value == null || value.trim().isEmpty()) {
            throw new DigitClientException("Search value cannot be null or empty");
        }
        if (registryData.getData() == null) {
            throw new DigitClientException("Data cannot be null");
        }
        try {
            log.debug("Updating registry data with schema code: {}, key: {}, value: {}", schemaCode, key, value);
            RegistryCacheEntry cached = getCachedEntry(schemaCode, key, value);
            String registryId;
            Integer currentVersion;
            if (cached != null) {
                log.debug("Cache hit for registry update: schemaCode={}, key={}, value={}", schemaCode, key, value);
                registryId = cached.getRegistryId();
                currentVersion = cached.getVersion();
            } else {
                RegistryDataResponse searchResponse = this.searchRegistryData(schemaCode, key, value);
                if (searchResponse == null || searchResponse.getData() == null) {
                    throw new DigitClientException("Registry data not found for key: " + key + " and value: " + value);
                }
                currentVersion = this.extractVersionFromResponse(searchResponse);
                if (currentVersion == null) {
                    throw new DigitClientException("Could not extract version from existing registry data");
                }
                registryId = this.extractRegistryIdFromResponse(searchResponse);
                if (registryId == null || registryId.trim().isEmpty()) {
                    throw new DigitClientException("Could not extract registry ID from existing registry data");
                }
            }
            registryData.setVersion(currentVersion);
            String url = this.apiProperties.getRegistryServiceUrl() + "/registry/v3/" + schemaCode + "/data?id=" + registryId;
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            ResponseEntity response = this.restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity(registryData, headers), RegistryDataResponse.class);
            log.debug("Successfully updated registry data with schema code: {}, key: {}, value: {}", schemaCode, key, value);
            RegistryDataResponse updateResult = (RegistryDataResponse) response.getBody();
            Integer updatedVersion = extractVersionFromResponse(updateResult);
            if (updatedVersion != null) {
                cacheEntry(schemaCode, resolveTenantId(), key, value, registryId, updatedVersion);
            }
            return updateResult;
        }
        catch (Exception e) {
            throw DigitClientException.wrap("Failed to update registry data", e);
        }
    }

    private void cacheAllFields(String schemaCode, JsonNode dataNode, RegistryDataResponse response) {
        if (registryCacheTemplate == null || response == null) return;
        String registryId = extractRegistryIdFromResponse(response);
        Integer version = extractVersionFromResponse(response);
        if (registryId == null || version == null || dataNode == null || !dataNode.isObject()) return;
        String tenantId = resolveTenantId();
        for (Map.Entry<String, JsonNode> field : dataNode.properties()) {
            if (field.getValue().isValueNode()) {
                cacheEntry(schemaCode, tenantId, field.getKey(), field.getValue().asString(),
                        registryId, version);
            }
        }
    }

    private void cacheEntry(String schemaCode, String tenantId, String key, String value, String registryId, Integer version) {
        if (registryCacheTemplate == null) return;
        try {
            String cacheKey = "registry:" + schemaCode + ":" + (tenantId != null ? tenantId : "default") + ":" + key + ":" + value;
            String json = CACHE_MAPPER.writeValueAsString(new RegistryCacheEntry(registryId, version));
            registryCacheTemplate.opsForValue().set(cacheKey, json);
        } catch (Exception e) {
            log.warn("Failed to write registry cache entry: {}",  e.getMessage());
        }
    }

    private RegistryCacheEntry getCachedEntry(String schemaCode, String key, String value) {
        if (registryCacheTemplate == null) return null;
        try {
            String tenantId = resolveTenantId();
            String cacheKey = "registry:" + schemaCode + ":" + (tenantId != null ? tenantId : "default") + ":" + key + ":" + value;
            String json = registryCacheTemplate.opsForValue().get(cacheKey);
            if (json != null) return CACHE_MAPPER.readValue(json, RegistryCacheEntry.class);
        } catch (Exception e) {
            log.warn("Failed to read registry cache entry: {}",  e.getMessage());
        }
        return null;
    }

    /**
     * The field map of the record in a data response, whether the service returned one object or a
     * list of them.
     *
     * <p>Both shapes occur on the same envelope: a write answers with the record, a search answers
     * with a list. Anything else — an absent payload, an empty list — is {@code null}, which both
     * callers treat as "no version/id available" and fail the update on.
     */
    private static Map<?, ?> firstRecord(Object data) {
        if (data instanceof Map<?, ?> record) {
            return record;
        }
        if (data instanceof List<?> records && !records.isEmpty()
                && records.get(0) instanceof Map<?, ?> record) {
            return record;
        }
        return null;
    }

    /**
     * The record's version, or null when the response carries none.
     *
     * <p>Read as a {@code Number} rather than an {@code Integer}: this value is untyped on the way in,
     * so a payload that widened the version to a long must still narrow cleanly instead of silently
     * reading as absent. A non-numeric version stays null — guessing at one would send an
     * {@code If-Match} the service rejects, or worse, one it accepts against the wrong revision.
     */
    private Integer extractVersionFromResponse(RegistryDataResponse response) {
        Map<?, ?> record = firstRecord(response.getData());
        if (record != null && record.get("version") instanceof Number version) {
            return version.intValue();
        }
        return null;
    }

    /**
     * The record's registry id, or null when the response carries none.
     *
     * <p>Feeds both the update path and the Redis cache key, so a wrong value here is a cross-tenant
     * cache collision rather than a visible failure. Non-string ids are stringified rather than
     * dropped, matching what the service accepts back.
     */
    private String extractRegistryIdFromResponse(RegistryDataResponse response) {
        Map<?, ?> record = firstRecord(response.getData());
        if (record == null) {
            return null;
        }
        Object registryId = record.get("registryId");
        return registryId != null ? registryId.toString() : null;
    }

    // ── Data endpoints the client previously lacked ───────────────────────────

    /**
     * Searches records with the service's two filter styles.
     *
     * <p>{@code filters} compares a JSON path to a value as text; {@code contains} is a JSON
     * containment test. They answer different questions, so both are exposed rather than one being
     * chosen on the caller's behalf.
     */
    public RegistryDataResponse searchRegistryData(String schemaCode, RegistrySearchCriteria criteria) {
        requireText(schemaCode, "Schema code cannot be null or empty");
        if (criteria == null
                || ((criteria.getFilters() == null || criteria.getFilters().isEmpty())
                    && (criteria.getContains() == null || criteria.getContains().isEmpty()))) {
            throw new DigitClientException("either filters or contains must be provided");
        }
        String url = dataUrl(schemaCode) + "/_search";
        Map<String, Object> body = new HashMap<>();
        if (criteria.getFilters() != null && !criteria.getFilters().isEmpty()) {
            body.put("filters", criteria.getFilters());
        }
        if (criteria.getContains() != null && !criteria.getContains().isEmpty()) {
            body.put("contains", criteria.getContains());
        }
        body.put("limit", criteria.getLimit() != null ? criteria.getLimit() : 5);
        body.put("offset", criteria.getOffset() != null ? criteria.getOffset() : 0);
        return exchangeJson(url, HttpMethod.POST, body);
    }

    /** One record by its internal id. */
    public RegistryDataResponse getRegistryDataById(String schemaCode, String id) {
        requireText(schemaCode, "Schema code cannot be null or empty");
        requireText(id, "id cannot be null or empty");
        String url = UriComponentsBuilder.fromUriString(dataUrl(schemaCode)).queryParam("id", id).toUriString();
        return exchangeJson(url, HttpMethod.GET, null);
    }

    /** Whether a record with this id exists. */
    public boolean registryDataExists(String schemaCode, String id) {
        requireText(schemaCode, "Schema code cannot be null or empty");
        requireText(id, "id cannot be null or empty");
        String url = UriComponentsBuilder.fromUriString(dataUrl(schemaCode) + "/_exists")
                .queryParam("id", id).toUriString();
        return flagFrom(exchangeJson(url, HttpMethod.GET, null), "exists");
    }

    /** Whether a record is valid against its schema. Supply either id or registryId. */
    public boolean verifyRegistryData(String schemaCode, String id, String registryId) {
        requireText(schemaCode, "Schema code cannot be null or empty");
        if (isBlank(id) && isBlank(registryId)) {
            throw new DigitClientException("either id or registryId is required");
        }
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(dataUrl(schemaCode) + "/_verify");
        if (!isBlank(id)) {
            builder.queryParam("id", id);
        }
        if (!isBlank(registryId)) {
            builder.queryParam("registryId", registryId);
        }
        return flagFrom(exchangeJson(builder.toUriString(), HttpMethod.GET, null), "valid");
    }

    /** Deletes a record. Answers 202 rather than 200 when the service persists asynchronously. */
    public RegistryDataResponse deleteRegistryData(String schemaCode, String id) {
        requireText(schemaCode, "Schema code cannot be null or empty");
        requireText(id, "id cannot be null or empty");
        return exchangeJson(dataUrl(schemaCode) + "/" + id, HttpMethod.DELETE, null);
    }

    // ── Schema reads ─────────────────────────────────────────────────────────

    /** Every schema registered for the tenant. */
    public List<RegistrySchema> listSchemas() {
        RegistryDataResponse response = exchangeJson(schemaUrl(), HttpMethod.GET, null);
        if (response == null || response.getData() == null) {
            return List.of();
        }
        return DigitJson.shared().convertValue(response.getData(),
                DigitJson.shared().getTypeFactory().constructCollectionType(List.class, RegistrySchema.class));
    }

    /** One schema; omit {@code version} for the latest. */
    public RegistrySchema getSchema(String schemaCode, Integer version) {
        requireText(schemaCode, "Schema code cannot be null or empty");
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(schemaUrl() + "/" + schemaCode);
        if (version != null) {
            builder.queryParam("version", version);
        }
        RegistryDataResponse response = exchangeJson(builder.toUriString(), HttpMethod.GET, null);
        return response == null ? null : response.getSchema();
    }

    /**
     * Whether any record of the schema already holds {@code value} in {@code field}. A blank field
     * means {@code registryId}, which is what the service defaults to.
     */
    public boolean schemaFieldExists(String schemaCode, String field, String value) {
        requireText(schemaCode, "Schema code cannot be null or empty");
        requireText(value, "value cannot be null or empty");
        Map<String, Object> body = new HashMap<>();
        body.put("field", isBlank(field) ? "registryId" : field);
        body.put("value", value);
        return flagFrom(exchangeJson(schemaUrl() + "/" + schemaCode + "/_isExist", HttpMethod.POST, body), "exists");
    }

    // ── Internals ────────────────────────────────────────────────────────────

    private String dataUrl(String schemaCode) {
        return this.apiProperties.getRegistryServiceUrl() + "/registry/v3/" + schemaCode + "/data";
    }

    private String schemaUrl() {
        return this.apiProperties.getRegistryServiceUrl() + "/registry/v3/schema";
    }

    /** Issues the call and records the status, so a queued (202) write is recognisable. */
    private RegistryDataResponse exchangeJson(String url, HttpMethod method, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        ResponseEntity<RegistryDataResponse> response = this.restTemplate.exchange(
                url, method, new HttpEntity<>(body, headers), RegistryDataResponse.class);
        RegistryDataResponse payload = response.getBody();
        if (payload != null) {
            payload.setHttpStatus(response.getStatusCode().value());
            if (payload.isQueued()) {
                log.debug("registry queued the write to {}; data will be absent from this response", url);
            }
        }
        return payload;
    }

    private static boolean flagFrom(RegistryDataResponse response, String key) {
        if (response == null || !(response.getData() instanceof Map<?, ?> map)) {
            return false;
        }
        return Boolean.TRUE.equals(map.get(key));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static void requireText(String value, String message) {
        if (isBlank(value)) {
            throw new DigitClientException(message);
        }
    }

    /**
     * Tenant for a cache key. Prefers an explicitly supplied context, because
     * {@code HeaderStore.extractTenantId} both returns null outside a servlet request and throws
     * when a forwarded token carries no realm claim — either of which would put every tenant's
     * entries in the same bucket.
     */
    private static String resolveTenantId() {
        DigitRequestContext context = DigitContextHolder.get();
        if (context != null && context.getTenantId() != null && !context.getTenantId().isBlank()) {
            return context.getTenantId();
        }
        try {
            return HeaderStore.extractTenantId();
        }
        catch (RuntimeException e) {
            return null;
        }
    }

    // ── Schema writes ────────────────────────────────────────────────────────

    /** Registers a schema, against which records of that code are then validated. */
    public RegistrySchema createSchema(RegistrySchemaRequest request) {
        requireSchemaRequest(request);
        RegistryDataResponse response = exchangeJson(schemaUrl(), HttpMethod.POST, request);
        return response == null ? null : response.getSchema();
    }

    /**
     * Replaces a schema, which the service records as a new version rather than editing the existing
     * one — records already stored keep validating against the version they were written under.
     */
    public RegistrySchema updateSchema(String schemaCode, RegistrySchemaRequest request) {
        requireText(schemaCode, "Schema code cannot be null or empty");
        requireSchemaRequest(request);
        RegistryDataResponse response = exchangeJson(schemaUrl() + "/" + schemaCode, HttpMethod.PUT, request);
        return response == null ? null : response.getSchema();
    }

    /** Removes a schema. */
    public boolean deleteSchema(String schemaCode) {
        requireText(schemaCode, "Schema code cannot be null or empty");
        RegistryDataResponse response = exchangeJson(schemaUrl() + "/" + schemaCode, HttpMethod.DELETE, null);
        return response != null && Boolean.TRUE.equals(response.getSuccess());
    }

    private static void requireSchemaRequest(RegistrySchemaRequest request) {
        if (request == null || request.getSchemaCode() == null || request.getSchemaCode().isBlank()) {
            throw new DigitClientException("schemaCode is required");
        }
        if (request.getDefinition() == null) {
            throw new DigitClientException("definition is required");
        }
    }
}
