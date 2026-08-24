package org.digit.services.billing.model;

import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
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
public class BillDetail {
    @JsonProperty(value="id")
    private UUID id;
    @JsonProperty(value="billId")
    private UUID billId;
    @JsonProperty(value="demandId")
    private UUID demandId;
    @JsonProperty(value="periodFrom")
    private long periodFrom;
    @JsonProperty(value="periodTo")
    private long periodTo;
    @JsonProperty(value="amount")
    private BigDecimal amount;
    @JsonProperty(value="amountPaid")
    private BigDecimal amountPaid;
    @JsonProperty(value="billAccountDetails")
    private List<BillAccountDetail> billAccountDetails;
    @JsonProperty(value="metadata")
    private Map<String, Object> metadata;
}