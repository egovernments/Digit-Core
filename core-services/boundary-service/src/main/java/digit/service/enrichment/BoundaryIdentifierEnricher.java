package digit.service.enrichment;

import digit.web.models.BoundaryIdentifier;
import digit.web.models.BoundaryIdentifierRequest;
import org.egov.common.utils.AuditDetailsEnrichmentUtil;
import org.egov.common.utils.UUIDEnrichmentUtil;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BoundaryIdentifierEnricher {

    private BoundaryIdentifierEnricher() {}

    /**
     * Enrich the create boundary identifier request with id and audit details
     * @param request
     */
    public static void enrichCreateRequest(BoundaryIdentifierRequest request) {
        request.getBoundaryIdentifier().forEach(identifier -> {
            UUIDEnrichmentUtil.enrichRandomUuid(identifier, "id");
            identifier.setAuditDetails(AuditDetailsEnrichmentUtil.prepareAuditDetails(identifier.getAuditDetails(), request.getRequestInfo(), Boolean.TRUE));
        });
    }

    /**
     * Enrich the update boundary identifier request with id and created audit details of the
     * existing record (matched by tenantId + hierarchyType + code), and fresh last-modified audit details
     * @param request
     * @param existingRecords
     */
    public static void enrichUpdateRequest(BoundaryIdentifierRequest request, List<BoundaryIdentifier> existingRecords) {
        Map<List<String>, BoundaryIdentifier> existingByKey = existingRecords.stream()
                .collect(Collectors.toMap(BoundaryIdentifierEnricher::key, existing -> existing));

        request.getBoundaryIdentifier().forEach(identifier -> {
            BoundaryIdentifier existing = existingByKey.get(key(identifier));
            identifier.setId(existing.getId());
            identifier.setAuditDetails(AuditDetailsEnrichmentUtil.prepareAuditDetails(existing.getAuditDetails(), request.getRequestInfo(), Boolean.FALSE));
        });
    }

    private static List<String> key(BoundaryIdentifier identifier) {
        return List.of(identifier.getTenantId(), identifier.getHierarchyType(), identifier.getCode());
    }
}
