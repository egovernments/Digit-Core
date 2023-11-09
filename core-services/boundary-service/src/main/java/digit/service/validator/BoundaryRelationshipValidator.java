package digit.service.validator;

import digit.repository.BoundaryRelationshipRepository;
import digit.repository.BoundaryRepository;
import digit.util.HierarchyUtil;
import digit.web.models.*;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Component
public class BoundaryRelationshipValidator {

    private BoundaryRelationshipRepository boundaryRelationshipRepository;

    private BoundaryRepository boundaryRepository;

    private HierarchyUtil hierarchyUtil;

    public BoundaryRelationshipValidator(BoundaryRelationshipRepository boundaryRelationshipRepository, BoundaryRepository boundaryRepository,
                                         HierarchyUtil hierarchyUtil) {
        this.boundaryRelationshipRepository = boundaryRelationshipRepository;
        this.boundaryRepository = boundaryRepository;
        this.hierarchyUtil = hierarchyUtil;
    }

    /**
     *
     * @param body
     * @return
     */
    public String validateBoundaryRelationshipCreateRequest(BoundaryRelationshipRequest body) {
        // Check if boundary entity exists
        validateIfBoundaryEntityExists(body);

        // Check for duplicates
        checkDuplicates(body);

        // Check if parent boundary entity exists and return its materialized path and boundary type
        GenericPair<String, String> parentAttributes = validateParentAndReturnAttributes(body);

        // Check if the relationship being created has proper hierarchy
        validateRelationshipForProperHierarchy(body, parentAttributes.getSecond());

        // Return ancestralMaterializedPath of parent
        return parentAttributes.getFirst();
    }

    /**
     *
     * @param body
     */
    public BoundaryRelationshipRequestDTO validateBoundaryRelationshipUpdateRequest(BoundaryRelationshipRequest body) {

        // Validate existence of boundary relationship being updated
        BoundaryRelationshipDTO boundaryRelationshipDTO = validateExistence(body);

        // Validate existence of parent and whether hierarchy is not disturbed
        validateParentAndHierarchy(boundaryRelationshipDTO, body.getBoundaryRelationship());

        // Return response
        return BoundaryRelationshipRequestDTO.builder()
                .boundaryRelationshipDTO(boundaryRelationshipDTO)
                .requestInfo(body.getRequestInfo())
                .build();
    }

    private void validateParentAndHierarchy(BoundaryRelationshipDTO boundaryRelationshipDTO, BoundaryRelation boundaryRelationship) {
        // Validate root node hierarchy in case of updation in root node
        if(ObjectUtils.isEmpty(boundaryRelationshipDTO.getParent()) && !ObjectUtils.isEmpty(boundaryRelationship.getParent())) {
            throw new CustomException("HIERARCHY_DISTURBED_ERR", "If a boundary relationship is created with root boundary type, it can't be made a child of any other boundary");
        }

        // Validate parent's existence and hierarchy
        if(!ObjectUtils.isEmpty(boundaryRelationship.getParent())) {
            List<BoundaryRelationshipDTO> boundaryRelationshipDTOList = boundaryRelationshipRepository.search(BoundaryRelationshipSearchCriteria.builder()
                    .tenantId(boundaryRelationship.getTenantId())
                    .codes(Collections.singletonList(boundaryRelationship.getParent()))
                    .build());

            if(CollectionUtils.isEmpty(boundaryRelationshipDTOList)) {
                throw new CustomException("BOUNDARY_RELATIONSHIP_DOES_NOT_EXIST", "Parent boundary relationship provided in update request does not exist");
            }

            if(!Objects.equals(boundaryRelationshipDTO.getBoundaryType(), boundaryRelationship.getBoundaryType())) {
                throw new CustomException("HIERARCHY_DISTURBED_ERR", "Parent updates are only allowed horizontally.");
            }

        }

    }

    /**
     *
     * @param body
     */
    private BoundaryRelationshipDTO validateExistence(BoundaryRelationshipRequest body) {
        List<BoundaryRelationshipDTO> boundaryRelationshipDTOList = boundaryRelationshipRepository.search(BoundaryRelationshipSearchCriteria.builder()
                .tenantId(body.getBoundaryRelationship().getTenantId())
                .hierarchyType(body.getBoundaryRelationship().getHierarchyType())
                .codes(Collections.singletonList(body.getBoundaryRelationship().getCode()))
                .build());

        if(CollectionUtils.isEmpty(boundaryRelationshipDTOList)) {
            throw new CustomException("BOUNDARY_RELATIONSHIP_DOES_NOT_EXIST", "Provided boundary relationship for update does not exist");
        }

        return boundaryRelationshipDTOList.get(0);
    }

    private void checkDuplicates(BoundaryRelationshipRequest body) {
        List<BoundaryRelationshipDTO> boundaryRelationshipDTOList = boundaryRelationshipRepository.search(BoundaryRelationshipSearchCriteria.builder()
                .tenantId(body.getBoundaryRelationship().getTenantId())
                .hierarchyType(body.getBoundaryRelationship().getHierarchyType())
                .codes(Collections.singletonList(body.getBoundaryRelationship().getCode()))
                .build());

        if(!CollectionUtils.isEmpty(boundaryRelationshipDTOList)) {
            throw new CustomException("DUPLICATE_RECORD", "Provided boundary relationship already exists");
        }
    }

    private GenericPair<String, String> validateParentAndReturnAttributes(BoundaryRelationshipRequest body) {
        String ancestralMaterializedPath = "";
        String boundaryType = body.getBoundaryRelationship().getBoundaryType();

        if(!ObjectUtils.isEmpty(body.getBoundaryRelationship().getParent())) {
            List<BoundaryRelationshipDTO> resultSet = boundaryRelationshipRepository.search(BoundaryRelationshipSearchCriteria.builder()
                    .tenantId(body.getBoundaryRelationship().getTenantId())
                    .hierarchyType(body.getBoundaryRelationship().getHierarchyType())
                    .codes(Collections.singletonList(body.getBoundaryRelationship().getParent()))
                    .build());

            if(CollectionUtils.isEmpty(resultSet)) {
                throw new CustomException("PARENT_NOT_FOUND", "Parent entity for current boundary relationship does not exist.");
            } else {
                ancestralMaterializedPath = resultSet.get(0).getAncestralMaterializedPath();
                boundaryType = resultSet.get(0).getBoundaryType();
            }
        }

        GenericPair<String, String> ancestralMaterializedPathAndBoundaryTypePair = GenericPair.<String, String>builder()
                .first(ancestralMaterializedPath)
                .second(boundaryType)
                .build();

        return ancestralMaterializedPathAndBoundaryTypePair;
    }

    private void validateRelationshipForProperHierarchy(BoundaryRelationshipRequest body, String parentBoundaryType) {
        List<String> hierarchyOrder = hierarchyUtil.getHierarchyOrder(body.getBoundaryRelationship().getTenantId(),
                body.getBoundaryRelationship().getHierarchyType());

        if(!hierarchyOrder.contains(body.getBoundaryRelationship().getBoundaryType())) {
            throw new CustomException("BOUNDARY_TYPE_ERROR", "The provided boundary type is not a part of provided hierarchy definition.");
        }

        if(ObjectUtils.isEmpty(body.getBoundaryRelationship().getParent())) {
            if(!Objects.equals(body.getBoundaryRelationship().getBoundaryType(), hierarchyOrder.get(0))) {
                throw new CustomException("HIERARCHY_ERROR", "Boundary relationship without defined parent should have root boundary hierarchy type.");
            }
        } else{
            if(!body.getBoundaryRelationship().getBoundaryType().equals(hierarchyOrder.get(hierarchyOrder.indexOf(parentBoundaryType) + 1))) {
                throw new CustomException("HIERARCHY_ERROR", "Hierarchy of child should be the direct descendant of parent's boundary hierarchy type.");
            }
        }
    }

    private void validateIfBoundaryEntityExists(BoundaryRelationshipRequest body) {
        List<Boundary> boundaryList = boundaryRepository.search(BoundarySearchCriteria.builder()
                .tenantId(body.getBoundaryRelationship().getTenantId())
                .codes(Collections.singletonList(body.getBoundaryRelationship().getCode()))
                .build());

        if(CollectionUtils.isEmpty(boundaryList)) {
            throw new CustomException("BOUNDARY_ENTITY_DOES_NOT_EXIST", "Boundary entity does not exist.");
        }
    }

}
