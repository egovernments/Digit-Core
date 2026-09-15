package org.egov.user.web.contract;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.egov.common.contract.request.RequestInfo;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Request object for clearing SSO decoder cache.
 */
@Getter
@Setter
public class SsoCacheClearRequest {

    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;

    @NotNull(message = "Tenant ID is required")
    @Size(max = 256, message = "Tenant ID must not exceed 256 characters")
    @JsonProperty("tenantId")
    private String tenantId;

    @Size(max = 100, message = "Provider ID must not exceed 100 characters")
    @JsonProperty("providerId")
    private String providerId;
}
