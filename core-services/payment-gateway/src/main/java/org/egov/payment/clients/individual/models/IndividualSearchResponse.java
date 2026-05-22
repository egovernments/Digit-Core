package org.egov.payment.clients.individual.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class IndividualSearchResponse {

    @JsonProperty("Individuals")
    private List<Individual> individuals;

    @JsonProperty("totalCount")
    private Long totalCount;
}
