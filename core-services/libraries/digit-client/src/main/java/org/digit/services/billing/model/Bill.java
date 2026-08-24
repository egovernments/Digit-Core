package org.digit.services.billing.model;

import java.util.UUID;
import org.digit.services.common.model.AuditDetails;
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
public class Bill {
    @JsonProperty(value="id")
    private UUID id;
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
    private long billIssueAt;
    @JsonProperty(value="billExpiryAt")
    private Long billExpiryAt;
    @JsonProperty(value="totalAmount")
    private BigDecimal totalAmount;
    @JsonProperty(value="totalCollectedAmount")
    private BigDecimal totalCollectedAmount;
    @JsonProperty(value="billDetails")
    private List<BillDetail> billDetails;
    @JsonProperty(value="status")
    private BillStatus status;
    @JsonProperty(value="metadata")
    private Map<String, Object> metadata;
    @JsonProperty(value="auditDetail")
    private AuditDetails auditDetail;
}