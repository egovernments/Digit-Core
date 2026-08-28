package org.digit.services.billing.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessService {
    @JsonProperty(value="id")
    private String id;
    @JsonProperty(value="code")
    private String code;
    @JsonProperty(value="name")
    private String name;
    @JsonProperty(value="description")
    private String description;
    @JsonProperty(value="collectionMode")
    private String collectionMode;
    @JsonProperty(value="allowedPaymentModes")
    private List<String> allowedPaymentModes;
    @JsonProperty(value="billExpiryDays")
    private Integer billExpiryDays;
    @JsonProperty(value="currency")
    private String currency;
    @JsonProperty(value="effectiveFrom")
    private Long effectiveFrom;
    @JsonProperty(value="effectiveTo")
    private Long effectiveTo;
    @JsonProperty(value="partialPaymentAllowed")
    private Boolean partialPaymentAllowed;
    @JsonProperty(value="minPayableAmount")
    private Double minPayableAmount;
    @JsonProperty(value="roundingRuleCode")
    private String roundingRuleCode;
    @JsonProperty(value="isActive")
    private Boolean isActive;
    @JsonProperty(value="version")
    private Integer version;
    @JsonProperty(value="metadata")
    private Map<String, Object> metadata;
}