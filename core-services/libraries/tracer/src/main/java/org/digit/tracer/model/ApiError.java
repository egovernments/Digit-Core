package org.digit.tracer.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
    @JsonProperty("id")          String id,
    @JsonProperty("parentId")    String parentId,
    @JsonProperty("code")        String code,
    @JsonProperty("message")     String message,
    @JsonProperty("description") String description,
    @JsonProperty("params")      List<String> params
) {
    public static ApiError of(String code, String message) {
        return new ApiError(null, null, code, message, null, null);
    }

    public static ApiError of(String code, String message, String description) {
        return new ApiError(null, null, code, message, description, null);
    }
}
