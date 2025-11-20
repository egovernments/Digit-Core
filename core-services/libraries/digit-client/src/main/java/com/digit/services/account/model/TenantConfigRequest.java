package com.digit.services.account.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request wrapper for TenantConfig operations.
 * Based on the actual API structure from Postman collection.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TenantConfigRequest {

    @JsonProperty("tenantConfig")
    private TenantConfig tenantConfig;

    // Explicit builder methods as fallback for Lombok
    public static TenantConfigRequest builder() {
        return new TenantConfigRequest();
    }

    public TenantConfigRequest tenantConfig(TenantConfig tenantConfig) {
        this.tenantConfig = tenantConfig;
        return this;
    }

    public TenantConfigRequest build() {
        return this;
    }

    // Explicit getter/setter methods
    public TenantConfig getTenantConfig() {
        return tenantConfig;
    }

    public void setTenantConfig(TenantConfig tenantConfig) {
        this.tenantConfig = tenantConfig;
    }
}
