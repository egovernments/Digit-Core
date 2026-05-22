package org.egov.payment.clients.registry.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class DataSearchResponse {

    @JsonProperty("success")
    private Boolean success;

    @JsonProperty("data")
    private List<RegistryData> data;

    @JsonProperty("error")
    private String error;

    @JsonProperty("message")
    private String message;
}
