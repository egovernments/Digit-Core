package digit.service;

import digit.repository.BoundaryRelationshipRepository;
import digit.service.enrichment.BoundaryRelationshipEnricher;
import digit.service.validator.BoundaryRelationshipValidator;
import digit.util.HierarchyUtil;
import digit.web.models.*;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.utils.ResponseInfoUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BoundaryRelationshipService {

    private BoundaryRelationshipValidator boundaryRelationshipValidator;

    private BoundaryRelationshipEnricher boundaryRelationshipEnricher;

    private BoundaryRelationshipRepository boundaryRelationshipRepository;

    private HierarchyUtil hierarchyUtil;

    public BoundaryRelationshipService(BoundaryRelationshipValidator boundaryRelationshipValidator, BoundaryRelationshipEnricher boundaryRelationshipEnricher,
                                       BoundaryRelationshipRepository boundaryRelationshipRepository, HierarchyUtil hierarchyUtil) {
        this.boundaryRelationshipValidator = boundaryRelationshipValidator;
        this.boundaryRelationshipEnricher = boundaryRelationshipEnricher;
        this.boundaryRelationshipRepository = boundaryRelationshipRepository;
        this.hierarchyUtil = hierarchyUtil;
    }

    /**
     * Request handler for processing boundary relationship create requests.
     * @param body
     * @return
     */
    public BoundaryRelationshipResponse createBoundaryRelationship(BoundaryRelationshipRequest body) {

        // Validate boundary relationship and get ancestral materialized path if successfully validated
        String ancestralMaterializedPath = boundaryRelationshipValidator.validateBoundaryRelationshipCreateRequest(body);

        // Enrich boundary relationship
        boundaryRelationshipEnricher.enrichBoundaryRelationshipCreateRequest(body, ancestralMaterializedPath);

        // Delegate request to repository
        boundaryRelationshipRepository.create(body);

        // Create boundary relationship response and return
        return BoundaryRelationshipResponse.builder()
                .responseInfo(ResponseInfoUtil.createResponseInfoFromRequestInfo(body.getRequestInfo(), Boolean.TRUE))
                .tenantBoundary(Collections.singletonList(body.getBoundaryRelationship()))
                .build();

    }

    /**
     * Request handler for processing boundary relationship search requests.
     * @param boundaryRelationshipSearchCriteria
     * @return
     */
    public BoundarySearchResponse getBoundaryRelationships(BoundaryRelationshipSearchCriteria boundaryRelationshipSearchCriteria, RequestInfo requestInfo) {

        // Enrich search criteria
        boundaryRelationshipEnricher.enrichSearchCriteria(boundaryRelationshipSearchCriteria);

        // Get list of boundary relationships based on provided search criteria
        List<BoundaryRelationshipDTO> boundaries = boundaryRelationshipRepository.search(boundaryRelationshipSearchCriteria);

        // Get parent boundaries if includeParents flag is checked
        List<BoundaryRelationshipDTO> parentBoundaries = getParentBoundaries(boundaries, boundaryRelationshipSearchCriteria);

        // Get children boundaries if includeChildren flag is checked
        List<BoundaryRelationshipDTO> childrenBoundaries = getChildrenBoundaries(boundaries, boundaryRelationshipSearchCriteria);

        // Add parents and children boundaries to main boundary search list
        addParentsAndChildrenToBoundariesList(boundaries, parentBoundaries, childrenBoundaries);

        // Prepare search response for boundary search
        BoundarySearchResponse boundarySearchResponse = boundaryRelationshipEnricher.createBoundaryRelationshipSearchResponse(boundaries, boundaryRelationshipSearchCriteria.getTenantId(), boundaryRelationshipSearchCriteria.getHierarchyType(), requestInfo);

        // Return boundary search response
        return boundarySearchResponse;
    }

    /**
     * Service method to fetch children boundary DTOs.
     * @param boundaries
     * @param boundaryRelationshipSearchCriteria
     * @return
     */
    private List<BoundaryRelationshipDTO> getChildrenBoundaries(List<BoundaryRelationshipDTO> boundaries, BoundaryRelationshipSearchCriteria boundaryRelationshipSearchCriteria) {
        List<BoundaryRelationshipDTO> childrenBoundaries = new ArrayList<>();

        // Fetch children boundary DTOs if includeChildren flag is set to true.
        if (!CollectionUtils.isEmpty(boundaries) && boundaryRelationshipSearchCriteria.getIncludeChildren()) {
            List<String> currentBoundaryCodes = boundaries.stream()
                    .map(BoundaryRelationshipDTO::getCode)
                    .collect(Collectors.toList());

            childrenBoundaries = boundaryRelationshipRepository.search(BoundaryRelationshipSearchCriteria.builder()
                    .tenantId(boundaryRelationshipSearchCriteria.getTenantId())
                    .hierarchyType(boundaryRelationshipSearchCriteria.getHierarchyType())
                    .currentBoundaryCodes(currentBoundaryCodes)
                    .build());
        }

        return childrenBoundaries;
    }

    /**
     * Service method to fetch parent boundary DTOs.
     * @param boundaries
     * @param boundaryRelationshipSearchCriteria
     * @return
     */
    private List<BoundaryRelationshipDTO> getParentBoundaries(List<BoundaryRelationshipDTO> boundaries, BoundaryRelationshipSearchCriteria boundaryRelationshipSearchCriteria) {
        List<BoundaryRelationshipDTO> parentBoundaries = new ArrayList<>();

        // Fetch parent boundaries if includeParents flag is true.
        if (!CollectionUtils.isEmpty(boundaries) && boundaryRelationshipSearchCriteria.getIncludeParents()) {
            Set<String> allAncestorCodes = boundaries.stream()
                    .map(dto -> dto.getAncestralMaterializedPath().split("\\|"))
                    .flatMap(Arrays::stream)
                    .collect(Collectors.toSet());

            parentBoundaries = boundaryRelationshipRepository.search(BoundaryRelationshipSearchCriteria.builder()
                    .tenantId(boundaryRelationshipSearchCriteria.getTenantId())
                    .hierarchyType(boundaryRelationshipSearchCriteria.getHierarchyType())
                    .codes(new ArrayList<>(allAncestorCodes))
                    .build());
        }

        return parentBoundaries;
    }

    /**
     * Request handler for processing boundary relationship update requests.
     * @param body
     * @return
     */
    public BoundaryRelationshipResponse updateBoundaryRelationship(BoundaryRelationshipRequest body) {

        // Validate update request
        BoundaryRelationshipRequestDTO validatedRelationshipDTORequest = boundaryRelationshipValidator.validateBoundaryRelationshipUpdateRequest(body);

        // Enrich update request
        String oldParentCode = boundaryRelationshipEnricher.enrichBoundaryRelationshipUpdateRequest(body, validatedRelationshipDTORequest);

        // Fetch children boundaries
        List<BoundaryRelationshipDTO> childrenBoundaryRelationships = getChildrenBoundaries(Collections
                .singletonList(validatedRelationshipDTORequest.getBoundaryRelationshipDTO()), BoundaryRelationshipSearchCriteria.builder()
                .tenantId(validatedRelationshipDTORequest.getBoundaryRelationshipDTO().getTenantId())
                .hierarchyType(validatedRelationshipDTORequest.getBoundaryRelationshipDTO().getHierarchyType())
                .includeChildren(Boolean.TRUE)
                .build());

        // Update ancestral materialized path of children boundary relationships
        preProcessNodesForUpdate(validatedRelationshipDTORequest, childrenBoundaryRelationships, oldParentCode);

        // Delegate request to repository
        boundaryRelationshipRepository.update(validatedRelationshipDTORequest);

        // Return response
        return BoundaryRelationshipResponse.builder()
                .responseInfo(ResponseInfoUtil.createResponseInfoFromRequestInfo(body.getRequestInfo(), Boolean.TRUE))
                .tenantBoundary(Collections.singletonList(body.getBoundaryRelationship()))
                .build();
    }

    /**
     * This method updates ancestral materialized path in the node being updated along with its
     * children nodes.
     * @param validatedRelationshipDTORequest
     * @param childrenBoundaryRelationships
     * @param oldParentCode
     */
    private void preProcessNodesForUpdate(BoundaryRelationshipRequestDTO validatedRelationshipDTORequest, List<BoundaryRelationshipDTO> childrenBoundaryRelationships, String oldParentCode) {
        // Add children boundary relationships to the list of nodes to be updated
        List<BoundaryRelationshipDTO> allNodesToBeUpdated = new ArrayList<>(childrenBoundaryRelationships);

        // Add the concerned boundary relationship which is being updated
        allNodesToBeUpdated.add(validatedRelationshipDTORequest.getBoundaryRelationshipDTO());

        // For each node, update ancestral materialized path - replace old parent code with new parent code
        allNodesToBeUpdated.forEach(boundaryRelationship -> {
            boundaryRelationship.setAncestralMaterializedPath(boundaryRelationship.getAncestralMaterializedPath()
                    .replace(oldParentCode,
                            validatedRelationshipDTORequest.getBoundaryRelationshipDTO().getParent()));
        });

        // Set list of nodes to be updated
        validatedRelationshipDTORequest.setBoundaryRelationshipDTOList(allNodesToBeUpdated);

    }

    /**
     * Add parent and children boundaries to searched boundaries list.
     * @param boundaries
     * @param parentBoundaries
     * @param childrenBoundaries
     */
    private void addParentsAndChildrenToBoundariesList(List<BoundaryRelationshipDTO> boundaries, List<BoundaryRelationshipDTO> parentBoundaries, List<BoundaryRelationshipDTO> childrenBoundaries) {
        boundaries.addAll(parentBoundaries);
        boundaries.addAll(childrenBoundaries);
    }

}
