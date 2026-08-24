package org.digit.services.employee.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Payload for creating or replacing a jurisdiction. Include {@code version} on an update. */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JurisdictionRequest {
    @JsonProperty("boundaryRelation")
    private List<BoundaryRef> boundaryRelation;
    @JsonProperty("isActive")
    private Boolean isActive;
    @JsonProperty("version")
    private Integer version;
}
