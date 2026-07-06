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
     * DB write): each record is published, one message per record, to the same save-boundary-relationship
     * topic the single {@link #create} uses. The publish is blocking (it returns once the broker has
     * accepted each record); egov-persister then writes each via an idempotent
     * INSERT ... ON CONFLICT (tenantId, code, hierarchyType) DO NOTHING, so redelivery is a safe no-op and
     * one un-insertable record never fails the others. Optionally listing that topic in the persister's
     * persister.batch.topics lets it aggregate a poll into one multi-row insert for throughput.
     *
     * @param boundaryRelationships validated and enriched relationships to persist
     * @param requestInfo request info propagated onto each published message
     */
    public void createBulk(List<BoundaryRelation> boundaryRelationships, RequestInfo requestInfo);

    public void update(BoundaryRelationshipRequestDTO boundaryRelationshipRequest);

    public List<BoundaryRelationshipDTO> search(BoundaryRelationshipSearchCriteria boundaryRelationshipSearchCriteria);

}
