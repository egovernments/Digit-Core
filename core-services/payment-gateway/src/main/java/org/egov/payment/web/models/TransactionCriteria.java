package org.egov.payment.web.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.egov.payment.models.Transaction;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class TransactionCriteria {

    @JsonIgnore
    private String tenantId;

    private String txnId;

    private String billId;

    private String userUuid;

    private String receipt;

    private String consumerCode;

    @JsonIgnore
    private Long createdTime;

    private Transaction.TxnStatusEnum txnStatus;

    @JsonIgnore
    private int limit;

    @JsonIgnore
    private int offset;
}
