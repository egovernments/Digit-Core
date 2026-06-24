package com.digit.tenant.migration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Event structure for schema creation, mirroring the Go {@code CreateSchemaEvent}.
 * JSON shape: {@code {"tenantId":"..."}}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateSchemaEvent {

    private String tenantId;

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
