package org.egov.infra.mdms.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MdmsHistory {

    @JsonProperty("changeDetails")
    private ChangeDetails changeDetails = null;

    @JsonProperty("mdms")
    private Mdms mdms = null;
}
