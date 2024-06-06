package org.egov.sunbirdrc.models;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.stereotype.Component;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Component
@Builder
public class DigitMDMSRequestBody {

    @JsonProperty("RequestInfo")
    RequestInfo requestInfo;

    @JsonProperty("SchemaDefinition")
    private SchemaDefinition SchemaDefinition;
}
