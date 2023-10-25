package digit.service.enrichment;

import digit.util.JsonUtil;
import digit.web.models.BoundaryTypeHierarchyRequest;
import org.egov.common.utils.AuditDetailsEnrichmentUtil;
import org.egov.common.utils.UUIDEnrichmentUtil;
import org.springframework.stereotype.Component;

@Component
public class BoundaryHierarchyEnricher {

    private JsonUtil jsonUtil;

    public BoundaryHierarchyEnricher(JsonUtil jsonUtil) {
        this.jsonUtil = jsonUtil;
    }

    public void enrichBoundaryHierarchyDefinition(BoundaryTypeHierarchyRequest body) {
        UUIDEnrichmentUtil.enrichRandomUuid(body.getBoundaryHierarchy(), "id");
        body.getBoundaryHierarchy().setAuditDetails(AuditDetailsEnrichmentUtil.prepareAuditDetails(body.getBoundaryHierarchy().getAuditDetails(), body.getRequestInfo(), Boolean.TRUE));
        body.getBoundaryHierarchy().setBoundaryHierarchyJsonNode(jsonUtil.getBoundaryHierarchyJsonNode(body.getBoundaryHierarchy().getBoundaryHierarchy()));
    }

}
