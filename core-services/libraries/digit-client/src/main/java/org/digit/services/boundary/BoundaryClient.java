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
import org.digit.services.boundary.model.BoundaryRelationshipSearchCriteria;
import org.digit.services.boundary.model.BoundaryRequest;
import org.digit.services.boundary.model.BoundaryResponse;
import org.digit.services.boundary.model.BoundarySearchResponse;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class BoundaryClient {
    /** Deep enough for any real administrative hierarchy; a deeper tree means corrupt parent data. */
    private static final int MAX_ANCESTRY_DEPTH = 64;
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
            log.debug("Creating {} boundaries", boundaries.size());
            String url = this.apiProperties.getBoundaryServiceUrl() + "/boundary/v3/boundaries";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            BoundaryRequest request = BoundaryRequest.builder().boundary(boundaries).build();
            ResponseEntity response = this.restTemplate.exchange(url, HttpMethod.POST, new HttpEntity(request, headers), BoundaryResponse.class);
            List<Boundary> created = response.getBody() != null ? ((BoundaryResponse)response.getBody()).getBoundary() : null;
            log.debug("Successfully created {} boundaries", (created != null ? created.size() : 0));
            return created;
        }
        catch (Exception e) {
            throw DigitClientException.wrap("Failed to create boundaries", e);
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
            ResponseEntity response = this.restTemplate.exchange(url, HttpMethod.GET, new HttpEntity(new HttpHeaders()), BoundaryResponse.class);
            List<Boundary> boundaries = response.getBody() != null ? ((BoundaryResponse)response.getBody()).getBoundary() : null;
            log.debug("Successfully retrieved {} boundaries", (boundaries != null ? boundaries.size() : 0));
            return boundaries;
        }
        catch (Exception e) {
            throw DigitClientException.wrap("Failed to search boundaries", e);
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
            ResponseEntity response = this.restTemplate.exchange(urlBuilder.toString(), HttpMethod.GET, new HttpEntity(new HttpHeaders()), BoundarySearchResponse.class);
            BoundarySearchResponse body = response.getBody() != null ? (BoundarySearchResponse) response.getBody() : null;
            Set<String> foundCodes = body != null && body.getTenantBoundary() != null
                ? body.getTenantBoundary().stream()
                    .filter(r -> r.getBoundary() != null)
                    .flatMap(r -> r.getBoundary().stream())
                    .map(BoundarySearchResponse.EnrichedBoundary::getCode)
                    .collect(Collectors.toSet())
                : java.util.Collections.emptySet();
            boolean allValid = foundCodes.containsAll(codes);
            log.debug("Boundary validation result: {} ({} out of {} found in hierarchy {})", allValid ? "valid" : "invalid", foundCodes.size(), codes.size(), hierarchyType);
            return allValid;
        }
        catch (Exception e) {
            throw DigitClientException.wrap("Failed to validate boundaries", e);
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
            log.debug("Updating boundary with ID: {}", boundaryId);
            String url = this.apiProperties.getBoundaryServiceUrl() + "/boundary/v3/boundaries/" + boundaryId;
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            ResponseEntity response = this.restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity(boundary, headers), BoundaryResponse.class);
            Boundary updated = null;
            if (response.getBody() != null && ((BoundaryResponse)response.getBody()).getBoundary() != null && !((BoundaryResponse)response.getBody()).getBoundary().isEmpty()) {
                updated = ((BoundaryResponse)response.getBody()).getBoundary().get(0);
            }
            log.debug("Successfully updated boundary: {}", boundaryId);
            return updated;
        }
        catch (Exception e) {
            throw DigitClientException.wrap("Failed to update boundary", e);
        }
    }

    public BoundaryHierarchy createBoundaryHierarchy(BoundaryHierarchy boundaryHierarchy) {
        if (boundaryHierarchy == null) {
            throw new DigitClientException("BoundaryHierarchy cannot be null");
        }
        try {
            log.debug("Creating boundary hierarchy: {}", boundaryHierarchy.getHierarchyType());
            String url = this.apiProperties.getBoundaryServiceUrl() + "/boundary/v3/hierarchy";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            BoundaryHierarchyRequest request = BoundaryHierarchyRequest.builder().boundaryHierarchy(boundaryHierarchy).build();
            ResponseEntity response = this.restTemplate.postForEntity(url, new HttpEntity(request, headers), BoundaryHierarchyResponse.class);
            List<BoundaryHierarchy> list = response.getBody() != null ? ((BoundaryHierarchyResponse)response.getBody()).getHierarchy() : null;
            BoundaryHierarchy created = list != null && !list.isEmpty() ? list.get(0) : null;
            log.debug("Successfully created boundary hierarchy: {}", (created != null ? created.getId() : "null"));
            return created;
        }
        catch (Exception e) {
            throw DigitClientException.wrap("Failed to create boundary hierarchy", e);
        }
    }

    public BoundaryHierarchy searchBoundaryHierarchy(String hierarchyType) {
        if (hierarchyType == null || hierarchyType.trim().isEmpty()) {
            throw new DigitClientException("Hierarchy type cannot be null or empty");
        }
        try {
            log.debug("Searching boundary hierarchy with type: {}", hierarchyType);
            String url = this.apiProperties.getBoundaryServiceUrl() + "/boundary/v3/hierarchy?hierarchyType=" + hierarchyType;
            ResponseEntity response = this.restTemplate.exchange(url, HttpMethod.GET, new HttpEntity(new HttpHeaders()), BoundaryHierarchyResponse.class);
            List<BoundaryHierarchy> list = response.getBody() != null ? ((BoundaryHierarchyResponse)response.getBody()).getHierarchy() : null;
            BoundaryHierarchy hierarchy = list != null && !list.isEmpty() ? list.get(0) : null;
            log.debug("Successfully retrieved boundary hierarchy: {}", hierarchyType);
            return hierarchy;
        }
        catch (Exception e) {
            throw DigitClientException.wrap("Failed to search boundary hierarchy", e);
        }
    }

    public BoundaryRelationship createBoundaryRelationship(BoundaryRelationship boundaryRelationship) {
        if (boundaryRelationship == null) {
            throw new DigitClientException("BoundaryRelationship cannot be null");
        }
        try {
            log.debug("Creating boundary relationship: {}", boundaryRelationship.getCode());
            String url = this.apiProperties.getBoundaryServiceUrl() + "/boundary/v3/relationship";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            BoundaryRelationshipRequest request = BoundaryRelationshipRequest.builder().boundaryRelationship(boundaryRelationship).build();
            ResponseEntity response = this.restTemplate.postForEntity(url, new HttpEntity(request, headers), BoundaryRelationshipResponse.class);
            BoundaryRelationship created = null;
            if (response.getBody() != null && ((BoundaryRelationshipResponse)response.getBody()).getRelationship() != null && !((BoundaryRelationshipResponse)response.getBody()).getRelationship().isEmpty()) {
                created = ((BoundaryRelationshipResponse)response.getBody()).getRelationship().get(0);
            }
            log.debug("Successfully created boundary relationship: {}", (created != null ? created.getId() : "null"));
            return created;
        }
        catch (Exception e) {
            throw DigitClientException.wrap("Failed to create boundary relationship", e);
        }
    }

    public List<BoundarySearchResponse.HierarchyRelation> searchBoundaryRelationships(String hierarchyType, String boundaryType, boolean includeChildren) {
        return searchBoundaryRelationships(BoundaryRelationshipSearchCriteria.builder()
                .hierarchyType(hierarchyType)
                .boundaryType(boundaryType)
                .includeChildren(includeChildren ? Boolean.TRUE : null)
                .build());
    }

    /**
     * Searches boundary relationships.
     *
     * <p>{@code codes} becomes one repeated parameter per entry, and the two include flags are only
     * emitted when true — the service tests them against the literal string {@code "true"}, so
     * sending {@code false} would be pointless noise.
     */
    public List<BoundarySearchResponse.HierarchyRelation> searchBoundaryRelationships(BoundaryRelationshipSearchCriteria criteria) {
        if (criteria == null || criteria.getHierarchyType() == null || criteria.getHierarchyType().isBlank()) {
            throw new DigitClientException("Hierarchy type cannot be null or empty");
        }
        try {
            BoundarySearchResponse body = fetchRelationships(criteria);
            List<BoundarySearchResponse.HierarchyRelation> relations = body != null ? body.getTenantBoundary() : null;
            log.debug("Retrieved {} boundary relationship groups", relations != null ? relations.size() : 0);
            return relations;
        }
        catch (Exception e) {
            throw DigitClientException.wrap("Failed to search boundary relationships", e);
        }
    }

    /**
     * Resolves a boundary's full lineage, ordered root first and ending with {@code boundaryCode}.
     *
     * <p>The relationship search never returns the stored ancestral path — that value only appears on
     * create and update responses, and the search response even hides each node's parent. What it
     * does offer is {@code includeParents}, which makes the service walk the stored path server-side
     * and return the chain as a nested tree; this method asks for that and flattens it.
     *
     * <p>Returns an empty list when the code is unknown. When the code exists but its ancestors could
     * not be resolved — which happens when the tenant has no hierarchy definition, because the
     * service then seeds the tree from the boundary's own type — the result is the single boundary and
     * a warning is logged, so "no ancestors" is distinguishable from "no such boundary".
     */
    public List<BoundarySearchResponse.EnrichedBoundary> getBoundaryAncestry(String hierarchyType, String boundaryCode) {
        if (hierarchyType == null || hierarchyType.isBlank()) {
            // Without it the service cannot order boundary types, silently drops the ancestors it
            // fetched, and answers with just the leaf — indistinguishable from a boundary with no parent.
            throw new DigitClientException("hierarchyType is required to resolve boundary ancestry");
        }
        if (boundaryCode == null || boundaryCode.isBlank()) {
            throw new DigitClientException("boundaryCode is required to resolve boundary ancestry");
        }
        BoundarySearchResponse response = fetchRelationships(BoundaryRelationshipSearchCriteria.builder()
                .hierarchyType(hierarchyType)
                .codes(List.of(boundaryCode))
                .includeParents(Boolean.TRUE)
                .build());
        List<BoundarySearchResponse.EnrichedBoundary> path = resolveAncestryPath(response, boundaryCode);
        if (path.isEmpty()) {
            log.warn("boundary {} not found in hierarchy {} while resolving ancestry", boundaryCode, hierarchyType);
        } else if (path.size() == 1 && !boundaryCode.equals(path.get(0).getCode())) {
            log.warn("ancestry for {} resolved to an unrelated root; check the hierarchy definition for {}",
                    boundaryCode, hierarchyType);
        }
        return path;
    }

    /** The ancestry as codes, root first. */
    public List<String> getBoundaryAncestryCodes(String hierarchyType, String boundaryCode) {
        return getBoundaryAncestry(hierarchyType, boundaryCode).stream()
                .map(BoundarySearchResponse.EnrichedBoundary::getCode)
                .toList();
    }

    /** The immediate parent's code, or null when the boundary is a root or was not found. */
    public String getParentBoundaryCode(String hierarchyType, String boundaryCode) {
        List<String> codes = getBoundaryAncestryCodes(hierarchyType, boundaryCode);
        return codes.size() < 2 ? null : codes.get(codes.size() - 2);
    }

    /**
     * Walks a relationship search response to the ordered path from a root down to
     * {@code targetCode}. Pure and side-effect free, so it can be exercised without a service.
     *
     * <p>Guards against malformed data: an empty boundary list (which the service sends as JSON null
     * rather than an empty array), a code that repeats on one path, and pathological depth.
     */
    public static List<BoundarySearchResponse.EnrichedBoundary> resolveAncestryPath(BoundarySearchResponse response,
                                                                                    String targetCode) {
        if (response == null || response.getTenantBoundary() == null || targetCode == null) {
            return List.of();
        }
        List<BoundarySearchResponse.EnrichedBoundary> found = null;
        int matches = 0;
        for (BoundarySearchResponse.HierarchyRelation relation : response.getTenantBoundary()) {
            if (relation == null || relation.getBoundary() == null) {
                continue;
            }
            for (BoundarySearchResponse.EnrichedBoundary root : relation.getBoundary()) {
                List<BoundarySearchResponse.EnrichedBoundary> path =
                        depthFirst(root, targetCode, new ArrayList<>(), new HashSet<>(), 0);
                if (path != null) {
                    ++matches;
                    if (found == null) {
                        found = path;
                    }
                }
            }
        }
        if (matches > 1) {
            log.warn("boundary {} appears under {} roots; using the first", targetCode, matches);
        }
        return found == null ? List.of() : found;
    }

    private static List<BoundarySearchResponse.EnrichedBoundary> depthFirst(BoundarySearchResponse.EnrichedBoundary node,
                                                                            String targetCode,
                                                                            List<BoundarySearchResponse.EnrichedBoundary> trail,
                                                                            Set<String> onPath,
                                                                            int depth) {
        if (node == null || node.getCode() == null) {
            return null;
        }
        if (depth > MAX_ANCESTRY_DEPTH) {
            log.warn("boundary tree deeper than {} while resolving {}; giving up on this branch",
                    MAX_ANCESTRY_DEPTH, targetCode);
            return null;
        }
        if (!onPath.add(node.getCode())) {
            log.warn("cycle in boundary tree at {}", node.getCode());
            return null;
        }
        trail.add(node);
        try {
            if (targetCode.equals(node.getCode())) {
                return List.copyOf(trail);
            }
            if (node.getChildren() != null) {
                for (BoundarySearchResponse.EnrichedBoundary child : node.getChildren()) {
                    List<BoundarySearchResponse.EnrichedBoundary> path =
                            depthFirst(child, targetCode, trail, onPath, depth + 1);
                    if (path != null) {
                        return path;
                    }
                }
            }
            return null;
        } finally {
            trail.remove(trail.size() - 1);
            onPath.remove(node.getCode());
        }
    }

    private BoundarySearchResponse fetchRelationships(BoundaryRelationshipSearchCriteria criteria) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(this.apiProperties.getBoundaryServiceUrl() + "/boundary/v3/relationship")
                .queryParam("hierarchyType", criteria.getHierarchyType());
        if (criteria.getBoundaryType() != null && !criteria.getBoundaryType().isBlank()) {
            builder.queryParam("boundaryType", criteria.getBoundaryType());
        }
        if (criteria.getCodes() != null) {
            for (String code : criteria.getCodes()) {
                builder.queryParam("codes", code);
            }
        }
        if (criteria.getParent() != null && !criteria.getParent().isBlank()) {
            builder.queryParam("parent", criteria.getParent());
        }
        if (Boolean.TRUE.equals(criteria.getIncludeChildren())) {
            builder.queryParam("includeChildren", "true");
        }
        if (Boolean.TRUE.equals(criteria.getIncludeParents())) {
            builder.queryParam("includeParents", "true");
        }
        if (criteria.getLimit() != null && criteria.getLimit() > 0) {
            builder.queryParam("limit", criteria.getLimit());
        }
        if (criteria.getOffset() != null && criteria.getOffset() > 0) {
            builder.queryParam("offset", criteria.getOffset());
        }
        ResponseEntity<BoundarySearchResponse> response = this.restTemplate.exchange(
                builder.toUriString(), HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), BoundarySearchResponse.class);
        return response.getBody();
    }

    public BoundaryRelationship updateBoundaryRelationship(String relationshipId, BoundaryRelationship boundaryRelationship) {
        if (relationshipId == null || relationshipId.trim().isEmpty()) {
            throw new DigitClientException("Relationship ID cannot be null or empty");
        }
        if (boundaryRelationship == null) {
            throw new DigitClientException("BoundaryRelationship cannot be null");
        }
        try {
            log.debug("Updating boundary relationship with ID: {}", relationshipId);
            String url = this.apiProperties.getBoundaryServiceUrl() + "/boundary/v3/relationship/" + relationshipId;
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            ResponseEntity response = this.restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity(boundaryRelationship, headers), BoundaryRelationshipResponse.class);
            BoundaryRelationship updated = null;
            if (response.getBody() != null && ((BoundaryRelationshipResponse)response.getBody()).getRelationship() != null && !((BoundaryRelationshipResponse)response.getBody()).getRelationship().isEmpty()) {
                updated = ((BoundaryRelationshipResponse)response.getBody()).getRelationship().get(0);
            }
            log.debug("Successfully updated boundary relationship: {}", relationshipId);
            return updated;
        }
        catch (Exception e) {
            throw DigitClientException.wrap("Failed to update boundary relationship", e);
        }
    }

    /**
     * Updates a boundary hierarchy definition.
     *
     * <p>{@code hierarchyType} is fixed once created — the service keeps the stored value and ignores
     * whatever the payload carries — so this effectively edits the boundary-type list.
     */
    public BoundaryHierarchy updateBoundaryHierarchy(String hierarchyId, BoundaryHierarchy hierarchy) {
        if (hierarchyId == null || hierarchyId.isBlank()) {
            throw new DigitClientException("Hierarchy ID cannot be null or empty");
        }
        if (hierarchy == null) {
            throw new DigitClientException("BoundaryHierarchy cannot be null");
        }
        try {
            String url = this.apiProperties.getBoundaryServiceUrl() + "/boundary/v3/hierarchy/" + hierarchyId;
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            BoundaryHierarchyRequest request = BoundaryHierarchyRequest.builder()
                    .boundaryHierarchy(hierarchy).build();
            ResponseEntity<BoundaryHierarchyResponse> response = this.restTemplate.exchange(
                    url, HttpMethod.PUT, new HttpEntity<>(request, headers), BoundaryHierarchyResponse.class);
            BoundaryHierarchyResponse body = response.getBody();
            if (body == null || body.getHierarchy() == null || body.getHierarchy().isEmpty()) {
                return null;
            }
            return body.getHierarchy().get(0);
        }
        catch (Exception e) {
            throw DigitClientException.wrap("Failed to update boundary hierarchy", e);
        }
    }
}
