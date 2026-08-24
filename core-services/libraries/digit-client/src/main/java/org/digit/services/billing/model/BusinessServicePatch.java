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
 * A partial update of a business service: only the fields set here change.
 *
 * <p>Note {@code partialPaymentAllowed} is absent — the service's patch record does not accept it,
 * so changing that setting needs a full update.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessServicePatch {
    @JsonProperty("name")
    private String name;
    @JsonProperty("collectionMode")
    private CollectionMode collectionMode;
    @JsonProperty("allowedPaymentModes")
    private List<PaymentMode> allowedPaymentModes;
    @JsonProperty("billExpiryDays")
    private Integer billExpiryDays;
    @JsonProperty("minPayableAmount")
    private BigDecimal minPayableAmount;
    @JsonProperty("roundingRuleCode")
    private String roundingRuleCode;
    @JsonProperty("effectiveFrom")
    private Long effectiveFrom;
    @JsonProperty("effectiveTo")
    private Long effectiveTo;
    @JsonProperty("isActive")
    private Boolean isActive;
}
