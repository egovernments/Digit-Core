package digit.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;

import java.util.List;

/**
 * Kafka payload for persisting a validated batch of boundary relationships in ONE message.
 *
 * <p>The whole validated list is published as a single message under the {@code BoundaryRelationship}
 * key (an ARRAY here, whereas the single-create {@link BoundaryRelationshipRequestDTO} publishes the
 * same key as a single object). The persister's {@code save-boundary-relationship} mapping reads this
 * with an array base path ({@code $.BoundaryRelationship.*}) so {@code PersistRepository.getRows}
 * emits one row per element and the listener does ONE {@code jdbcTemplate.batchUpdate} for the whole
 * message.</p>
 *
 * <p>{@code RequestInfo} is retained at the top level so the persister's version filter
 * ({@code $.RequestInfo.ver}) still selects the mapping exactly as it does for the single message.</p>
 *
 * <p>The elements are {@link BoundaryRelationshipDTO} (not the contract {@link BoundaryRelation})
 * because {@code BoundaryRelation.ancestralMaterializedPath} is {@code @JsonIgnore}; the DTO exposes it
 * as {@code ancestralMaterializedPath}, so the enriched path is carried on the wire and persisted —
 * identical to the single-create path, which serializes the same DTO field.</p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BulkBoundaryRelationshipRequestDTO {

    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;

    @JsonProperty("BoundaryRelationship")
    private List<BoundaryRelationshipDTO> boundaryRelationship;

}
