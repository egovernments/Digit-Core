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
public class PaymentDetailCreate {
    @JsonProperty(value="totalAmountPaid")
    private Double totalAmountPaid;
    @JsonProperty(value="manualReceiptNumber")
    private String manualReceiptNumber;
    @JsonProperty(value="manualReceiptDate")
    private Long manualReceiptDate;
    @JsonProperty(value="billId")
    private String billId;
    @JsonProperty(value="metadata")
    private Map<String, Object> metadata;
}