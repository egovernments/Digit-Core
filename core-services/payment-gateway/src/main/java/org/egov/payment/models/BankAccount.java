package org.egov.payment.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BankAccount {

    private Long id;
    private String accountNumber;
    private String description;
    private Boolean active;
    private String type;
}
