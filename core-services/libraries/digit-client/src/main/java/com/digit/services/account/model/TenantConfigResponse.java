package com.digit.services.account.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Response wrapper for TenantConfig operations.
 * Based on the actual API structure from Go service.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TenantConfigResponse {

    @JsonProperty("ResponseInfo")
    private ResponseInfo responseInfo;

    @JsonProperty("tenantConfigs")
    private List<TenantConfig> tenantConfigs;

    // Explicit getter/setter methods as fallback for Lombok
    public ResponseInfo getResponseInfo() {
        return responseInfo;
    }

    public void setResponseInfo(ResponseInfo responseInfo) {
        this.responseInfo = responseInfo;
    }

    public List<TenantConfig> getTenantConfigs() {
        return tenantConfigs;
    }

    public void setTenantConfigs(List<TenantConfig> tenantConfigs) {
        this.tenantConfigs = tenantConfigs;
    }
}
