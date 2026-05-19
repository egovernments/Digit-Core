package org.egov.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
    private String pattern;
    private Integer maxLength;
    private Integer minLength;
    private String errorMessage;
    private List<String> allowedStartingCharacters;
}
