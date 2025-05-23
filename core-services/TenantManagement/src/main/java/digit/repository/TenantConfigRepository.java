package digit.repository;

import digit.web.models.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TenantConfigRepository {

    public void create(TenantConfigRequest tenantConfigRequest);

    public void update(TenantConfigRequest tenantConfigRequest);

    List<TenantConfig> search(TenantConfigSearchCriteria searchCriteria);
}
