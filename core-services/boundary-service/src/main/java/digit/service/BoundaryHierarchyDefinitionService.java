package digit.service;

import digit.repository.BoundaryHierarchyRepository;
import digit.service.enrichment.BoundaryHierarchyEnricher;
import digit.service.validator.BoundaryHierarchyValidator;
import digit.util.HierarchyUtil;
import digit.web.models.*;
import org.egov.common.utils.ResponseInfoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

@Service
public class BoundaryHierarchyDefinitionService {

    private BoundaryHierarchyValidator boundaryHierarchyValidator;

    private BoundaryHierarchyEnricher boundaryHierarchyEnricher;

    private BoundaryHierarchyRepository boundaryHierarchyRepository;

    private HierarchyUtil hierarchyUtil;

    @Autowired
    public BoundaryHierarchyDefinitionService(BoundaryHierarchyValidator boundaryHierarchyValidator, BoundaryHierarchyEnricher boundaryHierarchyEnricher,
                                              BoundaryHierarchyRepository boundaryHierarchyRepository, HierarchyUtil hierarchyUtil) {
        this.boundaryHierarchyValidator = boundaryHierarchyValidator;
        this.boundaryHierarchyEnricher = boundaryHierarchyEnricher;
        this.boundaryHierarchyRepository = boundaryHierarchyRepository;
        this.hierarchyUtil = hierarchyUtil;
    }

    /**
     * Method for processing boundary hierarchy create requests.
     * @param body
     * @return
     */
    public BoundaryTypeHierarchyResponse createBoundaryHierarchyDefinition(BoundaryTypeHierarchyRequest body) {

        // Validate boundary hierarchy
        boundaryHierarchyValidator.validateBoundaryTypeHierarchy(body);

        // Enrich boundary hierarchy
        boundaryHierarchyEnricher.enrichBoundaryHierarchyDefinition(body);

        // Delegate request to boundary repository
        boundaryHierarchyRepository.create(body);

        // Build response and return
        return BoundaryTypeHierarchyResponse.builder()
                .boundaryHierarchy(Collections.singletonList(body.getBoundaryHierarchy()))
                .responseInfo(ResponseInfoUtil.createResponseInfoFromRequestInfo(body.getRequestInfo(), Boolean.TRUE))
                .build();
    }

    /**
     * Method for processing boundary hierarchy definition search requests.
     * @param body
     * @return
     */
    public BoundaryTypeHierarchyResponse searchBoundaryHierarchyDefinition(BoundaryTypeHierarchySearchRequest body) {

        // Search for boundary hierarchy depending on the provided search criteria
        List<BoundaryTypeHierarchyDefinition> boundaryTypeHierarchyDefinitionList = boundaryHierarchyRepository.search(body.getBoundaryTypeHierarchySearchCriteria());

        Integer totalCount = hierarchyUtil.getBoundaryTypeHierarchyDefinitionCount(body.getBoundaryTypeHierarchySearchCriteria());

        // Set boundary hierarchy definition as null if not found
        List<BoundaryTypeHierarchyDefinition> boundaryTypeHierarchyDefinition = CollectionUtils.isEmpty(boundaryTypeHierarchyDefinitionList) ? null : boundaryTypeHierarchyDefinitionList;

        // Build response and return
        return BoundaryTypeHierarchyResponse.builder()
                .boundaryHierarchy(boundaryTypeHierarchyDefinition)
                .responseInfo(ResponseInfoUtil.createResponseInfoFromRequestInfo(body.getRequestInfo(), Boolean.TRUE))
                .totalCount(totalCount)
                .build();
    }

}
