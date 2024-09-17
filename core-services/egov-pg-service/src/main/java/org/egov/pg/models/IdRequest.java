package org.egov.pg.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class IdRequest {

    @JsonProperty("idName")
    @NotNull
    private String idName;

    @NotNull
    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("format")
    private String format;

}
