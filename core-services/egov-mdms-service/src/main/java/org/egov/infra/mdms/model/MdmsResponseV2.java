package org.egov.infra.mdms.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.response.ResponseInfo;

import jakarta.validation.Valid;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MdmsResponseV2 {

    @JsonProperty("ResponseInfo")
    @Valid
    private ResponseInfo responseInfo = null;

    @JsonProperty("mdms")
    private List<Mdms> mdms = null;
}
