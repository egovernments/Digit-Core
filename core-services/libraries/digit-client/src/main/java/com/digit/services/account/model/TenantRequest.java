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
 * Request wrapper for Tenant operations.
 * Based on the actual API structure from Postman collection.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TenantRequest {

    @JsonProperty("tenant")
    private Tenant tenant;

    // Explicit builder methods as fallback for Lombok
    public static TenantRequest builder() {
        return new TenantRequest();
    }

    public TenantRequest tenant(Tenant tenant) {
        this.tenant = tenant;
        return this;
    }

    public TenantRequest build() {
        return this;
    }

    // Explicit getter/setter methods
    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }
}
