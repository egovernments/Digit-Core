package digit.service;

import digit.repository.BoundaryRelationshipRepository;
import digit.repository.impl.BoundaryHierarchyRepositoryImpl;
import digit.repository.impl.BoundaryRepositoryImpl;
import digit.service.enrichment.BoundaryHierarchyEnricher;
import digit.web.models.*;
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

    private BoundaryHierarchyRepositoryImpl boundaryHierarchyRepository;

    private BoundaryHierarchyEnricher boundaryHierarchyEnricher;

    private BoundaryRepositoryImpl boundaryRepository;

    private BoundaryRelationshipRepository boundaryRelationshipRepository;

    private BoundaryRelationshipService boundaryRelationshipService;

    public BoundaryMigrate(BoundaryHierarchyRepositoryImpl boundaryHierarchyRepository, BoundaryHierarchyEnricher boundaryHierarchyEnricher, BoundaryRepositoryImpl boundaryRepository, BoundaryRelationshipRepository boundaryRelationshipRepository, BoundaryRelationshipService boundaryRelationshipService) {
        this.boundaryHierarchyRepository = boundaryHierarchyRepository;
        this.boundaryHierarchyEnricher = boundaryHierarchyEnricher;
        this.boundaryRepository = boundaryRepository;
        this.boundaryRelationshipRepository = boundaryRelationshipRepository;
        this.boundaryRelationshipService = boundaryRelationshipService;
    }

    public void migrate(BoundaryMigrateRequest body) {

        // create boundary hierarchy definition
        body.getTenantBoundary().forEach(tenantBoundary -> {

            // create boundaries
            createBoundaryEntity(body.getTenantId() , tenantBoundary , body.getRequestInfo());

            // create hierarchy definition
            createBoundaryHierarchyDefinition(body.getTenantId() , tenantBoundary , body.getRequestInfo());

            // create boundary relationships
            try {
                createBoundaryRelationships(tenantBoundary , body.getRequestInfo() , body.getTenantId() , null);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        });

    }

    public void createBoundaryRelationships(TenantBoundary tenantBoundary , RequestInfo requestInfo , String tenantId , String Parent) throws InterruptedException {

        parseBoundaryRelationships(tenantBoundary.getBoundary() , requestInfo , tenantId , Parent , tenantBoundary.getHierarchyType().getCode());

    }

    public void parseBoundaryRelationships(Boundary boundary , RequestInfo requestInfo , String tenantId , String Parent , String hierarchyType) throws InterruptedException {

        BoundaryRelation boundaryRelation = BoundaryRelation.builder()
                .tenantId(tenantId)
                .code(boundary.getCode())
                .parent(Parent)
                .boundaryType(boundary.getLabel())
                .hierarchyType(hierarchyType)
                .build();

        BoundaryRelationshipRequest boundaryRelationshipRequest = BoundaryRelationshipRequest.builder()
                .boundaryRelationship(boundaryRelation)
                .requestInfo(requestInfo)
                .build();

        // create this relationship
        // 1. wait for some time
        Thread.sleep(1000);
        boundaryRelationshipService.createBoundaryRelationship(boundaryRelationshipRequest);

        for(int i=0;i<boundary.getChildren().size();i++) {
            if( boundary.getChildren().get(i) != null ) {
                parseBoundaryRelationships(boundary.getChildren().get(i) , requestInfo , tenantId , boundary.getCode() , hierarchyType);
            }
        }

    }

    public void createBoundaryEntity(String tenantId , TenantBoundary tenantBoundary , RequestInfo requestInfo ) {

        List<digit.web.models.Boundary> boundaryList = new ArrayList<>();

        parseBoundary(tenantBoundary.getBoundary() , tenantId , requestInfo, boundaryList);

        int batchSize = 300;
        List<BoundaryRequest> batchedRequests = new ArrayList<>();

        for (int i = 0; i < boundaryList.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, boundaryList.size());
            List<digit.web.models.Boundary> subList = boundaryList.subList(i, endIndex);

            BoundaryRequest batchedBoundaryRequest = BoundaryRequest.builder()
                    .requestInfo(requestInfo)
                    .boundary(subList)
                    .build();

            batchedRequests.add(batchedBoundaryRequest);
        }

        // Now, you can iterate through batchedRequests and save each batch to the repository
        for (BoundaryRequest batchedRequest : batchedRequests) {
            boundaryRepository.create(batchedRequest);
        }

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

