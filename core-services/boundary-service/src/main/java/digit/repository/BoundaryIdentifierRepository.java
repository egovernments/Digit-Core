package digit.repository;

import digit.web.models.BoundaryIdentifier;
import digit.web.models.BoundaryIdentifierGenerateCriteria;
import digit.web.models.BoundaryIdentifierRequest;
import digit.web.models.BoundaryIdentifierSearchCriteria;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface BoundaryIdentifierRepository {

    public void create(BoundaryIdentifierRequest boundaryIdentifierRequest);

    public List<BoundaryIdentifier> search(BoundaryIdentifierSearchCriteria boundaryIdentifierSearchCriteria);

    public void update(BoundaryIdentifierRequest boundaryIdentifierRequest);

    public List<String> searchCodesWithoutIdentifier(BoundaryIdentifierGenerateCriteria generateCriteria);

    public Set<String> searchCodesInHierarchy(String tenantId, String hierarchyType, Collection<String> codes);

    public Set<String> searchExistingIdentifierIds(String tenantId, String hierarchyType, Collection<String> identifierIds);

}
