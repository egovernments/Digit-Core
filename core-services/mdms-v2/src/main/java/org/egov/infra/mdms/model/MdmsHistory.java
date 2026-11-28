package org.egov.infra.mdms.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MdmsHistory {

    @JsonProperty("id")
    private String id = null;

    @JsonProperty("tenantId")
    private String tenantId = null;

    @JsonProperty("schemaCode")
    private String schemaCode = null;

    @JsonProperty("uniqueIdentifier")
    private String uniqueIdentifier = null;

    @JsonProperty("isActive")
    private Boolean isActive = null;

    @JsonProperty("baselineMissing")
    private Boolean baselineMissing = null;

    @JsonProperty("truncated")
    private Boolean truncated = null;

    @JsonProperty("versions")
    private List<MdmsVersion> versions = null;

}
