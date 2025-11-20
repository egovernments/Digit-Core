package com.digit.services.boundary;

import com.digit.config.ApiProperties;
import com.digit.exception.DigitClientException;
import com.digit.services.boundary.model.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Service client for Boundary API operations.
 * Provides methods to interact with the Boundary service.
 * Based on actual API endpoints from Postman collection.
 */
@Slf4j
@Getter
@Setter
public class BoundaryClient {

    private final RestTemplate restTemplate;
    private final ApiProperties apiProperties;

    /**
     * Constructor for BoundaryClient.
     *
     * @param restTemplate the RestTemplate for HTTP operations
     * @param apiProperties the API configuration properties
     */
    public BoundaryClient(RestTemplate restTemplate, ApiProperties apiProperties) {
        this.restTemplate = restTemplate;
        this.apiProperties = apiProperties;

        // DEBUG: Check which RestTemplate we're using
        System.out.println("🔍 BoundaryClient created with RestTemplate: " + restTemplate.getClass().getSimpleName());
        System.out.println("🔍 RestTemplate interceptors: " + restTemplate.getInterceptors().size());
        restTemplate.getInterceptors().forEach(interceptor -> {
            System.out.println("  - " + interceptor.getClass().getSimpleName());
        });
    }

    /**
     * Creates new boundaries.
     *
     * @param boundaries the list of boundaries to create
     * @return the list of created Boundary objects
     * @throws DigitClientException if creation fails
     */
    public List<Boundary> createBoundaries(List<Boundary> boundaries) {
        if (boundaries == null || boundaries.isEmpty()) {
            throw new DigitClientException("Boundaries list cannot be null or empty");
        }

        try {
            log.debug("Creating {} boundaries", boundaries.size());
            String url = apiProperties.getBoundaryServiceUrl() + "/boundary/v1";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            
            BoundaryRequest request = BoundaryRequest.builder().boundary(boundaries).build();
            HttpEntity<BoundaryRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<BoundaryResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, BoundaryResponse.class);
            
            List<Boundary> createdBoundaries = response.getBody() != null ? response.getBody().getBoundary() : null;
            log.debug("Successfully created {} boundaries", createdBoundaries != null ? createdBoundaries.size() : 0);
            return createdBoundaries;
            
        } catch (Exception e) {
            log.error("Failed to create boundaries", e);
            if (e instanceof DigitClientException) {
                throw e;
            }
            throw new DigitClientException("Failed to create boundaries: " + e.getMessage(), e);
        }
    }

    /**
     * Searches boundaries by codes.
     *
     * @param codes the list of boundary codes to search for
     * @return the list of Boundary objects
     * @throws DigitClientException if search fails
     */
    public List<Boundary> searchBoundariesByCodes(List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            throw new DigitClientException("Codes list cannot be null or empty");
        }

        try {
            log.debug("Searching boundaries with codes: {}", codes);
            StringBuilder urlBuilder = new StringBuilder(apiProperties.getBoundaryServiceUrl() + "/boundary/v1?");
            for (String code : codes) {
                urlBuilder.append("codes=").append(code).append("&");
            }
            String url = urlBuilder.toString().replaceAll("&$", "");
            
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<BoundaryResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, BoundaryResponse.class);
            List<Boundary> boundaries = response.getBody() != null ? response.getBody().getBoundary() : null;
            
            log.debug("Successfully retrieved {} boundaries", boundaries != null ? boundaries.size() : 0);
            return boundaries;
            
        } catch (Exception e) {
            log.error("Failed to search boundaries with codes: {}", codes, e);
            if (e instanceof DigitClientException) {
                throw e;
            }
            throw new DigitClientException("Failed to search boundaries: " + e.getMessage(), e);
        }
    }

    /**
 * Validates whether the provided boundary codes exist in the Boundary service.
 *
 * @param codes the list of boundary codes to validate
 * @return true if all codes are valid (i.e., found), false otherwise
 * @throws DigitClientException if request fails or input is invalid
 */
public boolean isValidBoundariesByCodes(List<String> codes) {
    if (codes == null || codes.isEmpty()) {
        throw new DigitClientException("Codes list cannot be null or empty");
    }

    try {
        log.debug("Validating boundaries with codes: {}", codes);
        StringBuilder urlBuilder = new StringBuilder(apiProperties.getBoundaryServiceUrl())
                .append("/boundary/v1?");
        
        for (String code : codes) {
            urlBuilder.append("codes=").append(code).append("&");
        }
        String url = urlBuilder.toString().replaceAll("&$", "");

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<BoundaryResponse> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, BoundaryResponse.class);

        List<Boundary> boundaries = response.getBody() != null ? response.getBody().getBoundary() : null;
        int validCount = boundaries != null ? boundaries.size() : 0;

        boolean allValid = validCount == codes.size();
        log.debug("Boundary validation result: {} ({} out of {} found)", 
                  allValid ? "valid" : "invalid", validCount, codes.size());
        return allValid;

    } catch (Exception e) {
        log.error("Failed to validate boundaries with codes: {}", codes, e);
        if (e instanceof DigitClientException) {
            throw e;
        }
        throw new DigitClientException("Failed to validate boundaries: " + e.getMessage(), e);
    }
}


    /**
     * Updates a boundary.
     *
     * @param boundaryId the ID of the boundary to update
     * @param boundary the updated boundary data
     * @return the updated Boundary object
     * @throws DigitClientException if update fails
     */
    public Boundary updateBoundary(String boundaryId, Boundary boundary) {
        if (boundaryId == null || boundaryId.trim().isEmpty()) {
            throw new DigitClientException("Boundary ID cannot be null or empty");
        }
        if (boundary == null) {
            throw new DigitClientException("Boundary cannot be null");
        }

        try {
            log.debug("Updating boundary with ID: {}", boundaryId);
            String url = apiProperties.getBoundaryServiceUrl() + "/boundary/v1/" + boundaryId;
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            
            BoundaryRequest request = BoundaryRequest.builder().boundary(List.of(boundary)).build();
            HttpEntity<BoundaryRequest> requestEntity = new HttpEntity<>(request, headers);
            
            ResponseEntity<BoundaryResponse> response = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, BoundaryResponse.class);
            Boundary updatedBoundary = null;
            if (response.getBody() != null && response.getBody().getBoundary() != null && !response.getBody().getBoundary().isEmpty()) {
                updatedBoundary = response.getBody().getBoundary().get(0);
            }
            
            log.debug("Successfully updated boundary: {}", boundaryId);
            return updatedBoundary;
            
        } catch (Exception e) {
            log.error("Failed to update boundary with ID: {}", boundaryId, e);
            if (e instanceof DigitClientException) {
                throw e;
            }
            throw new DigitClientException("Failed to update boundary: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a boundary hierarchy definition.
     *
     * @param boundaryHierarchy the boundary hierarchy to create
     * @return the created BoundaryHierarchy object
     * @throws DigitClientException if creation fails
     */
    public BoundaryHierarchy createBoundaryHierarchy(BoundaryHierarchy boundaryHierarchy) {
        if (boundaryHierarchy == null) {
            throw new DigitClientException("BoundaryHierarchy cannot be null");
        }

        try {
            log.debug("Creating boundary hierarchy: {}", boundaryHierarchy.getHierarchyType());
            String url = apiProperties.getBoundaryServiceUrl() + "/boundary/v1/boundary-hierarchy-definition";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            
            BoundaryHierarchyRequest request = BoundaryHierarchyRequest.builder().hierarchy(boundaryHierarchy).build();
            HttpEntity<BoundaryHierarchyRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<BoundaryHierarchyResponse> response = restTemplate.postForEntity(url, entity, BoundaryHierarchyResponse.class);
            BoundaryHierarchy createdHierarchy = response.getBody() != null ? response.getBody().getHierarchy() : null;
            
            log.debug("Successfully created boundary hierarchy: {}", createdHierarchy != null ? createdHierarchy.getId() : "null");
            return createdHierarchy;
            
        } catch (Exception e) {
            log.error("Failed to create boundary hierarchy: {}", boundaryHierarchy.getHierarchyType(), e);
            if (e instanceof DigitClientException) {
                throw e;
            }
            throw new DigitClientException("Failed to create boundary hierarchy: " + e.getMessage(), e);
        }
    }

    /**
     * Searches boundary hierarchies by hierarchy type.
     *
     * @param hierarchyType the hierarchy type to search for
     * @return the BoundaryHierarchy object if found
     * @throws DigitClientException if search fails
     */
    public BoundaryHierarchy searchBoundaryHierarchy(String hierarchyType) {
        if (hierarchyType == null || hierarchyType.trim().isEmpty()) {
            throw new DigitClientException("Hierarchy type cannot be null or empty");
        }

        try {
            log.debug("Searching boundary hierarchy with type: {}", hierarchyType);
            String url = apiProperties.getBoundaryServiceUrl() + "/boundary/v1/boundary-hierarchy-definition?hierarchyType=" + hierarchyType;
            
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<BoundaryHierarchyResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, BoundaryHierarchyResponse.class);
            BoundaryHierarchy hierarchy = response.getBody() != null ? response.getBody().getHierarchy() : null;
            
            log.debug("Successfully retrieved boundary hierarchy: {}", hierarchyType);
            return hierarchy;
            
        } catch (Exception e) {
            log.error("Failed to search boundary hierarchy with type: {}", hierarchyType, e);
            if (e instanceof DigitClientException) {
                throw e;
            }
            throw new DigitClientException("Failed to search boundary hierarchy: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a boundary relationship.
     *
     * @param boundaryRelationship the boundary relationship to create
     * @return the created BoundaryRelationship object
     * @throws DigitClientException if creation fails
     */
    public BoundaryRelationship createBoundaryRelationship(BoundaryRelationship boundaryRelationship) {
        if (boundaryRelationship == null) {
            throw new DigitClientException("BoundaryRelationship cannot be null");
        }

        try {
            log.debug("Creating boundary relationship: {}", boundaryRelationship.getCode());
            String url = apiProperties.getBoundaryServiceUrl() + "/boundary/v1/boundary-relationships";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            
            BoundaryRelationshipRequest request = BoundaryRelationshipRequest.builder().relationship(List.of(boundaryRelationship)).build();
            HttpEntity<BoundaryRelationshipRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<BoundaryRelationshipResponse> response = restTemplate.postForEntity(url, entity, BoundaryRelationshipResponse.class);
            BoundaryRelationship createdRelationship = null;
            if (response.getBody() != null && response.getBody().getRelationship() != null && !response.getBody().getRelationship().isEmpty()) {
                createdRelationship = response.getBody().getRelationship().get(0);
            }
            
            log.debug("Successfully created boundary relationship: {}", createdRelationship != null ? createdRelationship.getId() : "null");
            return createdRelationship;
            
        } catch (Exception e) {
            log.error("Failed to create boundary relationship: {}", boundaryRelationship.getCode(), e);
            if (e instanceof DigitClientException) {
                throw e;
            }
            throw new DigitClientException("Failed to create boundary relationship: " + e.getMessage(), e);
        }
    }

    /**
     * Searches boundary relationships with hierarchical structure.
     *
     * @param hierarchyType the hierarchy type
     * @param boundaryType the boundary type (optional)
     * @param includeChildren whether to include children in the response
     * @return the list of HierarchyRelation objects
     * @throws DigitClientException if search fails
     */
    public List<BoundarySearchResponse.HierarchyRelation> searchBoundaryRelationships(String hierarchyType, String boundaryType, boolean includeChildren) {
        if (hierarchyType == null || hierarchyType.trim().isEmpty()) {
            throw new DigitClientException("Hierarchy type cannot be null or empty");
        }

        try {
            log.debug("Searching boundary relationships with hierarchy type: {}", hierarchyType);
            StringBuilder urlBuilder = new StringBuilder(apiProperties.getBoundaryServiceUrl() + "/boundary/v1/boundary-relationships?");
            urlBuilder.append("hierarchyType=").append(hierarchyType);
            if (boundaryType != null && !boundaryType.trim().isEmpty()) {
                urlBuilder.append("&boundaryType=").append(boundaryType);
            }
            urlBuilder.append("&includeChildren=").append(includeChildren);
            String url = urlBuilder.toString();
            
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<BoundarySearchResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, BoundarySearchResponse.class);
            List<BoundarySearchResponse.HierarchyRelation> relationships = response.getBody() != null ? response.getBody().getTenantBoundary() : null;
            
            log.debug("Successfully retrieved {} boundary relationships", relationships != null ? relationships.size() : 0);
            return relationships;
            
        } catch (Exception e) {
            log.error("Failed to search boundary relationships with hierarchy type: {}", hierarchyType, e);
            if (e instanceof DigitClientException) {
                throw e;
            }
            throw new DigitClientException("Failed to search boundary relationships: " + e.getMessage(), e);
        }
    }

    /**
     * Updates an existing boundary relationship.
     *
     * @param relationshipId the relationship ID to update
     * @param boundaryRelationship the updated relationship data
     * @return the updated BoundaryRelationship object
     * @throws DigitClientException if update fails
     */
    public BoundaryRelationship updateBoundaryRelationship(String relationshipId, BoundaryRelationship boundaryRelationship) {
        if (relationshipId == null || relationshipId.trim().isEmpty()) {
            throw new DigitClientException("Relationship ID cannot be null or empty");
        }
        if (boundaryRelationship == null) {
            throw new DigitClientException("BoundaryRelationship cannot be null");
        }

        try {
            log.debug("Updating boundary relationship with ID: {}", relationshipId);
            String url = apiProperties.getBoundaryServiceUrl() + "/boundary/v1/boundary-relationships/" + relationshipId;
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            
            HttpEntity<BoundaryRelationship> entity = new HttpEntity<>(boundaryRelationship, headers);
            
            ResponseEntity<BoundaryRelationship> response = restTemplate.exchange(url, HttpMethod.PUT, entity, BoundaryRelationship.class);
            BoundaryRelationship updatedRelationship = response.getBody();
            
            log.debug("Successfully updated boundary relationship: {}", relationshipId);
            return updatedRelationship;
            
        } catch (Exception e) {
            log.error("Failed to update boundary relationship with ID: {}", relationshipId, e);
            if (e instanceof DigitClientException) {
                throw e;
            }
            throw new DigitClientException("Failed to update boundary relationship: " + e.getMessage(), e);
        }
    }
}
