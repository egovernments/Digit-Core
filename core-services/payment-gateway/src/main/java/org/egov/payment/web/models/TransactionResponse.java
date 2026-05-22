package org.egov.payment.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.*;
import org.egov.payment.models.Transaction;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * The payment response object, representing the status of the payment.
 */
@Validated
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionResponse {

    @JsonProperty("Transaction")
    @Valid
    private List<Transaction> transactions;
}
