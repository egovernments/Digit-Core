package org.egov.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Flat representation of the common-masters.MobileNumberValidation MDMS master.
 * Fields map 1-to-1 with the schema: countryCode, mobileNumberRegex, default.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MobileValidationConfig {

    @JsonProperty("countryCode")
    private String countryCode;

    @JsonProperty("mobileNumberRegex")
    private String mobileNumberRegex;

    @JsonProperty("default")
    private Boolean isDefault;
}
