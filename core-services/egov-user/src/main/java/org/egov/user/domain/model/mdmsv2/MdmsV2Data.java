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
public class MdmsV2Data {

    @JsonProperty("id")
    private String id;

    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("schemaCode")
    private String schemaCode;

    @JsonProperty("uniqueIdentifier")
    private String uniqueIdentifier;

    @JsonProperty("data")
    private ValidationData data;

    @JsonProperty("isActive")
    private Boolean isActive;
}
