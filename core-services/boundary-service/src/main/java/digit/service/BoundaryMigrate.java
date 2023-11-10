package digit.service;

import digit.repository.impl.BoundaryHierarchyRepositoryImpl;
import digit.service.enrichment.BoundaryHierarchyEnricher;
import digit.web.models.BoundaryTypeHierarchy;
import digit.web.models.BoundaryTypeHierarchyDefinition;
import digit.web.models.BoundaryTypeHierarchyRequest;
import digit.web.models.legacy.Boundary;
import digit.web.models.legacy.BoundaryMigrateRequest;
import digit.web.models.legacy.TenantBoundary;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.utils.AuditDetailsEnrichmentUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BoundaryMigrate {

    private final BoundaryHierarchyRepositoryImpl boundaryHierarchyRepository;
    private final BoundaryHierarchyEnricher boundaryHierarchyEnricher;

    public BoundaryMigrate(BoundaryHierarchyRepositoryImpl boundaryHierarchyRepository, BoundaryHierarchyEnricher boundaryHierarchyEnricher) {
        this.boundaryHierarchyRepository = boundaryHierarchyRepository;
        this.boundaryHierarchyEnricher = boundaryHierarchyEnricher;
    }

    public void migrate(BoundaryMigrateRequest body) {

        // create boundary hierarchy definition
        body.getTenantBoundary().forEach(tenantBoundary -> {
           createBoundaryHierarchyDefinition(body.getTenantId() , tenantBoundary , body.getRequestInfo());
        });

        // create boundaries
        System.out.println("Migrating boundaries");
    }

    public void createBoundaryHierarchyDefinition(String tenantId , TenantBoundary tenantBoundary , RequestInfo requestInfo) {

        List<BoundaryTypeHierarchy> boundaryHierarchy = parseBoundaryHierarchy(tenantBoundary.getBoundary(),null);

        BoundaryTypeHierarchyDefinition boundaryTypeHierarchyDefinition = BoundaryTypeHierarchyDefinition.builder()
                .tenantId(tenantId)
                .hierarchyType(tenantBoundary.getHierarchyType().getCode())
                .boundaryHierarchy(boundaryHierarchy)
                .auditDetails(AuditDetailsEnrichmentUtil.prepareAuditDetails(new AuditDetails(),requestInfo,Boolean.TRUE))
                .build();

        BoundaryTypeHierarchyRequest boundaryTypeHierarchyRequest = BoundaryTypeHierarchyRequest.builder()
                .boundaryHierarchy(boundaryTypeHierarchyDefinition)
                .requestInfo(requestInfo)
                .build();

        boundaryHierarchyEnricher.enrichBoundaryHierarchyDefinition(boundaryTypeHierarchyRequest);

        boundaryHierarchyRepository.create(boundaryTypeHierarchyRequest);
    }

    public List<BoundaryTypeHierarchy> parseBoundaryHierarchy(Boundary boundary, String parentBoundaryType) {

        List<BoundaryTypeHierarchy> boundaryTypeHierarchyList = new ArrayList<>();
        BoundaryTypeHierarchy boundaryTypeHierarchy = BoundaryTypeHierarchy.builder()
                .boundaryType(boundary.getLabel())
                .parentBoundaryType(parentBoundaryType)
                .active(Boolean.TRUE)
                .build();

        if(boundary.getChildren() == null) {
            boundaryTypeHierarchyList.add(boundaryTypeHierarchy);
            return boundaryTypeHierarchyList;
        }

        for(int i=0;i<boundary.getChildren().size();i++) {

            if( boundary.getChildren().get(i) != null ) {
                boundaryTypeHierarchyList = parseBoundaryHierarchy(boundary.getChildren().get(i),boundary.getLabel());
                break;
            }
        }

        boundaryTypeHierarchyList.add(boundaryTypeHierarchy);
        return boundaryTypeHierarchyList;
    }
}
