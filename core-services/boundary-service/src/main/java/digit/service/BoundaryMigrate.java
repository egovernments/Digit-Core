package digit.service;

import digit.repository.impl.BoundaryHierarchyRepositoryImpl;
import digit.repository.impl.BoundaryRepositoryImpl;
import digit.service.enrichment.BoundaryHierarchyEnricher;
import digit.web.models.BoundaryRequest;
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
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class BoundaryMigrate {

    private final BoundaryHierarchyRepositoryImpl boundaryHierarchyRepository;
    private final BoundaryHierarchyEnricher boundaryHierarchyEnricher;

    private final BoundaryRepositoryImpl boundaryRepository;

    public BoundaryMigrate(BoundaryHierarchyRepositoryImpl boundaryHierarchyRepository, BoundaryHierarchyEnricher boundaryHierarchyEnricher, BoundaryRepositoryImpl boundaryRepository) {
        this.boundaryHierarchyRepository = boundaryHierarchyRepository;
        this.boundaryHierarchyEnricher = boundaryHierarchyEnricher;
        this.boundaryRepository = boundaryRepository;
    }

    public void migrate(BoundaryMigrateRequest body) {

        // create boundary hierarchy definition
        body.getTenantBoundary().forEach(tenantBoundary -> {

            // create boundaries
            createBoundaryEntity(body.getTenantId() , tenantBoundary , body.getRequestInfo());

            // create hierarchy definition
            createBoundaryHierarchyDefinition(body.getTenantId() , tenantBoundary , body.getRequestInfo());
        });

    }

    public void createBoundaryEntity(String tenantId , TenantBoundary tenantBoundary , RequestInfo requestInfo ) {

        List<digit.web.models.Boundary> boundaryList = new ArrayList<>();

        parseBoundary(tenantBoundary.getBoundary() , tenantId , requestInfo, boundaryList);

        BoundaryRequest boundaryRequest = BoundaryRequest.builder()
                .requestInfo(requestInfo)
                .boundary(boundaryList)
                .build();

        boundaryRepository.create(boundaryRequest);

    }

    public void parseBoundary(Boundary boundary , String tenantId , RequestInfo requestInfo , List<digit.web.models.Boundary> boundaryList) {

        // Convert legacy boundary to new boundary
        digit.web.models.Boundary currBoundary =  digit.web.models.Boundary.builder()
                .tenantId(tenantId)
                .id(boundary.getId())
                .code(boundary.getCode())
                .auditDetails(AuditDetailsEnrichmentUtil.prepareAuditDetails(new AuditDetails(),requestInfo,Boolean.TRUE))
                .build();

        boundaryList.add(currBoundary);

        // if no children return the current boundaryList
        if(ObjectUtils.isEmpty(boundary.getChildren())) {
            return;
        }

        // if children are present parse them
        for(int i=0;i<boundary.getChildren().size();i++) {

            if( boundary.getChildren().get(i) != null ) {
                parseBoundary(boundary.getChildren().get(i) , tenantId , requestInfo, boundaryList);
            }
        }

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

        // collections util or object utils (Spring framework to check if collection is empty)
        if(boundary.getChildren() == null) {
            boundaryTypeHierarchyList.add(boundaryTypeHierarchy);
            return boundaryTypeHierarchyList;
        }

        for(int i=0;i<boundary.getChildren().size();i++) {
            if( boundary.getChildren().get(i) != null ) {
                boundaryTypeHierarchyList = parseBoundaryHierarchy(boundary.getChildren().get(i),boundary.getLabel());
            }
        }

        boundaryTypeHierarchyList.add(boundaryTypeHierarchy);
        return boundaryTypeHierarchyList;
    }
}
