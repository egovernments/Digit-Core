package org.egov.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MobileValidationRules {

    @JsonProperty("prefix")
    private String prefix;

    @JsonProperty("pattern")
    private String pattern;

    @JsonProperty("isActive")
    private Boolean isActive;

    @JsonProperty("maxLength")
    private Integer maxLength;

    @JsonProperty("minLength")
    private Integer minLength;

    @JsonProperty("errorMessage")
    private String errorMessage;

    @JsonProperty("allowedStartingDigits")
    private List<String> allowedStartingDigits;
}
