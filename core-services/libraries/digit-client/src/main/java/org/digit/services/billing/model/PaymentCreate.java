package org.digit.services.billing.model;

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
public class PaymentCreate {
    @JsonProperty(value="totalAmountPaid")
    private BigDecimal totalAmountPaid;
    @JsonProperty(value="transactionNumber")
    private String transactionNumber;
    @JsonProperty(value="transactionDate")
    private Long transactionDate;
    @JsonProperty(value="paymentMode")
    private PaymentMode paymentMode;
    @JsonProperty(value="instrumentNumber")
    private String instrumentNumber;
    @JsonProperty(value="instrumentDate")
    private Long instrumentDate;
    @JsonProperty(value="ifscCode")
    private String ifscCode;
    @JsonProperty(value="paidBy")
    private String paidBy;
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
    @JsonProperty(value="fileStoreId")
    private String fileStoreId;
    @JsonProperty(value="paymentDetails")
    private List<PaymentDetailCreate> paymentDetails;
    @JsonProperty(value="metadata")
    private Map<String, Object> metadata;
}