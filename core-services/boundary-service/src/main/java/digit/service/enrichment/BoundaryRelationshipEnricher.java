package digit.service.enrichment;

import digit.util.HierarchyUtil;
import digit.web.models.*;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.utils.AuditDetailsEnrichmentUtil;
import org.egov.common.utils.ResponseInfoUtil;
import org.egov.common.utils.UUIDEnrichmentUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class BoundaryRelationshipEnricher {

    private HierarchyUtil hierarchyUtil;

    public BoundaryRelationshipEnricher(HierarchyUtil hierarchyUtil) {
        this.hierarchyUtil = hierarchyUtil;
    }

    /**
     * Request handler for enriching boundary relationship request for id, auditDetails and ancestralMaterializedPath
     * @param body
     * @param ancestralMaterializedPath
     */
    public void enrichBoundaryRelationshipCreateRequest(BoundaryRelationshipRequest body, String ancestralMaterializedPath) {
        // Enrich uuid
        UUIDEnrichmentUtil.enrichRandomUuid(body.getBoundaryRelationship(), "id");

        // Enrich auditDetails
        body.getBoundaryRelationship().setAuditDetails(AuditDetailsEnrichmentUtil.prepareAuditDetails(body.getBoundaryRelationship().getAuditDetails(),
                body.getRequestInfo(),
                Boolean.TRUE));

        // Enrich ancestral materialized path
        enrichAncestralMaterializedPath(body.getBoundaryRelationship(), ancestralMaterializedPath);
    }

    /**
     * Method for creating and setting ancestralMaterializedPath.
     * @param boundaryRelationship
     * @param ancestralMaterializedPath
     */
    private void enrichAncestralMaterializedPath(BoundaryRelation boundaryRelationship, String ancestralMaterializedPath) {
        // Enrich ancestral materialized path if current node is non-parent node
        if(!ObjectUtils.isEmpty(boundaryRelationship.getParent())) {
            if(ObjectUtils.isEmpty(ancestralMaterializedPath)) {
                boundaryRelationship.setAncestralMaterializedPath(boundaryRelationship.getParent());
            } else {
                boundaryRelationship.setAncestralMaterializedPath(ancestralMaterializedPath + "|" + boundaryRelationship.getParent());
            }
        }
    }

    /**
     * Enrich root node search flag based on whether the search is for tenantId and hierarchyType.
     * @param boundaryRelationshipSearchCriteria
     */
    public void enrichSearchCriteria(BoundaryRelationshipSearchCriteria boundaryRelationshipSearchCriteria) {
        if(!ObjectUtils.isEmpty(boundaryRelationshipSearchCriteria.getTenantId())
                && !ObjectUtils.isEmpty(boundaryRelationshipSearchCriteria.getHierarchyType())
                && (ObjectUtils.isEmpty(boundaryRelationshipSearchCriteria.getBoundaryType())
                && CollectionUtils.isEmpty(boundaryRelationshipSearchCriteria.getCodes()))) {
            // Set flag for parent node search
            boundaryRelationshipSearchCriteria.setIsSearchForRootNode(Boolean.TRUE);
        }
    }

    /**
     * Method to create boundary relationship search response recursively from list of boundary relationships.
     * @param boundaryRelationships
     * @param tenantId
     * @param hierarchyType
     * @return boundarySearchResponse
     */
    public BoundarySearchResponse createBoundaryRelationshipSearchResponse(List<BoundaryRelationshipDTO> boundaryRelationships, String tenantId, String hierarchyType, RequestInfo requestInfo) {

        // Get hierarchy order
        List<String> hierarchyOrder = hierarchyUtil.getHierarchyOrder(tenantId, hierarchyType);

        // Convert DTO to EnrichedBoundary POJOs
        List<EnrichedBoundary> enrichedBoundaryList = convertBoundaryRelationshipToResponsePOJO(boundaryRelationships);

        // Create map of boundary type vs enriched boundaries
        Map<String, List<EnrichedBoundary>> boundaryTypeVsEnrichedBoundaries = enrichedBoundaryList.stream()
                .collect(Collectors.groupingBy(EnrichedBoundary::getBoundaryType));

        // Create map of parent vs children enriched boundaries
        Map<String, List<EnrichedBoundary>> parentVsChildrenEnrichedBoundaries = enrichedBoundaryList.stream()
                .filter(boundaryRelationship -> Objects.nonNull(boundaryRelationship.getParent()))
                .collect(Collectors.groupingBy(EnrichedBoundary::getParent));

        // Get seed boundaries based on hierarchy order
        List<EnrichedBoundary> seedResponseBoundaries = getSeedBoundaryList(boundaryTypeVsEnrichedBoundaries, hierarchyOrder);

        // Create nested boundary structure recursively
        mergeBoundariesRecursively(seedResponseBoundaries, parentVsChildrenEnrichedBoundaries);

        // Create HierarchyRelation POJO
        HierarchyRelation hierarchyRelation = HierarchyRelation.builder()
                .tenantId(tenantId)
                .hierarchyType(hierarchyType)
                .boundary(seedResponseBoundaries)
                .build();

        // Return response
        return BoundarySearchResponse.builder()
                .responseInfo(ResponseInfoUtil.createResponseInfoFromRequestInfo(requestInfo, Boolean.TRUE))
                .tenantBoundary(Collections.singletonList(hierarchyRelation))
                .build();
    }

    /**
     * Method to recursive merge list of boundaries to form hierarchical boundary response.
     * @param seedResponseBoundaries
     * @param parentVsChildrenEnrichedBoundaries
     */
    private void mergeBoundariesRecursively(List<EnrichedBoundary> seedResponseBoundaries, Map<String, List<EnrichedBoundary>> parentVsChildrenEnrichedBoundaries) {
        // Base case
        if(CollectionUtils.isEmpty(seedResponseBoundaries))
            return;

        // Traverse boundaries and add children to each boundary
        seedResponseBoundaries.forEach(parentBoundary -> {
            parentBoundary.setChildren(new ArrayList<>());

            if(!CollectionUtils.isEmpty(parentVsChildrenEnrichedBoundaries.get(parentBoundary.getCode()))) {
                parentBoundary.getChildren().addAll(parentVsChildrenEnrichedBoundaries.get(parentBoundary.getCode()));
                mergeBoundariesRecursively(parentBoundary.getChildren(), parentVsChildrenEnrichedBoundaries);
            }
        });

    }

    /**
     * This method gets the boundaries based on hierarchy order, returning the list
     * of boundaries belonging to the first boundary hierarchy type that it finds.
     * @param boundaryTypeVsEnrichedBoundaries
     * @param hierarchyOrder
     * @return
     */
    private List<EnrichedBoundary> getSeedBoundaryList(Map<String, List<EnrichedBoundary>> boundaryTypeVsEnrichedBoundaries, List<String> hierarchyOrder) {
        List<EnrichedBoundary> seedBoundaryList = new ArrayList<>();

        for(String boundaryType : hierarchyOrder) {
            if(boundaryTypeVsEnrichedBoundaries.containsKey(boundaryType)) {
                seedBoundaryList = boundaryTypeVsEnrichedBoundaries.get(boundaryType);
                break;
            }
        }

        return seedBoundaryList;
    }

    /**
     * This method converts list of boundary relationship DTOs into response POJO i.e. EnrichedBoundary.
     * @param boundaryRelationships
     * @return
     */
    private List<EnrichedBoundary> convertBoundaryRelationshipToResponsePOJO(List<BoundaryRelationshipDTO> boundaryRelationships) {
        List<EnrichedBoundary> enrichedBoundaryList = new ArrayList<>();

        boundaryRelationships.forEach(boundaryRelationshipDTO -> {
            enrichedBoundaryList.add(EnrichedBoundary.builder()
                    .id(boundaryRelationshipDTO.getId())
                    .boundaryType(boundaryRelationshipDTO.getBoundaryType())
                    .code(boundaryRelationshipDTO.getCode())
                    .parent(boundaryRelationshipDTO.getParent())
                    .children(new ArrayList<>())
                    .build());
        });

        return enrichedBoundaryList;
    }

    /**
     * This method enriches boundary relationship update request and returns back old parent
     * of the boundary relationship being updated.
     * @param body
     * @param validatedBoundaryRelationshipDTOFromDB
     * @return
     */
    public String enrichBoundaryRelationshipUpdateRequest(BoundaryRelationshipRequest body, BoundaryRelationshipRequestDTO validatedBoundaryRelationshipDTOFromDB) {
        // Capture old parent code
        StringBuilder oldParentCode = new StringBuilder(validatedBoundaryRelationshipDTOFromDB
                .getBoundaryRelationshipDTO()
                .getParent());

        // Set parent for update
        validatedBoundaryRelationshipDTOFromDB.getBoundaryRelationshipDTO()
                .setParent(body.getBoundaryRelationship().getParent());

        // Enrich audit details for update
        validatedBoundaryRelationshipDTOFromDB.getBoundaryRelationshipDTO()
                .setAuditDetails(AuditDetailsEnrichmentUtil.prepareAuditDetails(validatedBoundaryRelationshipDTOFromDB
                        .getBoundaryRelationshipDTO().getAuditDetails(), body.getRequestInfo(), Boolean.FALSE));

        // Enrich id and audit details back into the incoming request
        body.getBoundaryRelationship().setId(validatedBoundaryRelationshipDTOFromDB.getBoundaryRelationshipDTO().getId());
        body.getBoundaryRelationship().setAuditDetails(validatedBoundaryRelationshipDTOFromDB.getBoundaryRelationshipDTO().getAuditDetails());

        return oldParentCode.toString();
    }
}
