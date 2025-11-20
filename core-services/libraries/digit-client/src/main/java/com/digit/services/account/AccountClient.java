package com.digit.services.account;

import com.digit.config.ApiProperties;
import com.digit.exception.DigitClientException;
import com.digit.services.account.model.Tenant;
import com.digit.services.account.model.TenantConfig;
import com.digit.services.account.model.TenantRequest;
import com.digit.services.account.model.TenantResponse;
import com.digit.services.account.model.TenantConfigRequest;
import com.digit.services.account.model.TenantConfigResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Service client for Account API operations.
 * Provides methods to interact with the Account service (Tenant management).
 * Based on actual API endpoints from Postman collection.
 */
@Slf4j
@Getter
@Setter
public class AccountClient {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AccountClient.class);

    private final RestTemplate restTemplate;
    private final ApiProperties apiProperties;

    /**
     * Constructor for AccountClient.
     *
     * @param restTemplate the RestTemplate for HTTP operations
     * @param apiProperties the API configuration properties
     */
    public AccountClient(RestTemplate restTemplate, ApiProperties apiProperties) {
        this.restTemplate = restTemplate;
        this.apiProperties = apiProperties;
    }

    /**
     * Creates a new tenant.
     *
     * @param tenant the tenant to create
     * @return the created Tenant object
     * @throws DigitClientException if creation fails
     */
    public Tenant createTenant(Tenant tenant) {
        if (tenant == null) {
            throw new DigitClientException("Tenant cannot be null");
        }

        try {
            log.debug("Creating tenant: {}", tenant.getName());
            String url = apiProperties.getAccountServiceUrl() + "/account/v1";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            
            TenantRequest request = TenantRequest.builder().tenant(tenant).build();
            HttpEntity<TenantRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<TenantRequest> response = restTemplate.postForEntity(url, entity, TenantRequest.class);
            Tenant createdTenant = response.getBody() != null ? response.getBody().getTenant() : null;
            
            log.debug("Successfully created tenant: {}", createdTenant != null ? createdTenant.getId() : "null");
            return createdTenant;
            
        } catch (Exception e) {
            log.error("Failed to create tenant: {}", tenant.getName(), e);
            if (e instanceof DigitClientException) {
                throw e;
            }
            throw new DigitClientException("Failed to create tenant: " + e.getMessage(), e);
        }
    }

    /**
     * Searches for a tenant by code.
     *
     * @param code the tenant code to search for
     * @return the Tenant object if found
     * @throws DigitClientException if the tenant is not found or an error occurs
     */
    public Tenant searchTenantByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new DigitClientException("Tenant code cannot be null or empty");
        }

        try {
            log.debug("Searching tenant with code: {}", code);
            String url = apiProperties.getAccountServiceUrl() + "/account/v1?code=" + code;
            
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<TenantResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, TenantResponse.class);
            Tenant tenant = null;
            if (response.getBody() != null && response.getBody().getTenants() != null && !response.getBody().getTenants().isEmpty()) {
                tenant = response.getBody().getTenants().get(0);
            }
            
            log.debug("Successfully retrieved tenant by code: {}", code);
            return tenant;
            
        } catch (Exception e) {
            log.error("Failed to retrieve tenant with code: {}", code, e);
            if (e instanceof DigitClientException) {
                throw e;
            }
            throw new DigitClientException("Failed to retrieve tenant by code: " + e.getMessage(), e);
        }
    }

    /**
     * Updates an existing tenant.
     *
     * @param tenantId the ID of the tenant to update
     * @param tenant the updated tenant data
     * @return the updated Tenant object
     * @throws DigitClientException if update fails
     */
    public Tenant updateTenant(String tenantId, Tenant tenant) {
        if (tenantId == null || tenantId.trim().isEmpty()) {
            throw new DigitClientException("Tenant ID cannot be null or empty");
        }
        if (tenant == null) {
            throw new DigitClientException("Tenant cannot be null");
        }

        try {
            log.debug("Updating tenant with ID: {}", tenantId);
            String url = apiProperties.getAccountServiceUrl() + "/account/v1/" + tenantId;
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            
            TenantRequest request = TenantRequest.builder().tenant(tenant).build();
            HttpEntity<TenantRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<TenantRequest> response = restTemplate.exchange(url, HttpMethod.PUT, entity, TenantRequest.class);
            Tenant updatedTenant = response.getBody() != null ? response.getBody().getTenant() : null;
            
            log.debug("Successfully updated tenant: {}", tenantId);
            return updatedTenant;
            
        } catch (Exception e) {
            log.error("Failed to update tenant with ID: {}", tenantId, e);
            if (e instanceof DigitClientException) {
                throw e;
            }
            throw new DigitClientException("Failed to update tenant: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a new tenant configuration.
     *
     * @param tenantConfig the tenant configuration to create
     * @return the created TenantConfig object
     * @throws DigitClientException if creation fails
     */
    public TenantConfig createTenantConfig(TenantConfig tenantConfig) {
        if (tenantConfig == null) {
            throw new DigitClientException("TenantConfig cannot be null");
        }

        try {
            log.debug("Creating tenant config: {}", tenantConfig.getName());
            String url = apiProperties.getAccountServiceUrl() + "/account/v1/config";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            
            TenantConfigRequest request = TenantConfigRequest.builder().tenantConfig(tenantConfig).build();
            HttpEntity<TenantConfigRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<TenantConfigRequest> response = restTemplate.postForEntity(url, entity, TenantConfigRequest.class);
            TenantConfig createdConfig = response.getBody() != null ? response.getBody().getTenantConfig() : null;
            
            log.debug("Successfully created tenant config: {}", createdConfig != null ? createdConfig.getId() : "null");
            return createdConfig;
            
        } catch (Exception e) {
            log.error("Failed to create tenant config: {}", tenantConfig.getName(), e);
            if (e instanceof DigitClientException) {
                throw e;
            }
            throw new DigitClientException("Failed to create tenant config: " + e.getMessage(), e);
        }
    }

    /**
     * Searches for tenant configuration by code.
     *
     * @param code the tenant code to search for
     * @return the TenantConfig object if found
     * @throws DigitClientException if the config is not found or an error occurs
     */
    public TenantConfig searchTenantConfigByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new DigitClientException("Tenant code cannot be null or empty");
        }

        try {
            log.debug("Searching tenant config with code: {}", code);
            String url = apiProperties.getAccountServiceUrl() + "/account/v1/config?code=" + code;
            
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<TenantConfigResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, TenantConfigResponse.class);
            TenantConfig tenantConfig = null;
            if (response.getBody() != null && response.getBody().getTenantConfigs() != null && !response.getBody().getTenantConfigs().isEmpty()) {
                tenantConfig = response.getBody().getTenantConfigs().get(0);
            }
            
            log.debug("Successfully retrieved tenant config by code: {}", code);
            return tenantConfig;
            
        } catch (Exception e) {
            log.error("Failed to retrieve tenant config with code: {}", code, e);
            if (e instanceof DigitClientException) {
                throw e;
            }
            throw new DigitClientException("Failed to retrieve tenant config by code: " + e.getMessage(), e);
        }
    }

    /**
     * Updates an existing tenant configuration.
     *
     * @param configId the ID of the config to update
     * @param tenantConfig the updated tenant configuration data
     * @return the updated TenantConfig object
     * @throws DigitClientException if update fails
     */
    public TenantConfig updateTenantConfig(String configId, TenantConfig tenantConfig) {
        if (configId == null || configId.trim().isEmpty()) {
            throw new DigitClientException("Config ID cannot be null or empty");
        }
        if (tenantConfig == null) {
            throw new DigitClientException("TenantConfig cannot be null");
        }

        try {
            log.debug("Updating tenant config with ID: {}", configId);
            String url = apiProperties.getAccountServiceUrl() + "/account/v1/config/" + configId;
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            
            TenantConfigRequest request = TenantConfigRequest.builder().tenantConfig(tenantConfig).build();
            HttpEntity<TenantConfigRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<TenantConfigRequest> response = restTemplate.exchange(url, HttpMethod.PUT, entity, TenantConfigRequest.class);
            TenantConfig updatedConfig = response.getBody() != null ? response.getBody().getTenantConfig() : null;
            
            log.debug("Successfully updated tenant config: {}", configId);
            return updatedConfig;
            
        } catch (Exception e) {
            log.error("Failed to update tenant config with ID: {}", configId, e);
            if (e instanceof DigitClientException) {
                throw e;
            }
            throw new DigitClientException("Failed to update tenant config: " + e.getMessage(), e);
        }
    }
}
