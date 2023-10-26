package digit.repository;

import digit.web.models.*;
import java.util.List;

public interface BoundaryHierarchyRepository {

    public void create(BoundaryTypeHierarchyRequest boundaryTypeHierarchyRequest);

    public void update(BoundaryTypeHierarchyRequest boundaryTypeHierarchyRequest);

    public List<BoundaryTypeHierarchyDefinition> search(BoundaryTypeHierarchySearchCriteria boundaryTypeHierarchySearchCriteria);

}
