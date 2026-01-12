package org.egov.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessServiceResponse {

    @JsonProperty("id")
    private String id;

    @JsonProperty("code")
    private String code;

    @JsonProperty("name")
    private String name;

    @JsonProperty("collectionMode")
    private String collectionMode;

    @JsonProperty("allowedPaymentModes")
    private List<String> allowedPaymentModes;

    @JsonProperty("billExpiryDays")
    private Integer billExpiryDays;

    @JsonProperty("partialPaymentAllowed")
    private Boolean partialPaymentAllowed;

    @JsonProperty("minPayableAmount")
    private Double minPayableAmount;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("roundingRuleCode")
    private String roundingRuleCode;

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