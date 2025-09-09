package org.egov.wf.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessServiceDeleteRequest {
    @JsonProperty("tenantId")
    private String tenantId;
    @JsonProperty("businessService")
    private String businessServices;
}
