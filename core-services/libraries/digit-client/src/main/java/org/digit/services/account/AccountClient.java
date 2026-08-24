package org.digit.services.account;

import org.digit.config.ApiProperties;
import org.digit.exception.DigitClientException;
import org.digit.services.account.model.Tenant;
import org.digit.services.account.model.TenantConfig;
import org.digit.services.account.model.TenantConfigCreateRequest;
import org.digit.services.account.model.TenantConfigListResponse;
import org.digit.services.account.model.TenantConfigUpdateRequest;
import org.digit.services.account.model.TenantListResponse;
import java.util.List;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Client for the account service: tenant lookup and per-tenant configuration.
 *
 * <p>Tenant configuration has no delete endpoint — the service exposes only create, list and update,
 * on both its header-based and canonical routes — so entries are retired by updating them rather than
 * removed. Tenant creation, update and deletion are likewise not exposed here; tenants are created
 * through the service's own provisioning flow.
 */
@Slf4j
@Getter
public class AccountClient {
    private final RestTemplate restTemplate;
    private final ApiProperties apiProperties;

    public AccountClient(RestTemplate restTemplate, ApiProperties apiProperties) {
        this.restTemplate = restTemplate;
        this.apiProperties = apiProperties;
    }

    // ── Tenants ──────────────────────────────────────────────────────────────

    /** All tenants, first page. */
    public TenantListResponse searchTenants() {
        return searchTenants(null, null, null, null);
    }

    /**
     * Searches tenants by name and/or email.
     *
     * <p>Both filters are optional; omit them to list. {@code page} is one-based, and both paging
     * parameters fall back to the service's own defaults when null.
     */
    public TenantListResponse searchTenants(String name, String email, Integer page, Integer size) {
        return searchTenants(null, name, email, page, size);
    }

    /**
     * Searches tenants by code, name and/or email.
     *
     * <p>{@code code} is the tenant's identifier — {@code TEST3} and the like — so it is the precise
     * filter of the three, where name and email are descriptive. All are optional; omit them to list.
     */
    public TenantListResponse searchTenants(String code, String name, String email,
                                            Integer page, Integer size) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(tenantsUrl());
        addIfText(builder, "code", code);
        addIfText(builder, "name", name);
        addIfText(builder, "email", email);
        addIfPositive(builder, "page", page);
        addIfPositive(builder, "size", size);
        ResponseEntity<TenantListResponse> response = this.restTemplate.exchange(
                builder.toUriString(), HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), TenantListResponse.class);
        return response.getBody();
    }

    /**
     * The tenant with this exact code, or null when there is none.
     *
     * <p>Preferred over the name and email lookups: a code identifies a tenant, so this is the one of
     * the three that cannot match the wrong row. Matched case-sensitively, as tenant codes are.
     */
    public Tenant getTenantByCode(String code) {
        requireText(code, "code is required");
        TenantListResponse response = searchTenants(code, null, null, null, null);
        return firstMatch(response == null ? null : response.getTenants(),
                tenant -> code.equals(tenant.getCode()));
    }

    /** The tenant with this exact name, or null when there is none. */
    public Tenant getTenantByName(String name) {
        requireText(name, "name is required");
        TenantListResponse response = searchTenants(name, null, null, null);
        return firstMatch(response == null ? null : response.getTenants(),
                tenant -> name.equals(tenant.getName()));
    }

    /** The tenant registered with this email, or null when there is none. */
    public Tenant getTenantByEmail(String email) {
        requireText(email, "email is required");
        TenantListResponse response = searchTenants(null, email, null, null);
        return firstMatch(response == null ? null : response.getTenants(),
                tenant -> email.equalsIgnoreCase(tenant.getEmail()));
    }

    // ── Tenant configuration ─────────────────────────────────────────────────

    /**
     * Creates a configuration entry for the tenant in context.
     *
     * <p>The tenant is taken from the request context, not the payload, so a caller outside a servlet
     * request must supply it via {@link org.digit.util.DigitContextHolder}.
     */
    public TenantConfig createTenantConfig(TenantConfigCreateRequest request) {
        if (request == null || isBlank(request.getConfigKey())) {
            throw new DigitClientException("configKey is required to create a tenant config");
        }
        ResponseEntity<TenantConfig> response = this.restTemplate.postForEntity(
                configUrl(), request, TenantConfig.class);
        return response.getBody();
    }

    /** Every configuration entry for the tenant in context, first page. */
    public TenantConfigListResponse searchTenantConfigs() {
        return searchTenantConfigs(null, null, null);
    }

    /** Configuration entries, optionally narrowed to one key. */
    public TenantConfigListResponse searchTenantConfigs(String configKey, Integer page, Integer size) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(configUrl());
        addIfText(builder, "configKey", configKey);
        addIfPositive(builder, "page", page);
        addIfPositive(builder, "size", size);
        ResponseEntity<TenantConfigListResponse> response = this.restTemplate.exchange(
                builder.toUriString(), HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), TenantConfigListResponse.class);
        return response.getBody();
    }

    /**
     * One configuration entry by key, or null when it is not set.
     *
     * <p>Built on the list endpoint because the service has no read-by-id or read-by-key route.
     */
    public TenantConfig getTenantConfig(String configKey) {
        requireText(configKey, "configKey is required");
        TenantConfigListResponse response = searchTenantConfigs(configKey, null, null);
        return firstMatch(response == null ? null : response.getConfigs(),
                config -> configKey.equals(config.getConfigKey()));
    }

    /** The value of one configuration entry, or null when it is not set. */
    public String getTenantConfigValue(String configKey) {
        TenantConfig config = getTenantConfig(configKey);
        return config == null ? null : config.getConfigValue();
    }

    /** Updates a configuration entry by its id. */
    public TenantConfig updateTenantConfig(String id, TenantConfigUpdateRequest request) {
        requireText(id, "config id is required");
        if (request == null) {
            throw new DigitClientException("update payload is required");
        }
        ResponseEntity<TenantConfig> response = this.restTemplate.exchange(
                configUrl() + "/" + id, HttpMethod.PUT, new HttpEntity<>(request), TenantConfig.class);
        return response.getBody();
    }

    /**
     * Sets a configuration key, creating it when absent and updating it otherwise.
     *
     * <p>The service has no upsert route, so this reads first. That makes it a two-call sequence and
     * therefore not atomic — two callers setting the same key at once can have one overwrite the other.
     */
    public TenantConfig setTenantConfig(String configKey, String configValue, String description) {
        requireText(configKey, "configKey is required");
        TenantConfig existing = getTenantConfig(configKey);
        if (existing == null) {
            return createTenantConfig(TenantConfigCreateRequest.builder()
                    .configKey(configKey).configValue(configValue).description(description).build());
        }
        return updateTenantConfig(existing.getId(), TenantConfigUpdateRequest.builder()
                .configKey(configKey).configValue(configValue).description(description).build());
    }

    // ── Internals ────────────────────────────────────────────────────────────

    private String tenantsUrl() {
        return this.apiProperties.getAccountServiceUrl() + "/accounts/v3/tenants";
    }

    private String configUrl() {
        return this.apiProperties.getAccountServiceUrl() + "/accounts/v3/config";
    }

    private static <T> T firstMatch(List<T> items, java.util.function.Predicate<T> predicate) {
        if (items == null) {
            return null;
        }
        return items.stream().filter(predicate).findFirst().orElse(null);
    }

    private static void addIfText(UriComponentsBuilder builder, String name, String value) {
        if (!isBlank(value)) {
            builder.queryParam(name, value);
        }
    }

    private static void addIfPositive(UriComponentsBuilder builder, String name, Integer value) {
        if (value != null && value > 0) {
            builder.queryParam(name, value);
        }
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
