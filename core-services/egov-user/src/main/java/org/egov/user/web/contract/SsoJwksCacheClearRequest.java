package org.egov.user.web.contract;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.egov.common.contract.request.RequestInfo;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Request object for clearing SSO JWKS cache.
 */
@Getter
@Setter
public class SsoJwksCacheClearRequest {

    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;

    @NotNull(message = "Tenant ID is required")
    @Size(max = 256, message = "Tenant ID must not exceed 256 characters")
    @JsonProperty("tenantId")
    private String tenantId;

    @NotNull(message = "JWKS URI is required")
    @Size(max = 2048, message = "JWKS URI must not exceed 2048 characters")
    @JsonProperty("jwksUri")
    private String jwksUri;
}
