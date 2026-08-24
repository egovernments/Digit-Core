package org.digit.services.mdms.model;

import org.digit.services.common.model.AuditDetails;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown=true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Mdms {
    @JsonProperty(value="id")
    private String id;
    @JsonProperty(value="tenantId")
    private String tenantId;
    @JsonProperty(value="schemaCode")
    private String schemaCode;
    @JsonProperty(value="uniqueIdentifier")
    private String uniqueIdentifier;
    @JsonProperty(value="data")
    private JsonNode data;
    /**
     * Left unset rather than defaulted to {@code true}.
     *
     * <p>{@code @Builder} ignores field initializers and Jackson binds through the all-args creator,
     * so the old {@code = true} applied to {@code new Mdms()} alone — the builder and every parsed
     * response saw null. A {@code Boolean} omitted under {@code NON_NULL} lets MDMS apply its own
     * default on a write, and keeps a response that omits the key from reading as active.
     */
    @JsonProperty(value="isActive")
    private Boolean isActive;
    @JsonProperty(value="auditDetails")
    private AuditDetails auditDetails;
}