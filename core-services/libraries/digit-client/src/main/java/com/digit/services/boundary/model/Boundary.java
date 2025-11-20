package com.digit.services.boundary.model;

import com.digit.services.common.model.AuditDetails;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * Boundary model representing boundary data from Boundary service.
 * Based on the actual Go service implementation.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Boundary {

    @JsonProperty("id")
    private String id;

    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("code")
    private String code;

    @JsonProperty("geometry")
    private Map<String, Object> geometry;

    @JsonProperty("additionalDetails")
    private Map<String, Object> additionalDetails;

    @JsonProperty("auditDetails")
    private AuditDetails auditDetails;
}
