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
public class PaymentDetail {
    @JsonProperty(value="id")
    private String id;
    @JsonProperty(value="paymentId")
    private String paymentId;
    @JsonProperty(value="billId")
    private String billId;
    @JsonProperty(value="totalAmountPaid")
    private Double totalAmountPaid;
    @JsonProperty(value="totalAmountDue")
    private Double totalAmountDue;
    @JsonProperty(value="businessServiceCode")
    private String businessServiceCode;
    @JsonProperty(value="manualReceiptNumber")
    private String manualReceiptNumber;
    @JsonProperty(value="manualReceiptDate")
    private Long manualReceiptDate;
    @JsonProperty(value="receiptNumber")
    private String receiptNumber;
    @JsonProperty(value="receiptDate")
    private Long receiptDate;
    @JsonProperty(value="receiptType")
    private String receiptType;
    @JsonProperty(value="bill")
    private Bill bill;
    @JsonProperty(value="metadata")
    private Map<String, Object> metadata;
}