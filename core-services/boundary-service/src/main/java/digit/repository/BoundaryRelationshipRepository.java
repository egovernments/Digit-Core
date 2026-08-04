package digit.repository;

import digit.web.models.BoundaryRelation;
import digit.web.models.BoundaryRelationshipDTO;
import digit.web.models.BoundaryRelationshipRequest;
import digit.web.models.BoundaryRelationshipRequestDTO;
import digit.web.models.BoundaryRelationshipSearchCriteria;
import org.egov.common.contract.request.RequestInfo;
import java.util.List;

public interface BoundaryRelationshipRepository {

    public void create(BoundaryRelationshipRequest boundaryRelationshipRequest);

    /**
     * Persists the given validated and enriched boundary relationships through egov-persister (no direct
     * DB write): the WHOLE list is published as ONE message to the DEDICATED bulk topic
     * {@code boundary-relationship-bulk-create-job} (NOT the single {@link #create} topic
     * {@code save-boundary-relationship}), carrying the relationships as an array under the
     * {@code BoundaryRelationship} key. A dedicated topic is required because the persister maps this one
     * with an array base path ({@code $.BoundaryRelationship.*}) while single-create stays single-object
     * ({@code $.BoundaryRelationship}) — one queryMap has one base path, so the two shapes cannot share a
     * topic. The publish is blocking (it returns once the broker has accepted the message); egov-persister
     * reads the array and writes it as a single batchUpdate through the idempotent
     * INSERT ... ON CONFLICT (tenantId, code, hierarchyType) DO NOTHING, so redelivery is a safe no-op and
     * duplicates are skipped without aborting the batch. Batching is WITHIN the one message, so it needs no
     * persister.batch.topics / persister.bulk.enabled configuration.
     *
     * @param boundaryRelationships validated and enriched relationships to persist
     * @param requestInfo request info propagated onto the published message
     */
    public void createBulk(List<BoundaryRelation> boundaryRelationships, RequestInfo requestInfo);

    public void update(BoundaryRelationshipRequestDTO boundaryRelationshipRequest);

    public List<BoundaryRelationshipDTO> search(BoundaryRelationshipSearchCriteria boundaryRelationshipSearchCriteria);

}
