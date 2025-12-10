package org.digit.services.registry.model;

import org.digit.services.common.model.AuditDetails;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Registry data model representing registry data from Registry service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegistryData {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("registryId")
    private String registryId;

    @JsonIgnore
    private String tenantId;

    @JsonProperty("schemaCode")
    private String schemaCode;

    @JsonProperty("schemaVersion")
    private Integer schemaVersion;

    @JsonProperty("version")
    private Integer version;

    @JsonProperty("data")
    private JsonNode data;

    @JsonProperty("isActive")
    private Boolean isActive;

    @JsonProperty("effectiveFrom")
    private LocalDateTime effectiveFrom;

    @JsonProperty("effectiveTo")
    private LocalDateTime effectiveTo;

    @JsonProperty("auditDetails")
    private AuditDetails auditDetails;

    @JsonIgnore
    private LocalDateTime createdAt;

    @JsonIgnore
    private LocalDateTime updatedAt;

    @JsonIgnore
    private String createdBy;

    @JsonIgnore
    private String updatedBy;
}