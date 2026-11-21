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
public class LineItem {
    @JsonProperty(value="id")
    private String id;
    @JsonProperty(value="demandId")
    private String demandId;
    @JsonProperty(value="taxHeadCode")
    private String taxHeadCode;
    @JsonProperty(value="amount")
    private Double amount;
    @JsonProperty(value="collectedAmount")
    private Double collectedAmount;
    @JsonProperty(value="metadata")
    private Map<String, Object> metadata;
}