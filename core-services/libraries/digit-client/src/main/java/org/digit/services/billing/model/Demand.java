package org.digit.services.billing.model;

import org.digit.services.common.model.AuditDetails;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A demand as returned by the billing service. Mirrors its {@code Demand} record.
 *
 * <p>Monetary fields are {@link BigDecimal} because billing serializes them as quoted decimal
 * strings; reading them into a {@code double} loses exactness on values it can't represent.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Demand {
    @JsonProperty(value="id")
    private UUID id;
    @JsonProperty(value="businessServiceCode")
    private String businessServiceCode;
    @JsonProperty(value="periodFrom")
    private long periodFrom;
    @JsonProperty(value="periodTo")
    private long periodTo;
    @JsonProperty(value="consumerCode")
    private String consumerCode;
    @JsonProperty(value="billExpiryDays")
    private Integer billExpiryDays;
    @JsonProperty(value="payer")
    private List<String> payer;
    @JsonProperty(value="arrearDemandIds")
    private List<String> arrearDemandIds;
    @JsonProperty(value="lineItems")
    private List<LineItem> lineItems;
    @JsonProperty(value="status")
    private DemandStatus status;
    @JsonProperty(value="totalAmount")
    private BigDecimal totalAmount;
    @JsonProperty(value="totalCollectedAmount")
    private BigDecimal totalCollectedAmount;
    // Explicitly named: Lombok's generated accessor for a boolean called isDemandPaid would
    // otherwise make Jackson infer the property name "demandPaid".
    @JsonProperty(value="isDemandPaid")
    private boolean isDemandPaid;
    @JsonProperty(value="metadata")
    private Map<String, Object> metadata;
    @JsonProperty(value="version")
    private int version;
    @JsonProperty(value="auditDetail")
    private AuditDetails auditDetail;
}
