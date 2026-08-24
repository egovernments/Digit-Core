package org.digit.services.billing.model;

import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown=true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillAccountDetail {
    @JsonProperty(value="id")
    private UUID id;
    @JsonProperty(value="billDetailId")
    private UUID billDetailId;
    @JsonProperty(value="lineItemId")
    private UUID lineItemId;
    @JsonProperty(value="taxHeadCode")
    private String taxHeadCode;
    @JsonProperty(value="order")
    private int order;
    @JsonProperty(value="amount")
    private BigDecimal amount;
    @JsonProperty(value="adjustedAmount")
    private BigDecimal adjustedAmount;
    @JsonProperty(value="metadata")
    private Map<String, Object> metadata;
}