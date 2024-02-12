package org.egov.pg.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import org.egov.pg.models.enums.InstrumentStatusEnum;
import org.egov.pg.models.enums.CollectionPaymentModeEnum;
import org.egov.pg.models.enums.PaymentStatusEnum;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class CollectionPayment {

    @Size(max=64)
    @JsonProperty("id")
    private String id;

    @NotNull
    @Size(max=64)
    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("totalDue")
    private BigDecimal totalDue;

    @NotNull
    @JsonProperty("totalAmountPaid")
    private BigDecimal totalAmountPaid;

    @Size(max=128)
    @JsonProperty("transactionNumber")
    private String transactionNumber;

    @JsonProperty("transactionDate")
    private Long transactionDate;

    @NotNull
    @JsonProperty("paymentMode")
    private CollectionPaymentModeEnum paymentMode;

    
    @JsonProperty("instrumentDate")
    private Long instrumentDate;

    @Size(max=128)
    @JsonProperty("instrumentNumber")
    private String instrumentNumber;

    @JsonProperty("instrumentStatus")
    private InstrumentStatusEnum instrumentStatus;

    @Size(max=64)
    @JsonProperty("ifscCode")
    private String ifscCode;

    @JsonProperty("auditDetails")
    private AuditDetails auditDetails;

    @JsonProperty("additionalDetails")
    private JsonNode additionalDetails;

    @JsonProperty("paymentDetails")
    @Valid
    private List<CollectionPaymentDetail> paymentDetails;

    @Size(max=128)
    @NotNull
    @JsonProperty("paidBy")
    private String paidBy = null;

    @Size(max=64)
    @NotNull
    @JsonProperty("mobileNumber")
    private String mobileNumber = null;

    @Size(max=128)
    @JsonProperty("payerName")
    private String payerName = null;

    @Size(max=1024)
    @JsonProperty("payerAddress")
    private String payerAddress = null;

    @Size(max=64)
    @JsonProperty("payerEmail")
    private String payerEmail = null;

    @Size(max=64)
    @JsonProperty("payerId")
    private String payerId = null;

    @JsonProperty("paymentStatus")
    private PaymentStatusEnum paymentStatus;

    @JsonProperty("fileStoreId")
    private String fileStoreId;


    public CollectionPayment addpaymentDetailsItem(CollectionPaymentDetail paymentDetail) {
        if (this.paymentDetails == null) {
            this.paymentDetails = new ArrayList<>();
        }
        this.paymentDetails.add(paymentDetail);
        return this;
    }




}
