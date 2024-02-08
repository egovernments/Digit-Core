package org.egov.pg.models;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class InstrumentTypeProperty {

    @NotNull
    private TransactionType transactionType;
    @NotNull
    private Boolean reconciledOncreate;
    @NotNull
    private InstrumentStatus statusOnCreate;
    @NotNull
    private InstrumentStatus statusOnUpdate;
    @NotNull
    private InstrumentStatus statusOnReconcile;
}
