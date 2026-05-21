package org.digit.services.billing.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillAccountDetail {
    @JsonProperty(value="id")
    private String id;
    @JsonProperty(value="billDetailId")
    private String billDetailId;
    @JsonProperty(value="lineItemId")
    private String lineItemId;
    @JsonProperty(value="taxHeadCode")
    private String taxHeadCode;
    @JsonProperty(value="order")
    private Integer order;
    @JsonProperty(value="amount")
    private Double amount;
    @JsonProperty(value="adjustedAmount")
    private Double adjustedAmount;
    @JsonProperty(value="metadata")
    private Map<String, Object> metadata;
}