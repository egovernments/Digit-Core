package org.digit.services.account.model;

import org.digit.services.common.model.AuditDetails;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** One tenant configuration entry. Mirrors the service's {@code TenantConfigResponse}. */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantConfig {
    @JsonProperty("id")
    private String id;
    @JsonProperty("tenantId")
    private String tenantId;
    @JsonProperty("configKey")
    private String configKey;
    @JsonProperty("configValue")
    private String configValue;
    @JsonProperty("description")
    private String description;
    @JsonProperty("isActive")
    private boolean isActive;
    @JsonProperty("version")
    private int version;
    @JsonProperty("auditDetail")
    private AuditDetails auditDetail;
}
