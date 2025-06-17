package digit.service.enrichment;

import digit.util.AuditDetailsEnrichmentUtil;
import digit.util.TenantUtil;
import digit.web.models.Tenant;
import digit.web.models.TenantRequest;
import org.egov.common.utils.UUIDEnrichmentUtil;
import org.springframework.stereotype.Component;

@Component
public class TenantDataEnricher {

    private AuditDetailsEnrichmentUtil auditDetailsEnrichmentUtil;

    private TenantUtil tenantUtil;

    public TenantDataEnricher(AuditDetailsEnrichmentUtil auditDetailsEnrichmentUtil, TenantUtil tenantUtil) {
        this.auditDetailsEnrichmentUtil = auditDetailsEnrichmentUtil;
        this.tenantUtil = tenantUtil;
    }

    public void enrichCreateReq(TenantRequest tenantRequest) {

        Tenant tenant = tenantRequest.getTenant();

        // Set the code
        String code = tenantUtil.convertNameToCode(tenantRequest.getTenant().getName());
        tenantRequest.getTenant().setCode(code);
        tenantRequest.getTenant().setParentId(code);

        // enrich id
        UUIDEnrichmentUtil.enrichRandomUuid(tenant, "id");

        // enrich audit details
        tenant.setAuditDetails(auditDetailsEnrichmentUtil.prepareAuditDetails(tenant.getAuditDetails(), tenantRequest.getRequestInfo(), Boolean.TRUE));

    }

    public void enrichUpdateReq(TenantRequest tenantRequest) {

        Tenant tenant = tenantRequest.getTenant();
        tenant.setAuditDetails(AuditDetailsEnrichmentUtil.prepareAuditDetails(tenant.getAuditDetails(), tenantRequest.getRequestInfo(), Boolean.TRUE));

    }
}