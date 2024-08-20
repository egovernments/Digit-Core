package digit.service.enrichment;

import digit.web.models.Tenant;
import digit.web.models.TenantRequest;
import org.egov.common.utils.AuditDetailsEnrichmentUtil;
import org.egov.common.utils.UUIDEnrichmentUtil;
import org.springframework.stereotype.Component;

@Component
public class TenantDataEnricher {

    public void enrichCreateReq(TenantRequest tenantRequest){

        Tenant tenant = tenantRequest.getTenant();

        // enrich code
        tenantRequest.getTenant().setCode(tenantRequest.getTenant().getName());

        // enrich id
        UUIDEnrichmentUtil.enrichRandomUuid(tenant, "id");

        // enrich audit details
        tenant.setAuditDetails(AuditDetailsEnrichmentUtil.prepareAuditDetails(tenant.getAuditDetails(), tenantRequest.getRequestInfo(), Boolean.TRUE));

    }

    public void enrichUpdateReq(TenantRequest tenantRequest){

        Tenant tenant = tenantRequest.getTenant();

        tenant.setAuditDetails(AuditDetailsEnrichmentUtil.prepareAuditDetails(tenant.getAuditDetails(), tenantRequest.getRequestInfo(), Boolean.TRUE));

    }
}