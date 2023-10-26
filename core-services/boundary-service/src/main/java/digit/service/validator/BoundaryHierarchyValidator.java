package digit.service.validator;

import digit.repository.BoundaryHierarchyRepository;
import digit.web.models.BoundaryTypeHierarchyRequest;
import digit.web.models.BoundaryTypeHierarchySearchCriteria;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class BoundaryHierarchyValidator {

    private BoundaryHierarchyRepository boundaryHierarchyRepository;

    @Autowired
    public BoundaryHierarchyValidator(BoundaryHierarchyRepository boundaryHierarchyRepository) {
        this.boundaryHierarchyRepository = boundaryHierarchyRepository;
    }

    /**
     * Parent method for handling boundary hierarchy request validation.
     * @param body
     */
    public void validateBoundaryTypeHierarchy(BoundaryTypeHierarchyRequest body) {

        // Validate if provided boundary hierarchy forms a directed acyclic graph dependency
        validateIfBoundaryHierarchyFormsDAG(body);

        // Validate if provided boundary hierarchy already exists
        validateIfBoundaryHierarchyAlreadyExists(body);

    }

    /**
     * This method receives boundary type hierarchy request and ensures that the
     * provided hierarchy definition forms a directed acyclic dependency graph.
     * @param body
     */
    private void validateIfBoundaryHierarchyFormsDAG(BoundaryTypeHierarchyRequest body) {

        Map<String, String> parentToChildMap = new LinkedHashMap<>();

        // Populate parent boundaries
        body.getBoundaryHierarchy().getBoundaryHierarchy().forEach(boundaryTypeHierarchy -> {
            parentToChildMap.put(boundaryTypeHierarchy.getBoundaryType(), null);
        });

        // Check if the the hierarchy definition forms a directed acyclic graph
        body.getBoundaryHierarchy().getBoundaryHierarchy().forEach(boundaryTypeHierarchy -> {
            if(!ObjectUtils.isEmpty(boundaryTypeHierarchy.getParentBoundaryType())) {

                if(!parentToChildMap.containsKey(boundaryTypeHierarchy.getParentBoundaryType())) {
                    throw new CustomException("INVALID_HIERARCHY_DEFINITION", "Given parent type is not part of boundary hierarchy definition - " + boundaryTypeHierarchy.getParentBoundaryType());
                }

                if(!ObjectUtils.isEmpty(parentToChildMap.get(boundaryTypeHierarchy.getParentBoundaryType()))) {
                    throw new CustomException("INVALID_HIERARCHY_DEFINITION", "Hierarchy entities must not form a cycle.");
                }

                parentToChildMap.put(boundaryTypeHierarchy.getParentBoundaryType(), boundaryTypeHierarchy.getBoundaryType());
            }
        });
    }

    /**
     * This method validates if the provided boundary hierarchy is already created or not.
     * @param body
     */
    private void validateIfBoundaryHierarchyAlreadyExists(BoundaryTypeHierarchyRequest body) {
        // Prepare boundary type hierarchy search criteria
        BoundaryTypeHierarchySearchCriteria boundaryTypeHierarchySearchCriteria = BoundaryTypeHierarchySearchCriteria
                .builder()
                .tenantId(body.getBoundaryHierarchy().getTenantId())
                .hierarchyType(body.getBoundaryHierarchy().getHierarchyType())
                .build();

        // Check if boundary type with the provided tenantId and hierarchy type already exists
        if(!CollectionUtils.isEmpty(boundaryHierarchyRepository.search(boundaryTypeHierarchySearchCriteria))) {
            throw new CustomException("DUPLICATE_RECORD", "Boundary hierarchy with the provided tenantId and hierarchy type already exists.");
        }
    }

}
