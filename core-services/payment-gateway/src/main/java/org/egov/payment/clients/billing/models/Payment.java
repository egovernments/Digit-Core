package org.egov.payment.clients.billing.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.payment.clients.billing.models.enums.InstrumentStatus;
import org.egov.payment.clients.billing.models.enums.PaymentMode;
import org.egov.payment.clients.billing.models.enums.PaymentStatus;
import org.egov.tracer.model.AuditDetails;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class Payment {

    @JsonProperty("id")
    private String id;

    @JsonProperty("totalAmountDue")
    private BigDecimal totalAmountDue;

    @JsonProperty("totalAmountPaid")
    private BigDecimal totalAmountPaid;

    @JsonProperty("transactionNumber")
    private String transactionNumber;

    @JsonProperty("transactionDate")
    private Long transactionDate;

    @JsonProperty("paymentMode")
    private PaymentMode paymentMode;

    @JsonProperty("paymentStatus")
    private PaymentStatus paymentStatus;

    @JsonProperty("instrumentNumber")
    private String instrumentNumber;

    @JsonProperty("instrumentDate")
    private Long instrumentDate;

    @JsonProperty("instrumentStatus")
    private InstrumentStatus instrumentStatus;

    @JsonProperty("ifscCode")
    private String ifscCode;

    @JsonProperty("paidBy")
    private String paidBy;

    @JsonProperty("payerId")
    private String payerId;

    @JsonProperty("payerName")
    private String payerName;

    @JsonProperty("payerAddress")
    private String payerAddress;

    @JsonProperty("payerMobileNumber")
    private String payerMobileNumber;

    @JsonProperty("payerEmail")
    private String payerEmail;

    @JsonProperty("fileStoreId")
    private String fileStoreId;

    @JsonProperty("paymentDetails")
    private List<PaymentDetail> paymentDetails;

    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    @JsonProperty("auditDetails")
    private AuditDetails auditDetails;
}
