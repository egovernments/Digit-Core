package org.egov.payment.clients.individual.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndividualSearchCriteria {

    private List<String> id;
    private List<String> individualId;
    private String givenName;
    private List<String> mobileNumber;
    private String gender;
    private String dateOfBirth;
    private Integer limit;
    private Integer offset;
    private Boolean includeDeleted;
}
