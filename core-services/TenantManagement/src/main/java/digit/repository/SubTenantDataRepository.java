package digit.repository;

import digit.web.models.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubTenantDataRepository {

    public void create(SubTenantRequest tenantRequest);

    public void update(SubTenantRequest tenantRequest);

    List<SubTenant> search(SubTenantDataSearchCriteria searchCriteria);
}
