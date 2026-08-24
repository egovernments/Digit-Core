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
public class PaymentDetailCreate {
    @JsonProperty(value="totalAmountPaid")
    private BigDecimal totalAmountPaid;
    @JsonProperty(value="manualReceiptNumber")
    private String manualReceiptNumber;
    @JsonProperty(value="manualReceiptDate")
    private Long manualReceiptDate;
    @JsonProperty(value="billId")
    private UUID billId;
    @JsonProperty(value="metadata")
    private Map<String, Object> metadata;
}