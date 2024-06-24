package org.egov.sunbirdrc.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.egov.common.contract.request.RequestInfo;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MdmsSearch {
    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;

    @JsonProperty("SchemaDefCriteria")
    private SchemaDefCriteria schemaDefCriteria;
}
