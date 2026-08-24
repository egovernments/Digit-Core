package org.digit.services.billing.model;

import java.util.UUID;
import org.digit.services.common.model.AuditDetails;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown=true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/*
 * Jackson would otherwise publish each is-prefixed boolean twice: once under the name its
 * @JsonProperty gives it, and again under the name inferred from Lombok's isX() getter — so
 * {"isActive":false,"active":false}. Services that parse strictly reject the second key outright,
 * which made every individual write fail; the rest silently ignored it. Suppressing is-getter
 * detection leaves the annotated fields as the single source of the wire contract.
 */
@JsonAutoDetect(isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class BusinessService {
    @JsonProperty(value="id")
    private UUID id;
    @JsonProperty(value="code")
    private String code;
    @JsonProperty(value="name")
    private String name;
    @JsonProperty(value="collectionMode")
    private CollectionMode collectionMode;
    @JsonProperty(value="allowedPaymentModes")
    private List<PaymentMode> allowedPaymentModes;
    @JsonProperty(value="billExpiryDays")
    private Integer billExpiryDays;
    @JsonProperty(value="currency")
    private String currency;
    @JsonProperty(value="effectiveFrom")
    private long effectiveFrom;
    @JsonProperty(value="effectiveTo")
    private Long effectiveTo;
    @JsonProperty(value="partialPaymentAllowed")
    private boolean partialPaymentAllowed;
    @JsonProperty(value="minPayableAmount")
    private BigDecimal minPayableAmount;
    @JsonProperty(value="roundingRuleCode")
    private String roundingRuleCode;
    @JsonProperty(value="isActive")
    private boolean isActive;
    @JsonProperty(value="version")
    private int version;
    @JsonProperty(value="auditDetail")
    private AuditDetails auditDetail;
}