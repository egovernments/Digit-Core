package org.egov.payment.clients.billing.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDetailCreate {

    @JsonProperty("totalAmountPaid")
    private BigDecimal totalAmountPaid;

    @JsonProperty("manualReceiptNumber")
    private String manualReceiptNumber;

    @JsonProperty("manualReceiptDate")
    private Long manualReceiptDate;

    @JsonProperty("billId")
    private String billId;

    @JsonProperty("metadata")
    private Map<String, Object> metadata;
}
