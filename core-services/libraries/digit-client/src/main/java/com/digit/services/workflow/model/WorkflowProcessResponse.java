package com.digit.services.workflow.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkflowProcessResponse {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("code")
    private String code;

    @JsonProperty("description")
    private String description;

    @JsonProperty("version")
    private String version;

    @JsonProperty("sla")
    private Long sla;

    @JsonProperty("auditDetail")
    private AuditDetail auditDetail;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class AuditDetail {
        @JsonProperty("createdBy")
        private String createdBy;

        @JsonProperty("createdTime")
        private Long createdTime;

        @JsonProperty("modifiedBy")
        private String modifiedBy;

        @JsonProperty("modifiedTime")
        private Long modifiedTime;
    }
}
