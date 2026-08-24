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
public class LineItem {
    @JsonProperty(value="id")
    private UUID id;
    @JsonProperty(value="demandId")
    private UUID demandId;
    @JsonProperty(value="taxHeadCode")
    private String taxHeadCode;
    @JsonProperty(value="amount")
    private BigDecimal amount;
    @JsonProperty(value="collectedAmount")
    private BigDecimal collectedAmount;
    @JsonProperty(value="metadata")
    private Map<String, Object> metadata;
}