package org.egov.sunbirdrc.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MdmsSchema {

    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;

    private MdmsData mdmsData;
    private Schema schema;
    private List<String> tags;
    private String status;
}

