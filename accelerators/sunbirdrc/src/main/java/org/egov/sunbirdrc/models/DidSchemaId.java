package org.egov.sunbirdrc.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Component
public class DidSchemaId {

    @JsonProperty("did")
    private String did;

    @JsonProperty("schemaId")
    private String schemaId;

}
