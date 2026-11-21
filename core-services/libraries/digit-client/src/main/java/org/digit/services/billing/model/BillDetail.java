package org.digit.services.billing.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillDetail {
    @JsonProperty(value="id")
    private String id;
    @JsonProperty(value="billId")
    private String billId;
    @JsonProperty(value="demandId")
    private String demandId;
    @JsonProperty(value="periodFrom")
    private Long periodFrom;
    @JsonProperty(value="periodTo")
    private Long periodTo;
    @JsonProperty(value="amount")
    private Double amount;
    @JsonProperty(value="amountPaid")
    private Double amountPaid;
    @JsonProperty(value="billAccountDetails")
    private List<BillAccountDetail> billAccountDetails;
    @JsonProperty(value="metadata")
    private Map<String, Object> metadata;
}