package org.egov.sunbirdrc.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.*;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MdmsSchema {

    @JsonProperty("RequestInfo")
    @Valid
    private RequestInfo requestInfo;

    @JsonProperty("mdmsData")
    @Valid
    private MdmsData mdmsData;

    @JsonProperty("schema")
    @Valid
    private Schema schema;

    private List<String> tags;

    @JsonProperty("status")
    @Valid
    private String status;

    @JsonProperty("tenantId")
    private String tenantId;
}

