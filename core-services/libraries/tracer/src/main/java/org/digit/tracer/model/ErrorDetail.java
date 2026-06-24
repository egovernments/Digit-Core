package org.digit.tracer.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorDetail(
    @JsonProperty("apiDetails") ApiDetails apiDetails,
    @JsonProperty("errors")     List<ErrorEntity> errors
) {
    public static ErrorDetail of(ApiDetails apiDetails, List<ErrorEntity> errors) {
        return new ErrorDetail(apiDetails, errors);
    }
}
