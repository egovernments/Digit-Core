package org.egov.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MobileValidationConfig {
    private MobileValidationRules rules;
    private String fieldType;
    @JsonProperty("default")
    private Boolean isDefault;
    private MobileValidationAttributes attributes;
}
