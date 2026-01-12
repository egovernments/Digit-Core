package org.egov.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxHeadResponse {

    @JsonProperty("id")
    private String id;

    @JsonProperty("code")
    private String code;

    @JsonProperty("name")
    private String name;

    @JsonProperty("businessServiceCode")
    private String businessServiceCode;

    @JsonProperty("category")
    private String category;

    @JsonProperty("order")
    private Integer order;

    @JsonProperty("effectiveFrom")
    private Long effectiveFrom;

    @JsonProperty("effectiveTo")
    private Long effectiveTo;

    @JsonProperty("isActive")
    private Boolean isActive;

    @JsonProperty("version")
    private Integer version;

    @JsonProperty("auditDetail")
    private AuditDetails auditDetail;
}