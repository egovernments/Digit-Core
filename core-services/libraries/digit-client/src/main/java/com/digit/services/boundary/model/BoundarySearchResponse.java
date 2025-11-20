package com.digit.services.boundary.model;

import com.digit.services.account.model.ResponseInfo;
import com.digit.services.common.model.AuditDetails;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response wrapper for Boundary search operations.
 * Based on the actual API structure from Go service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BoundarySearchResponse {

    @JsonProperty("responseInfo")
    private ResponseInfo responseInfo;

    @JsonProperty("tenantBoundary")
    private List<HierarchyRelation> tenantBoundary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HierarchyRelation {

        @JsonProperty("tenantId")
        private String tenantId;

        @JsonProperty("boundary")
        private List<EnrichedBoundary> boundary;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EnrichedBoundary {

        @JsonProperty("id")
        private String id;

        @JsonProperty("tenantId")
        private String tenantId;

        @JsonProperty("code")
        private String code;

        @JsonProperty("name")
        private String name;

        @JsonProperty("type")
        private String type;

        @JsonProperty("parent")
        private String parent;

        @JsonProperty("children")
        private List<String> children;

        @JsonProperty("materializedPath")
        private String materializedPath;

        @JsonProperty("auditDetails")
        private AuditDetails auditDetails;
    }
}
