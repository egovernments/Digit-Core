package org.egov.pg.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.*;
import org.egov.pg.models.Transaction;
import org.springframework.validation.annotation.Validated;
import java.util.List;

/**
 * The payment seatch response object, representing the status of the payment
 */
@Validated
//@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2018-06-05T12:58:12.679+05:30")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentSearchResponse {

    @JsonProperty("responseInfo")
    @Valid
    private ResponseInfo responseInfo = null;

    @JsonProperty("TransactionStatus")
    @Valid
    private List<Transaction
            > transactionStatus = null;


}

