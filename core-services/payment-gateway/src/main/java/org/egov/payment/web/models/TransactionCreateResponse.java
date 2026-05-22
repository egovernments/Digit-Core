package org.egov.payment.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.*;
import org.egov.payment.models.Transaction;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionCreateResponse {

    @JsonProperty("Transaction")
    @Valid
    private Transaction transaction;
}
