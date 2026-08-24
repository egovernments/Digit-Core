package org.digit.services.billing.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A business service to register — the catalogue entry every demand and bill is filed under.
 *
 * <p>Required by the service: {@code code}, {@code name}, a non-empty {@code allowedPaymentModes},
 * {@code billExpiryDays}, {@code currency}, {@code effectiveFrom} and {@code isActive}.
 * {@code collectionMode} defaults to {@code BOTH}. {@code effectiveTo}, when given, must be strictly
 * later than {@code effectiveFrom}.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessServiceCreate {
    /** Upper-case, 2..32 characters, matching {@code ^[A-Z][A-Z0-9_]{1,31}$}. */
    @JsonProperty("code")
    private String code;
    @JsonProperty("name")
    private String name;
    @JsonProperty("collectionMode")
    private CollectionMode collectionMode;
    @JsonProperty("allowedPaymentModes")
    private List<PaymentMode> allowedPaymentModes;
    /** 0..1095 days. */
    @JsonProperty("billExpiryDays")
    private Integer billExpiryDays;
    @JsonProperty("partialPaymentAllowed")
    private Boolean partialPaymentAllowed;
    /** 0..9999999.99, at most two decimal places. */
    @JsonProperty("minPayableAmount")
    private BigDecimal minPayableAmount;
    /** A three-letter code, e.g. {@code INR}. */
    @JsonProperty("currency")
    private String currency;
    @JsonProperty("roundingRuleCode")
    private String roundingRuleCode;
    /** Epoch millis. */
    @JsonProperty("effectiveFrom")
    private Long effectiveFrom;
    @JsonProperty("effectiveTo")
    private Long effectiveTo;
    @JsonProperty("isActive")
    private Boolean isActive;
}
