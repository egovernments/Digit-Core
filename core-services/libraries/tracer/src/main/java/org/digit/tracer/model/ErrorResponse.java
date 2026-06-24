package org.digit.tracer.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    @JsonProperty("Errors") List<ApiError> errors
) {
    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(List.of(ApiError.of(code, message)));
    }

    public static ErrorResponse of(List<ApiError> errors) {
        return new ErrorResponse(errors);
    }
}
