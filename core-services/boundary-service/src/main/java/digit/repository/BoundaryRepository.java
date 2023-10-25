package digit.repository;

import digit.web.models.Boundary;
import digit.web.models.BoundarySearchCriteria;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface BoundaryRepository {

    public List<Boundary> searchBoundaryEntity(BoundarySearchCriteria boundarySearchCriteria);
    public Set<String> getCodeListByTenantId(String tenantId);
}
