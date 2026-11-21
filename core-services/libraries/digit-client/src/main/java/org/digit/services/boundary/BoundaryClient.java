package org.digit.services.boundary;

import org.digit.config.ApiProperties;
import org.digit.exception.DigitClientException;
import org.digit.services.boundary.model.Boundary;
import org.digit.services.boundary.model.BoundaryHierarchy;
import org.digit.services.boundary.model.BoundaryHierarchyRequest;
import org.digit.services.boundary.model.BoundaryHierarchyResponse;
import org.digit.services.boundary.model.BoundaryRelationship;
import org.digit.services.boundary.model.BoundaryRelationshipRequest;
import org.digit.services.boundary.model.BoundaryRelationshipResponse;
import org.digit.services.boundary.model.BoundaryRequest;
import org.digit.services.boundary.model.BoundaryResponse;
import org.digit.services.boundary.model.BoundarySearchResponse;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class BoundaryClient {
    private final RestTemplate restTemplate;
    private final ApiProperties apiProperties;

    public BoundaryClient(RestTemplate restTemplate, ApiProperties apiProperties) {
        this.restTemplate = restTemplate;
        this.apiProperties = apiProperties;
    }

    public List<Boundary> createBoundaries(List<Boundary> boundaries) {
        if (boundaries == null || boundaries.isEmpty()) {
            throw new DigitClientException("Boundaries list cannot be null or empty");
        }
        try {
            log.debug("Creating {} boundaries", (Object)boundaries.size());
            String url = this.apiProperties.getBoundaryServiceUrl() + "/boundary/v3/boundaries";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            BoundaryRequest request = BoundaryRequest.builder().boundary(boundaries).build();
            ResponseEntity response = this.restTemplate.exchange(url, HttpMethod.POST, new HttpEntity((Object)request, headers), BoundaryResponse.class, new Object[0]);
            List<Boundary> created = response.getBody() != null ? ((BoundaryResponse)response.getBody()).getBoundary() : null;
            log.debug("Successfully created {} boundaries", (Object)(created != null ? created.size() : 0));
            return created;
        }
        catch (Exception e) {
            if (e instanceof DigitClientException) {
                throw e;
            }
            throw new DigitClientException("Failed to create boundaries: " + e.getMessage(), e);
        }
    }

    public List<Boundary> searchBoundariesByCodes(List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            throw new DigitClientException("Codes list cannot be null or empty");
        }
        try {
            log.debug("Searching boundaries with codes: {}", codes);
            StringBuilder urlBuilder = new StringBuilder(this.apiProperties.getBoundaryServiceUrl() + "/boundary/v3/boundaries?");
            for (String code : codes) {
                urlBuilder.append("codes=").append(code).append("&");
            }
            String url = urlBuilder.toString().replaceAll("&$", "");
            ResponseEntity response = this.restTemplate.exchange(url, HttpMethod.GET, new HttpEntity(new HttpHeaders()), BoundaryResponse.class, new Object[0]);
            List<Boundary> boundaries = response.getBody() != null ? ((BoundaryResponse)response.getBody()).getBoundary() : null;
            log.debug("Successfully retrieved {} boundaries", (Object)(boundaries != null ? boundaries.size() : 0));
            return boundaries;
        }
        catch (Exception e) {
            if (e instanceof DigitClientException) {
                throw e;
            }
            throw new DigitClientException("Failed to search boundaries: " + e.getMessage(), e);
        }
    }

    public boolean isValidBoundariesByCodes(List<String> codes, String hierarchyType) {
        if (codes == null || codes.isEmpty()) {
            throw new DigitClientException("Codes list cannot be null or empty");
        }
        if (hierarchyType == null || hierarchyType.trim().isEmpty()) {
            throw new DigitClientException("Hierarchy type cannot be null or empty");
        }
        try {
            log.debug("Validating boundaries with codes: {} in hierarchyType: {}", codes, hierarchyType);
            StringBuilder urlBuilder = new StringBuilder(this.apiProperties.getBoundaryServiceUrl() + "/boundary/v3/relationship?");
            urlBuilder.append("hierarchyType=").append(hierarchyType);
            for (String code : codes) {
                urlBuilder.append("&codes=").append(code);
            }
            ResponseEntity response = this.restTemplate.exchange(urlBuilder.toString(), HttpMethod.GET, new HttpEntity(new HttpHeaders()), BoundarySearchResponse.class, new Object[0]);
            BoundarySearchResponse body = response.getBody() != null ? (BoundarySearchResponse) response.getBody() : null;
            Set<String> foundCodes = body != null && body.getTenantBoundary() != null
                ? body.getTenantBoundary().stream()
                    .filter(r -> r.getBoundary() != null)
                    .flatMap(r -> r.getBoundary().stream())
                    .map(BoundarySearchResponse.EnrichedBoundary::getCode)
                    .collect(Collectors.toSet())
                : java.util.Collections.emptySet();
            boolean allValid = foundCodes.containsAll(codes);
            log.debug("Boundary validation result: {} ({} out of {} found in hierarchy {})", new Object[]{allValid ? "valid" : "invalid", foundCodes.size(), codes.size(), hierarchyType});
            return allValid;
        }
        catch (Exception e) {
            if (e instanceof DigitClientException) {
                throw e;
            }
            throw new DigitClientException("Failed to validate boundaries: " + e.getMessage(), e);
        }
    }

    public Boundary updateBoundary(String boundaryId, Boundary boundary) {
        if (boundaryId == null || boundaryId.trim().isEmpty()) {
            throw new DigitClientException("Boundary ID cannot be null or empty");
        }
        if (boundary == null) {
            throw new DigitClientException("Boundary cannot be null");
        }
        try {
            log.debug("Updating boundary with ID: {}", (Object)boundaryId);
            String url = this.apiProperties.getBoundaryServiceUrl() + "/boundary/v3/boundaries/" + boundaryId;
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            ResponseEntity response = this.restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity((Object)boundary, headers), BoundaryResponse.class, new Object[0]);
            Boundary updated = null;
            if (response.getBody() != null && ((BoundaryResponse)response.getBody()).getBoundary() != null && !((BoundaryResponse)response.getBody()).getBoundary().isEmpty()) {
                updated = ((BoundaryResponse)response.getBody()).getBoundary().get(0);
            }
            log.debug("Successfully updated boundary: {}", (Object)boundaryId);
            return updated;
        }
        catch (Exception e) {
            if (e instanceof DigitClientException) {
                throw e;
            }
            throw new DigitClientException("Failed to update boundary: " + e.getMessage(), e);
        }
    }

    public BoundaryHierarchy createBoundaryHierarchy(BoundaryHierarchy boundaryHierarchy) {
        if (boundaryHierarchy == null) {
            throw new DigitClientException("BoundaryHierarchy cannot be null");
        }
        try {
            log.debug("Creating boundary hierarchy: {}", (Object)boundaryHierarchy.getHierarchyType());
            String url = this.apiProperties.getBoundaryServiceUrl() + "/boundary/v3/hierarchy";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            BoundaryHierarchyRequest request = BoundaryHierarchyRequest.builder().boundaryHierarchy(boundaryHierarchy).build();
            ResponseEntity response = this.restTemplate.postForEntity(url, (Object)new HttpEntity((Object)request, headers), BoundaryHierarchyResponse.class, new Object[0]);
            List<BoundaryHierarchy> list = response.getBody() != null ? ((BoundaryHierarchyResponse)response.getBody()).getHierarchy() : null;
            BoundaryHierarchy created = list != null && !list.isEmpty() ? list.get(0) : null;
            log.debug("Successfully created boundary hierarchy: {}", (Object)(created != null ? created.getId() : "null"));
            return created;
        }
        catch (Exception e) {
            if (e instanceof DigitClientException) {
                throw e;
            }
            throw new DigitClientException("Failed to create boundary hierarchy: " + e.getMessage(), e);
        }
    }

    public BoundaryHierarchy searchBoundaryHierarchy(String hierarchyType) {
        if (hierarchyType == null || hierarchyType.trim().isEmpty()) {
            throw new DigitClientException("Hierarchy type cannot be null or empty");
        }
        try {
            log.debug("Searching boundary hierarchy with type: {}", (Object)hierarchyType);
            String url = this.apiProperties.getBoundaryServiceUrl() + "/boundary/v3/hierarchy?hierarchyType=" + hierarchyType;
            ResponseEntity response = this.restTemplate.exchange(url, HttpMethod.GET, new HttpEntity(new HttpHeaders()), BoundaryHierarchyResponse.class, new Object[0]);
            List<BoundaryHierarchy> list = response.getBody() != null ? ((BoundaryHierarchyResponse)response.getBody()).getHierarchy() : null;
            BoundaryHierarchy hierarchy = list != null && !list.isEmpty() ? list.get(0) : null;
            log.debug("Successfully retrieved boundary hierarchy: {}", (Object)hierarchyType);
            return hierarchy;
        }
        catch (Exception e) {
            if (e instanceof DigitClientException) {
                throw e;
            }
            throw new DigitClientException("Failed to search boundary hierarchy: " + e.getMessage(), e);
        }
    }

    public BoundaryRelationship createBoundaryRelationship(BoundaryRelationship boundaryRelationship) {
        if (boundaryRelationship == null) {
            throw new DigitClientException("BoundaryRelationship cannot be null");
        }
        try {
            log.debug("Creating boundary relationship: {}", (Object)boundaryRelationship.getCode());
            String url = this.apiProperties.getBoundaryServiceUrl() + "/boundary/v3/relationship";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            BoundaryRelationshipRequest request = BoundaryRelationshipRequest.builder().boundaryRelationship(boundaryRelationship).build();
            ResponseEntity response = this.restTemplate.postForEntity(url, (Object)new HttpEntity((Object)request, headers), BoundaryRelationshipResponse.class, new Object[0]);
            BoundaryRelationship created = null;
            if (response.getBody() != null && ((BoundaryRelationshipResponse)response.getBody()).getRelationship() != null && !((BoundaryRelationshipResponse)response.getBody()).getRelationship().isEmpty()) {
                created = ((BoundaryRelationshipResponse)response.getBody()).getRelationship().get(0);
            }
            log.debug("Successfully created boundary relationship: {}", (Object)(created != null ? created.getId() : "null"));
            return created;
        }
        catch (Exception e) {
            if (e instanceof DigitClientException) {
                throw e;
            }
            throw new DigitClientException("Failed to create boundary relationship: " + e.getMessage(), e);
        }
    }

    public List<BoundarySearchResponse.HierarchyRelation> searchBoundaryRelationships(String hierarchyType, String boundaryType, boolean includeChildren) {
        if (hierarchyType == null || hierarchyType.trim().isEmpty()) {
            throw new DigitClientException("Hierarchy type cannot be null or empty");
        }
        try {
            log.debug("Searching boundary relationships with hierarchy type: {}", (Object)hierarchyType);
            StringBuilder urlBuilder = new StringBuilder(this.apiProperties.getBoundaryServiceUrl() + "/boundary/v3/relationship?");
            urlBuilder.append("hierarchyType=").append(hierarchyType);
            if (boundaryType != null && !boundaryType.trim().isEmpty()) {
                urlBuilder.append("&boundaryType=").append(boundaryType);
            }
            urlBuilder.append("&includeChildren=").append(includeChildren);
            ResponseEntity response = this.restTemplate.exchange(urlBuilder.toString(), HttpMethod.GET, new HttpEntity(new HttpHeaders()), BoundarySearchResponse.class, new Object[0]);
            List<BoundarySearchResponse.HierarchyRelation> relationships = response.getBody() != null ? ((BoundarySearchResponse)response.getBody()).getTenantBoundary() : null;
            log.debug("Successfully retrieved {} boundary relationships", (Object)(relationships != null ? relationships.size() : 0));
            return relationships;
        }
        catch (Exception e) {
            if (e instanceof DigitClientException) {
                throw e;
            }
            throw new DigitClientException("Failed to search boundary relationships: " + e.getMessage(), e);
        }
    }

    public BoundaryRelationship updateBoundaryRelationship(String relationshipId, BoundaryRelationship boundaryRelationship) {
        if (relationshipId == null || relationshipId.trim().isEmpty()) {
            throw new DigitClientException("Relationship ID cannot be null or empty");
        }
        if (boundaryRelationship == null) {
            throw new DigitClientException("BoundaryRelationship cannot be null");
        }
        try {
            log.debug("Updating boundary relationship with ID: {}", (Object)relationshipId);
            String url = this.apiProperties.getBoundaryServiceUrl() + "/boundary/v3/relationship/" + relationshipId;
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            ResponseEntity response = this.restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity((Object)boundaryRelationship, headers), BoundaryRelationshipResponse.class, new Object[0]);
            BoundaryRelationship updated = null;
            if (response.getBody() != null && ((BoundaryRelationshipResponse)response.getBody()).getRelationship() != null && !((BoundaryRelationshipResponse)response.getBody()).getRelationship().isEmpty()) {
                updated = ((BoundaryRelationshipResponse)response.getBody()).getRelationship().get(0);
            }
            log.debug("Successfully updated boundary relationship: {}", (Object)relationshipId);
            return updated;
        }
        catch (Exception e) {
            if (e instanceof DigitClientException) {
                throw e;
            }
            throw new DigitClientException("Failed to update boundary relationship: " + e.getMessage(), e);
        }
    }
}