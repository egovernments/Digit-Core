package digit.util;

import digit.repository.BoundaryHierarchyRepository;
import digit.web.models.BoundaryTypeHierarchy;
import digit.web.models.BoundaryTypeHierarchyDefinition;
import digit.web.models.BoundaryTypeHierarchySearchCriteria;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Component
public class HierarchyUtil {

    private BoundaryHierarchyRepository boundaryHierarchyRepository;

    public HierarchyUtil(BoundaryHierarchyRepository boundaryHierarchyRepository) {
        this.boundaryHierarchyRepository = boundaryHierarchyRepository;
    }

    /**
     * This method gives the hierarchy order from hierarchy definition.
     * @param tenantId
     * @param hierarchyType
     * @return
     */
    public List<String> getHierarchyOrder(String tenantId, String hierarchyType) {
        List<BoundaryTypeHierarchyDefinition> boundaryTypeHierarchyDefinitionList = boundaryHierarchyRepository.search(BoundaryTypeHierarchySearchCriteria.builder()
                .tenantId(tenantId)
                .hierarchyType(hierarchyType)
                .build());

        if(CollectionUtils.isEmpty(boundaryTypeHierarchyDefinitionList)) {
            throw new CustomException("HIERARCHY_DEFINITION_DOES_NOT_EXIST_ERR", "Hierarchy definition does not exist");
        }

        List<BoundaryTypeHierarchy> boundaryTypeHierarchyList = boundaryTypeHierarchyDefinitionList.get(0).getBoundaryHierarchy();

        Map<String, String> parentToChildMap = prepareParentToChildMap(boundaryTypeHierarchyList);

        List<String> hierarchyOrder = new ArrayList<>();

        String rootHierarchyNode = boundaryTypeHierarchyList
                .stream()
                .filter(hierarchyNode -> ObjectUtils.isEmpty(hierarchyNode.getParentBoundaryType()))
                .findFirst()
                .get()
                .getBoundaryType();

        hierarchyOrder.add(rootHierarchyNode);

        IntStream.range(0, boundaryTypeHierarchyList.size() - 1).forEach(i -> {
            hierarchyOrder.add(parentToChildMap.get(hierarchyOrder.get(i)));
        });

        return hierarchyOrder;
    }

    private Map<String, String> prepareParentToChildMap(List<BoundaryTypeHierarchy> boundaryTypeHierarchyList) {
        Map<String, String> parentToChildMap = new HashMap<>();

        boundaryTypeHierarchyList.forEach(boundaryTypeHierarchy -> {
            if(!ObjectUtils.isEmpty(boundaryTypeHierarchy.getParentBoundaryType())) {
                parentToChildMap.put(boundaryTypeHierarchy.getParentBoundaryType(), boundaryTypeHierarchy.getBoundaryType());
            }
        });

        return parentToChildMap;
    }
}
