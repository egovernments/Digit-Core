package digit.repository;

import digit.web.models.Boundary;
import digit.web.models.BoundaryRequest;
import digit.web.models.BoundarySearchCriteria;
import java.util.List;

public interface BoundaryRepository {

    public void create(BoundaryRequest boundaryRequest);

    public List<Boundary> search(BoundarySearchCriteria boundarySearchCriteria);

    public void update(BoundaryRequest boundaryRequest);

}
