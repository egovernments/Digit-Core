package org.egov.payment.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.*;
import org.egov.payment.models.Transaction;
import org.springframework.validation.annotation.Validated;

/**
 * The payment object, containing all necessary information for initiating a payment.
 */
@Validated
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionRequest {

    @JsonProperty("Transaction")
    @Valid
    private Transaction transaction;
}
