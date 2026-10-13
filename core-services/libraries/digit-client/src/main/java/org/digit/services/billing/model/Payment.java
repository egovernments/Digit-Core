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
public class Payment {
    @JsonProperty(value="id")
    private String id;
    @JsonProperty(value="totalAmountPaid")
    private Double totalAmountPaid;
    @JsonProperty(value="totalAmountDue")
    private Double totalAmountDue;
    @JsonProperty(value="transactionNumber")
    private String transactionNumber;
    @JsonProperty(value="transactionDate")
    private Long transactionDate;
    @JsonProperty(value="paymentMode")
    private String paymentMode;
    @JsonProperty(value="instrumentDate")
    private Long instrumentDate;
    @JsonProperty(value="instrumentNumber")
    private String instrumentNumber;
    @JsonProperty(value="instrumentStatus")
    private String instrumentStatus;
    @JsonProperty(value="paymentStatus")
    private String paymentStatus;
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
    private List<PaymentDetail> paymentDetails;
    @JsonProperty(value="metadata")
    private Map<String, Object> metadata;
}