package digit.repository;

import digit.web.models.BoundaryRelationshipDTO;
import digit.web.models.BoundaryRelationshipRequest;
import digit.web.models.BoundaryRelationshipSearchCriteria;
import java.util.List;

public interface BoundaryRelationshipRepository {

    public void create(BoundaryRelationshipRequest boundaryTypeHierarchyRequest);

    public void update(BoundaryRelationshipRequest boundaryTypeHierarchyRequest);

    public List<BoundaryRelationshipDTO> search(BoundaryRelationshipSearchCriteria boundaryRelationshipSearchCriteria);

}
