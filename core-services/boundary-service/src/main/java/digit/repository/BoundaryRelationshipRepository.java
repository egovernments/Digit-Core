package digit.repository;

import digit.web.models.BoundaryRelationshipDTO;
import digit.web.models.BoundaryRelationshipRequest;
import digit.web.models.BoundaryRelationshipRequestDTO;
import digit.web.models.BoundaryRelationshipSearchCriteria;
import java.util.List;

public interface BoundaryRelationshipRepository {

    public void create(BoundaryRelationshipRequest boundaryRelationshipRequest);

    public void update(BoundaryRelationshipRequestDTO boundaryRelationshipRequest);

    public List<BoundaryRelationshipDTO> search(BoundaryRelationshipSearchCriteria boundaryRelationshipSearchCriteria);

}
