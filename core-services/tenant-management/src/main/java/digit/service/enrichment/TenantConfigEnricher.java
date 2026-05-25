package digit.service.enrichment;

import digit.util.AuditDetailsEnrichmentUtil;
import digit.web.models.Tenant;
import digit.web.models.TenantConfig;
import digit.web.models.TenantConfigRequest;
import digit.web.models.TenantRequest;
import org.egov.common.utils.UUIDEnrichmentUtil;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TenantConfigEnricher {

    private AuditDetailsEnrichmentUtil auditDetailsEnrichmentUtil;

    public TenantConfigEnricher(AuditDetailsEnrichmentUtil auditDetailsEnrichmentUtil) {
        this.auditDetailsEnrichmentUtil = auditDetailsEnrichmentUtil;
    }

    public void enrichCreateReq(TenantConfigRequest tenantConfigRequest) {

        // enrich code
//        tenantConfigRequest.getTenantConfig().setName(tenantConfigRequest.getTenantConfig().getCode());

        // enrich id
        UUIDEnrichmentUtil.enrichRandomUuid(tenantConfigRequest.getTenantConfig(), "id");

        // enrich audit details
        tenantConfigRequest.getTenantConfig().setAuditDetails(auditDetailsEnrichmentUtil.prepareAuditDetails(tenantConfigRequest.getTenantConfig().getAuditDetails(), tenantConfigRequest.getRequestInfo(), Boolean.TRUE));

        // enrich audit details for document
        if (tenantConfigRequest.getTenantConfig().getDocuments() != null &&
                !tenantConfigRequest.getTenantConfig().getDocuments().isEmpty()) {

            tenantConfigRequest.getTenantConfig().getDocuments().forEach(document -> {

                // enrich id
                UUIDEnrichmentUtil.enrichRandomUuid(document, "id");

                // enrich tenantConfigId
                document.setTenantConfigId(tenantConfigRequest.getTenantConfig().getId());

                // enrich audit details
                document.setAuditDetails(auditDetailsEnrichmentUtil.prepareAuditDetails(
                        document.getAuditDetails(), tenantConfigRequest.getRequestInfo(), Boolean.TRUE));
            });

        } else {
            // Handle the case where documents list is null or empty
            System.out.println("No documents to process.");
        }


    }

    public void enrichUpdateReq(TenantConfigRequest tenantConfigRequest) {

        TenantConfig tenant = tenantConfigRequest.getTenantConfig();
        tenant.getAuditDetails().setLastModifiedTime(System.currentTimeMillis());
        tenant.getAuditDetails().setLastModifiedBy(tenantConfigRequest.getRequestInfo().getUserInfo().getUuid());
//        tenant.setAuditDetails(AuditDetailsEnrichmentUtil.prepareAuditDetails(tenant.getAuditDetails(), tenantConfigRequest.getRequestInfo(), Boolean.FALSE));
    }
}