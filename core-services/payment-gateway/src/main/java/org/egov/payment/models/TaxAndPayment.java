package org.egov.payment.models;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TaxAndPayment {

    private BigDecimal taxAmount;

    @NotNull
    private BigDecimal amountPaid;

    @NotNull
    private String billId;
}
