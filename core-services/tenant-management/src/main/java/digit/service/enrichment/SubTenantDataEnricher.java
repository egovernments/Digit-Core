package digit.service.enrichment;

import digit.util.AuditDetailsEnrichmentUtil;
import digit.util.TenantUtil;
import digit.web.models.SubTenant;
import digit.web.models.SubTenantRequest;
import digit.web.models.Tenant;
import digit.web.models.TenantRequest;
import org.egov.common.utils.UUIDEnrichmentUtil;
import org.springframework.stereotype.Component;

@Component
public class SubTenantDataEnricher {

    private AuditDetailsEnrichmentUtil auditDetailsEnrichmentUtil;

    private TenantUtil tenantUtil;

    public SubTenantDataEnricher(AuditDetailsEnrichmentUtil auditDetailsEnrichmentUtil, TenantUtil tenantUtil) {
        this.auditDetailsEnrichmentUtil = auditDetailsEnrichmentUtil;
        this.tenantUtil = tenantUtil;
    }

    public void enrichCreateReq(SubTenantRequest tenantRequest) {

        SubTenant subTenant = tenantRequest.getTenant();

        // Set the code
        String subTenantCode = tenantUtil.generateSubTenantCode(tenantRequest);
        subTenant.setCode(subTenantCode);

        // enrich id
        UUIDEnrichmentUtil.enrichRandomUuid(subTenant, "id");

        // enrich audit details
        subTenant.setAuditDetails(auditDetailsEnrichmentUtil.prepareAuditDetails(subTenant.getAuditDetails(), tenantRequest.getRequestInfo(), Boolean.TRUE));

    }

    public void enrichUpdateReq(SubTenantRequest tenantRequest) {

        SubTenant tenant = tenantRequest.getTenant();

        tenant.setAuditDetails(AuditDetailsEnrichmentUtil.prepareAuditDetails(tenant.getAuditDetails(), tenantRequest.getRequestInfo(), Boolean.TRUE));

    }
}