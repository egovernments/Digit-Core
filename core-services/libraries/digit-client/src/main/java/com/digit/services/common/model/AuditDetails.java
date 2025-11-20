package com.digit.services.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Common audit details model for tracking creation and modification information.
 * This model can be used across all services for consistent audit tracking.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditDetails {

    @JsonProperty("createdBy")
    private String createdBy;

    @JsonProperty("createdTime")
    private Long createdTime;

    @JsonProperty("lastModifiedBy")
    private String lastModifiedBy;

    @JsonProperty("lastModifiedTime")
    private Long lastModifiedTime;

    // Explicit getter methods as fallback for Lombok
    public String getCreatedBy() {
        return createdBy;
    }

    public Long getCreatedTime() {
        return createdTime;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public Long getLastModifiedTime() {
        return lastModifiedTime;
    }
}
