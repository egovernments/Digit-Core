package org.egov.pgr.web.models.pgrV1;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @JsonProperty("name")
    private String name;
    
    @JsonProperty("code")
    private String code;
    
    @JsonProperty("tenantId")
    private String tenantId;
}
