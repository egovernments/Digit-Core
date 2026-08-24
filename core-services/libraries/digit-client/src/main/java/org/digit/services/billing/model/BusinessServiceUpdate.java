package org.digit.services.billing.model;

import java.math.BigDecimal;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A full replacement of a business service. Same required fields as a create except the code,
 * which is taken from the path and cannot be changed.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessServiceUpdate {
    @JsonProperty("name")
    private String name;
    @JsonProperty("collectionMode")
    private CollectionMode collectionMode;
    @JsonProperty("allowedPaymentModes")
    private List<PaymentMode> allowedPaymentModes;
    @JsonProperty("billExpiryDays")
    private Integer billExpiryDays;
    @JsonProperty("partialPaymentAllowed")
    private Boolean partialPaymentAllowed;
    @JsonProperty("minPayableAmount")
    private BigDecimal minPayableAmount;
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
}
