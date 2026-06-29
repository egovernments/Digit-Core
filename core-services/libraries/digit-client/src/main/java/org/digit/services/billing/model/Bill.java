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
public class Bill {
    @JsonProperty(value="id")
    private String id;
    @JsonProperty(value="consumerCode")
    private String consumerCode;
    @JsonProperty(value="businessServiceCode")
    private String businessServiceCode;
    @JsonProperty(value="payerId")
    private String payerId;
    @JsonProperty(value="payerName")
    private String payerName;
    @JsonProperty(value="payerAddress")
    private String payerAddress;
    @JsonProperty(value="payerMobileNumber")
    private String payerMobileNumber;
    @JsonProperty(value="payerEmail")
    private String payerEmail;
    @JsonProperty(value="billNumber")
    private String billNumber;
    @JsonProperty(value="billIssueAt")
    private Long billIssueAt;
    @JsonProperty(value="billExpiryAt")
    private Long billExpiryAt;
    @JsonProperty(value="totalAmount")
    private Double totalAmount;
    @JsonProperty(value="totalCollectedAmount")
    private Double totalCollectedAmount;
    @JsonProperty(value="billDetails")
    private List<BillDetail> billDetails;
    @JsonProperty(value="status")
    private String status;
    @JsonProperty(value="metadata")
    private Map<String, Object> metadata;
}