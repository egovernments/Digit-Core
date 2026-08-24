package org.digit.services.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** One entry of the error array DIGIT services return on a failed request. */
@JsonIgnoreProperties(ignoreUnknown=true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DigitError {
    @JsonProperty(value="code")
    private String code;
    @JsonProperty(value="message")
    private String message;
    @JsonProperty(value="description")
    private String description;
    @JsonProperty(value="params")
    private List<String> params;
}
