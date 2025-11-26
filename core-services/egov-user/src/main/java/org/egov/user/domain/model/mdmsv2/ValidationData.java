package org.egov.user.domain.model.mdmsv2;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationData {

    @JsonProperty("rules")
    private ValidationRules rules;

    @JsonProperty("validationName")
    private String validationName;
}
