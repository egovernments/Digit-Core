package org.egov.tracer.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

/**
 * Error object will be returned as a part of reponse body in conjunction with
 * ResponseInfo as part of ErrorResponse whenever the request processing status
 * in the ResponseInfo is FAILED.
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Error {
    @JsonProperty("id")
    private String id = null;

    @JsonProperty("parentId")
    private String parentId = null;

    @JsonProperty("code")
    private String code = null;

    @JsonProperty("message")
    private String message = null;

    @JsonProperty("description")
    private String description = null;

    @JsonProperty("params")
    private List<String> params = null;

    public Error(String id, String code, String message, String description){
        this.id = id;
        this.code = code;
        this.message = message;
        this.description = description;
    }

}
