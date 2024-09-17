package digit.repository;

import digit.web.models.Tenant;
import digit.web.models.TenantDataSearchCriteria;
import digit.web.models.TenantRequest;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TenantDataRepository {

    public void create(TenantRequest tenantRequest);

    public void update(TenantRequest tenantRequest);

    List<Tenant> search(TenantDataSearchCriteria searchCriteria);
}
