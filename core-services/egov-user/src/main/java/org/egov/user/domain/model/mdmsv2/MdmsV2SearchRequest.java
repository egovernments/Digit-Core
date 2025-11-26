package org.egov.user.domain.model.mdmsv2;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MdmsV2SearchRequest {

    @JsonProperty("MdmsCriteria")
    private MdmsV2SearchCriteria mdmsCriteria;

    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;
}
