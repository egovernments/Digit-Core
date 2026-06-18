package digit.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Builder;

import java.util.List;

/**
 * Request payload for creating boundary relationships in bulk.
 *
 * <p>Each record in {@code boundaryRelationships} is expected to be a sibling at the same
 * level whose parent has already been persisted (callers batch children under a single,
 * already-persisted parent). The batch is capped to keep a single request bounded; callers
 * are responsible for chunking larger levels into multiple requests.</p>
 */
@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BulkBoundaryRelationshipRequest {

    @JsonProperty("RequestInfo")
    @Valid
    private RequestInfo requestInfo = null;

    @JsonProperty("BoundaryRelationships")
    @NotNull
    @Valid
    @Size(min = 1, max = 100)
    private List<BoundaryRelation> boundaryRelationships = null;

}
