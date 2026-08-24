package org.digit.services.registry.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A stored registry record. Mirrors the registry service's {@code RegistryData}.
 *
 * <p>The timestamps are {@link Instant}, not epoch millis: registry is the one DIGIT service that
 * serializes them as instants rather than numbers.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistryRecord {
    @JsonProperty("id")
    private UUID id;
    @JsonProperty("registryId")
    private String registryId;
    @JsonProperty("tenantId")
    private String tenantId;
    @JsonProperty("schemaCode")
    private String schemaCode;
    @JsonProperty("schemaVersion")
    private int schemaVersion;
    @JsonProperty("version")
    private int version;
    @JsonProperty("data")
    private JsonNode data;
    @JsonProperty("isActive")
    private boolean isActive;
    @JsonProperty("effectiveFrom")
    private Instant effectiveFrom;
    @JsonProperty("effectiveTo")
    private Instant effectiveTo;
    @JsonProperty("createdTime")
    private Instant createdTime;
    @JsonProperty("modifiedTime")
    private Instant modifiedTime;
    @JsonProperty("createdBy")
    private String createdBy;
    @JsonProperty("modifiedBy")
    private String modifiedBy;
    @JsonProperty("requestId")
    private String requestId;
}
