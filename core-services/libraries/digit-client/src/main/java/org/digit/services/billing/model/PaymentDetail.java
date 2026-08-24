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
public class PaymentDetail {
    @JsonProperty(value="id")
    private UUID id;
    @JsonProperty(value="paymentId")
    private UUID paymentId;
    @JsonProperty(value="billId")
    private UUID billId;
    @JsonProperty(value="totalAmountPaid")
    private BigDecimal totalAmountPaid;
    @JsonProperty(value="totalAmountDue")
    private BigDecimal totalAmountDue;
    @JsonProperty(value="businessServiceCode")
    private String businessServiceCode;
    @JsonProperty(value="manualReceiptNumber")
    private String manualReceiptNumber;
    @JsonProperty(value="manualReceiptDate")
    private Long manualReceiptDate;
    @JsonProperty(value="receiptNumber")
    private String receiptNumber;
    @JsonProperty(value="receiptDate")
    private long receiptDate;
    @JsonProperty(value="receiptType")
    private ReceiptType receiptType;
    @JsonProperty(value="bill")
    private Bill bill;
    @JsonProperty(value="metadata")
    private Map<String, Object> metadata;
}