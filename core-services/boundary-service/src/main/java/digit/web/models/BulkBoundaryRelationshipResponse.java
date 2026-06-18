package digit.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.egov.common.contract.response.ResponseInfo;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Builder;

import java.util.List;

/**
 * Response for a bulk boundary relationship create request.
 *
 * <p>Reports the outcome of each record explicitly: {@code successfulBoundaryRelationships}
 * holds the records that were durably persisted (with their generated ids), while
 * {@code failedBoundaryRelationships} holds the records that could not be created, each with a
 * reason. The whole request does not fail because of individual record failures.</p>
 */
@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BulkBoundaryRelationshipResponse {

    @JsonProperty("ResponseInfo")
    @Valid
    private ResponseInfo responseInfo = null;

    @JsonProperty("successfulBoundaryRelationships")
    @Valid
    private List<BoundaryRelation> successfulBoundaryRelationships = null;

    @JsonProperty("failedBoundaryRelationships")
    @Valid
    private List<FailedBoundaryRelationship> failedBoundaryRelationships = null;

}
