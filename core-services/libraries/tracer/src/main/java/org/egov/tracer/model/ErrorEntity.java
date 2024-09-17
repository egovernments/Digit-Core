package org.egov.tracer.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorEntity {
    @JsonProperty("exception")
    private Exception exception = null;

    @JsonProperty("type")
    private ErrorType errorType = null;

    @JsonProperty("errorCode")
    private String errorCode = null;

    @JsonProperty("errorMessage")
    private String errorMessage = null;

    @JsonProperty("additionalDetails")
    private Object additionalDetails = null;
}
