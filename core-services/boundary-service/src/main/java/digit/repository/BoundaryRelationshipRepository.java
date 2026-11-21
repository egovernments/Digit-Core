package digit.repository;

import digit.web.models.BoundaryRelation;
import digit.web.models.BoundaryRelationshipDTO;
import digit.web.models.BoundaryRelationshipRequest;
import digit.web.models.BoundaryRelationshipRequestDTO;
import digit.web.models.BoundaryRelationshipSearchCriteria;
import java.util.List;

public interface BoundaryRelationshipRepository {

    public void create(BoundaryRelationshipRequest boundaryRelationshipRequest);

    /**
     * Synchronously persists the given validated and enriched boundary relationships within a
     * single database transaction (atomic committed write). Throws if the batch cannot be
     * committed, in which case no record from the batch is persisted.
     *
     * @param boundaryRelationships validated and enriched relationships to persist
     */
    public void createBulk(List<BoundaryRelation> boundaryRelationships);

    /**
     * Synchronously persists a single boundary relationship in its own transaction. Used as the
     * per-record fallback when an atomic {@link #createBulk} aborts on a row-level error: persisting
     * each row independently lets the good rows commit and isolates only the offending row(s), so one
     * un-insertable record no longer fails the rest of the batch. Throws if this row cannot be
     * committed.
     *
     * @param boundaryRelationship a validated and enriched relationship to persist
     */
    public void createOne(BoundaryRelation boundaryRelationship);

    public void update(BoundaryRelationshipRequestDTO boundaryRelationshipRequest);

    public List<BoundaryRelationshipDTO> search(BoundaryRelationshipSearchCriteria boundaryRelationshipSearchCriteria);

}
