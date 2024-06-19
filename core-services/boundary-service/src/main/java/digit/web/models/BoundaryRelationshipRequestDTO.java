package digit.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BoundaryRelationshipRequestDTO {

    @JsonProperty("BoundaryRelationship")
    private BoundaryRelationshipDTO boundaryRelationshipDTO;

    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;

    @JsonProperty("BoundaryRelationshipList")
    private List<BoundaryRelationshipDTO> boundaryRelationshipDTOList;

}
