package org.egov.payment.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.egov.tracer.model.AuditDetails;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionDump {

    @JsonProperty("txnId")
    private String txnId;

    @JsonProperty("txnRequest")
    private String txnRequest;

    @JsonProperty("txnResponse")
    private Object txnResponse;

    @JsonProperty("auditDetails")
    private AuditDetails auditDetails;
}
