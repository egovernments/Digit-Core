package org.egov.payment.clients.billing.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.payment.clients.billing.models.enums.ReceiptType;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
public class PaymentDetail {

    @JsonProperty("totalAmountDue")
    private BigDecimal totalAmountDue;

    @JsonProperty("totalAmountPaid")
    private BigDecimal totalAmountPaid;

    @JsonProperty("manualReceiptNumber")
    private String manualReceiptNumber;

    @JsonProperty("manualReceiptDate")
    private Long manualReceiptDate;

    @JsonProperty("receiptNumber")
    private String receiptNumber;

    @JsonProperty("receiptDate")
    private Long receiptDate;

    @JsonProperty("receiptType")
    private ReceiptType receiptType;

    @JsonProperty("businessServiceCode")
    private String businessServiceCode;

    @JsonProperty("billId")
    private String billId;

    @JsonProperty("metadata")
    private Map<String, Object> metadata;
}
