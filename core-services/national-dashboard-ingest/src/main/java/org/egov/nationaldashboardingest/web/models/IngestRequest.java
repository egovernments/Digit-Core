package org.egov.nationaldashboardingest.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.egov.common.contract.request.RequestInfo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class IngestRequest {

    @Valid
    @NotNull
    @NotEmpty
    @JsonProperty("Data")
    private List<Data> ingestData;

    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;

}
