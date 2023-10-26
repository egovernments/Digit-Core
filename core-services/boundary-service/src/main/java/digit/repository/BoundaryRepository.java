package digit.repository;

import digit.web.models.Boundary;
import digit.web.models.BoundaryRequest;
import digit.web.models.BoundarySearchCriteria;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface BoundaryRepository {

    public void create(BoundaryRequest boundaryRequest);
    public List<Boundary> search(BoundarySearchCriteria boundarySearchCriteria);
    public void update(BoundaryRequest boundaryRequest);
    public Set<String> getCodeListByTenantId(String tenantId);

}
