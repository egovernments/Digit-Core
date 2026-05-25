package org.egov.user.domain.model.mdmsv2;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MdmsV2SearchCriteria {

    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("schemaCode")
    private String schemaCode;

    @JsonProperty("limit")
    @Builder.Default
    private Integer limit = 1000;

    @JsonProperty("offset")
    @Builder.Default
    private Integer offset = 0;
}
