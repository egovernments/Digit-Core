package digit.service;

import digit.config.ApplicationProperties;
import digit.errors.ErrorCodes;
import digit.repository.BoundaryRelationshipRepository;
import digit.service.enrichment.BoundaryRelationshipEnricher;
import digit.service.validator.BoundaryRelationshipValidator;
import digit.util.HierarchyUtil;
import digit.web.models.*;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.utils.ResponseInfoUtil;
import org.egov.tracer.model.CustomException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BoundaryRelationshipService {

    private BoundaryRelationshipValidator boundaryRelationshipValidator;

    private BoundaryRelationshipEnricher boundaryRelationshipEnricher;

    private BoundaryRelationshipRepository boundaryRelationshipRepository;

    private HierarchyUtil hierarchyUtil;

    private ApplicationProperties applicationProperties;

    public BoundaryRelationshipService(BoundaryRelationshipValidator boundaryRelationshipValidator, BoundaryRelationshipEnricher boundaryRelationshipEnricher,
                                       BoundaryRelationshipRepository boundaryRelationshipRepository, HierarchyUtil hierarchyUtil,
                                       ApplicationProperties applicationProperties) {
        this.boundaryRelationshipValidator = boundaryRelationshipValidator;
        this.boundaryRelationshipEnricher = boundaryRelationshipEnricher;
        this.boundaryRelationshipRepository = boundaryRelationshipRepository;
        this.hierarchyUtil = hierarchyUtil;
        this.applicationProperties = applicationProperties;
    }

    /**
     * Request handler for processing boundary relationship create requests.
     * @param body
     * @return
     */
    public BoundaryRelationshipResponse createBoundaryRelationship(BoundaryRelationshipRequest body) {

        // Validate boundary relationship and get ancestral materialized path if successfully validated
        String ancestralMaterializedPath = boundaryRelationshipValidator.validateBoundaryRelationshipCreateRequest(body);

        // Enrich boundary relationship
        boundaryRelationshipEnricher.enrichBoundaryRelationshipCreateRequest(body, ancestralMaterializedPath);

        // Delegate request to repository
        boundaryRelationshipRepository.create(body);

        // Create boundary relationship response and return
        return BoundaryRelationshipResponse.builder()
                .responseInfo(ResponseInfoUtil.createResponseInfoFromRequestInfo(body.getRequestInfo(), Boolean.TRUE))
                .tenantBoundary(Collections.singletonList(body.getBoundaryRelationship()))
                .build();

    }

    /**
     * Request handler for processing bulk boundary relationship create requests.
     *
     * <p>Each record is validated and enriched independently (reusing the same business rules as
     * the single create), so a business failure on one record does not abort the others. The records
     * that pass validation are handed to egov-persister for the actual DB write (see
     * {@link BoundaryRelationshipRepository#createBulk}); this service performs no direct DB insert.
     * The response reports the outcome of every record: the relationships accepted for persistence
     * and, separately, the ones that failed validation/enrichment with a reason for each.</p>
     *
     * @param body bulk create request
     * @return per-record success/failure response
     */
    public BulkBoundaryRelationshipResponse createBulkBoundaryRelationship(BulkBoundaryRelationshipRequest body) {

        RequestInfo requestInfo = body.getRequestInfo();

        // Guard the request envelope explicitly: bean validation (@Valid/@Size/@NotNull) is NOT active in
        // this service, and this endpoint is the sole entry for bulk creation, so the size/shape limits
        // must be enforced here (they are also required for a bounded synchronous validation+enrich pass).
        if (requestInfo == null || requestInfo.getUserInfo() == null) {
            throw new CustomException(ErrorCodes.BULK_REQUEST_INFO_MISSING_CODE, ErrorCodes.BULK_REQUEST_INFO_MISSING_MSG);
        }
        if (CollectionUtils.isEmpty(body.getBoundaryRelationships())) {
            throw new CustomException(ErrorCodes.BULK_REQUEST_EMPTY_CODE, ErrorCodes.BULK_REQUEST_EMPTY_MSG);
        }
        if (body.getBoundaryRelationships().size() > applicationProperties.getBulkCreateMaxSize()) {
            throw new CustomException(ErrorCodes.BULK_REQUEST_SIZE_EXCEEDED_CODE,
                    ErrorCodes.BULK_REQUEST_SIZE_EXCEEDED_MSG + applicationProperties.getBulkCreateMaxSize());
        }

        List<BoundaryRelation> validatedRelationships = new ArrayList<>();
        List<FailedBoundaryRelationship> failedRelationships = new ArrayList<>();

        // Track records seen in this batch to reject intra-batch duplicates. The per-record
        // duplicate check queries the database, which cannot see other records in the same request;
        // without this, two identical records would both validate and be published for the same
        // natural key, giving the caller no clear per-record duplicate signal and asking the persister
        // to insert the same (tenantId, code, hierarchyType) twice in one batch.
        Set<String> seenKeysInBatch = new HashSet<>();

        for (BoundaryRelation boundaryRelationship : body.getBoundaryRelationships()) {
            try {
                String key = buildUniquenessKey(boundaryRelationship);
                if (seenKeysInBatch.contains(key)) {
                    throw new CustomException(ErrorCodes.DUPLICATE_RECORD_IN_REQUEST_CODE, ErrorCodes.DUPLICATE_RECORD_IN_REQUEST_MSG);
                }

                // Reuse the existing single-record validation and enrichment so bulk and single
                // create share identical business rules.
                BoundaryRelationshipRequest singleRequest = BoundaryRelationshipRequest.builder()
                        .requestInfo(requestInfo)
                        .boundaryRelationship(boundaryRelationship)
                        .build();

                String ancestralMaterializedPath = boundaryRelationshipValidator.validateBoundaryRelationshipCreateRequest(singleRequest);
                boundaryRelationshipEnricher.enrichBoundaryRelationshipCreateRequest(singleRequest, ancestralMaterializedPath);

                seenKeysInBatch.add(key);
                validatedRelationships.add(singleRequest.getBoundaryRelationship());
            } catch (CustomException e) {
                failedRelationships.add(FailedBoundaryRelationship.builder()
                        .boundaryRelationship(boundaryRelationship)
                        .errorCode(e.getCode())
                        .errorMessage(e.getMessage())
                        .build());
            } catch (TransientDataAccessException | RecoverableDataAccessException | DataAccessResourceFailureException e) {
                // The validation pass issues several JDBC reads per record. A DB blip or Hikari pool
                // exhaustion surfaces as CannotGetJdbcConnectionException, which extends
                // DataAccessResourceFailureException (a NON-transient marker in Spring's hierarchy) — so it
                // must be caught explicitly here alongside the transient/recoverable markers. Classifying
                // it transient lets the caller retry (the reads are stateless and the persister insert is
                // idempotent) instead of the whole campaign aborting on a momentary DB saturation.
                failedRelationships.add(FailedBoundaryRelationship.builder()
                        .boundaryRelationship(boundaryRelationship)
                        .errorCode(ErrorCodes.BULK_RELATIONSHIP_PERSIST_TRANSIENT_CODE)
                        .errorMessage(withCause(ErrorCodes.BULK_RELATIONSHIP_PERSIST_TRANSIENT_MSG, e))
                        .build());
            } catch (Exception e) {
                // A non-business RuntimeException (e.g. null userInfo during enrichment, a malformed
                // hierarchy definition) must not unwind the whole job; record it as a per-record
                // failure so the remaining records still proceed.
                failedRelationships.add(FailedBoundaryRelationship.builder()
                        .boundaryRelationship(boundaryRelationship)
                        .errorCode(ErrorCodes.BULK_RELATIONSHIP_VALIDATION_ERROR_CODE)
                        .errorMessage(withCause(ErrorCodes.BULK_RELATIONSHIP_VALIDATION_ERROR_MSG, e))
                        .build());
            }
        }

        // Persistence is delegated to egov-persister (no direct DB write from this service): each
        // validated + enriched record is published, one message per record, to the save-boundary-relationship
        // topic, which egov-persister writes via an idempotent INSERT ... ON CONFLICT DO NOTHING. Publishing
        // is blocking (CustomKafkaTemplate.send().get()); a publish failure (broker unreachable within
        // max.block.ms, serialization) is reported transient so the caller retries the affected records —
        // re-publishing is a safe no-op because the insert is idempotent. One message per record preserves
        // per-record isolation on the persister side regardless of whether it runs the normal or the
        // (optional) batch listener.
        List<BoundaryRelation> successfulRelationships = validatedRelationships;
        if (!CollectionUtils.isEmpty(validatedRelationships)) {
            try {
                boundaryRelationshipRepository.createBulk(validatedRelationships, requestInfo);
            } catch (Exception e) {
                successfulRelationships = new ArrayList<>();
                markPersistFailure(validatedRelationships, failedRelationships, ErrorCodes.BULK_RELATIONSHIP_PERSIST_TRANSIENT_CODE, withCause(ErrorCodes.BULK_RELATIONSHIP_PERSIST_TRANSIENT_MSG, e));
            }
        }

        return BulkBoundaryRelationshipResponse.builder()
                .responseInfo(ResponseInfoUtil.createResponseInfoFromRequestInfo(requestInfo, Boolean.TRUE))
                .successfulBoundaryRelationships(successfulRelationships)
                .failedBoundaryRelationships(failedRelationships)
                .build();
    }

    /**
     * Builds the natural-key string (tenantId, hierarchyType, code) used to detect duplicate
     * relationships within a single bulk request. Mirrors the table's primary key.
     */
    private String buildUniquenessKey(BoundaryRelation boundaryRelationship) {
        return boundaryRelationship.getTenantId() + "|"
                + boundaryRelationship.getHierarchyType() + "|"
                + boundaryRelationship.getCode();
    }

    /**
     * Records the whole validated set as failed with the given (transient) persistence error code
     * when publishing the validated set to the batch-persister topic failed.
     */
    private void markPersistFailure(List<BoundaryRelation> relationships, List<FailedBoundaryRelationship> failures, String errorCode, String errorMessage) {
        for (BoundaryRelation relationship : relationships) {
            failures.add(FailedBoundaryRelationship.builder()
                    .boundaryRelationship(relationship)
                    .errorCode(errorCode)
                    .errorMessage(errorMessage)
                    .build());
        }
    }

    /**
     * Returns the stable, caller-facing message for a failure and logs the underlying cause separately.
     * The concrete exception text is deliberately NOT folded into the returned message: that string is
     * surfaced in the HTTP response payload and republished to the Kafka error topic, so raw
     * SQL/driver/internal details must not leak into it.
     */
    private String withCause(String message, Throwable cause) {
        if (cause != null) {
            log.warn("{} (cause: {})", message, cause.toString(), cause);
        }
        return message;
    }

    /**
     * Request handler for processing boundary relationship search requests.
     * @param boundaryRelationshipSearchCriteria
     * @return
     */
    public BoundarySearchResponse getBoundaryRelationships(BoundaryRelationshipSearchCriteria boundaryRelationshipSearchCriteria, RequestInfo requestInfo) {

        // Enrich search criteria
        boundaryRelationshipEnricher.enrichSearchCriteria(boundaryRelationshipSearchCriteria);

        // Get list of boundary relationships based on provided search criteria
        List<BoundaryRelationshipDTO> boundaries = boundaryRelationshipRepository.search(boundaryRelationshipSearchCriteria);

        // Get parent boundaries if includeParents flag is checked
        List<BoundaryRelationshipDTO> parentBoundaries = getParentBoundaries(boundaries, boundaryRelationshipSearchCriteria);

        // Get children boundaries if includeChildren flag is checked
        List<BoundaryRelationshipDTO> childrenBoundaries = getChildrenBoundaries(boundaries, boundaryRelationshipSearchCriteria);

        // Add parents and children boundaries to main boundary search list
        addParentsAndChildrenToBoundariesList(boundaries, parentBoundaries, childrenBoundaries);

        // Prepare search response for boundary search
        BoundarySearchResponse boundarySearchResponse = boundaryRelationshipEnricher.createBoundaryRelationshipSearchResponse(boundaries, boundaryRelationshipSearchCriteria.getTenantId(), boundaryRelationshipSearchCriteria.getHierarchyType(), requestInfo);

        // Return boundary search response
        return boundarySearchResponse;
    }

    /**
     * Service method to fetch children boundary DTOs.
     * @param boundaries
     * @param boundaryRelationshipSearchCriteria
     * @return
     */
    private List<BoundaryRelationshipDTO> getChildrenBoundaries(List<BoundaryRelationshipDTO> boundaries, BoundaryRelationshipSearchCriteria boundaryRelationshipSearchCriteria) {
        List<BoundaryRelationshipDTO> childrenBoundaries = new ArrayList<>();

        // Fetch children boundary DTOs if includeChildren flag is set to true.
        if (!CollectionUtils.isEmpty(boundaries) && boundaryRelationshipSearchCriteria.getIncludeChildren()) {
            List<String> currentBoundaryCodes = boundaries.stream()
                    .map(BoundaryRelationshipDTO::getCode)
                    .collect(Collectors.toList());

            childrenBoundaries = boundaryRelationshipRepository.search(BoundaryRelationshipSearchCriteria.builder()
                    .tenantId(boundaryRelationshipSearchCriteria.getTenantId())
                    .hierarchyType(boundaryRelationshipSearchCriteria.getHierarchyType())
                    .currentBoundaryCodes(currentBoundaryCodes)
                    .build());
        }

        return childrenBoundaries;
    }

    /**
     * Service method to fetch parent boundary DTOs.
     * @param boundaries
     * @param boundaryRelationshipSearchCriteria
     * @return
     */
    private List<BoundaryRelationshipDTO> getParentBoundaries(List<BoundaryRelationshipDTO> boundaries, BoundaryRelationshipSearchCriteria boundaryRelationshipSearchCriteria) {
        List<BoundaryRelationshipDTO> parentBoundaries = new ArrayList<>();

        // Fetch parent boundaries if includeParents flag is true.
        if (!CollectionUtils.isEmpty(boundaries) && boundaryRelationshipSearchCriteria.getIncludeParents()) {
            Set<String> allAncestorCodes = boundaries.stream()
                    .map(BoundaryRelationshipDTO::getAncestralMaterializedPath)
                    .filter(path -> path != null && !path.isEmpty())
                    .flatMap(path -> Arrays.stream(path.split("\\|")))
                    .collect(Collectors.toSet());

            // Root nodes have an empty materialized path, so they contribute no ancestor codes. If NONE of
            // the matched boundaries has an ancestor, there are no parents to fetch — return early. Passing
            // an empty codes list to search would otherwise cause the query builder to drop the
            // `code IN (...)` predicate and scan the entire tenant/hierarchy (a full-table read on a public
            // endpoint that would also return the whole tree as bogus parents).
            if (allAncestorCodes.isEmpty()) {
                return parentBoundaries;
            }

            parentBoundaries = boundaryRelationshipRepository.search(BoundaryRelationshipSearchCriteria.builder()
                    .tenantId(boundaryRelationshipSearchCriteria.getTenantId())
                    .hierarchyType(boundaryRelationshipSearchCriteria.getHierarchyType())
                    .codes(new ArrayList<>(allAncestorCodes))
                    .build());
        }

        return parentBoundaries;
    }

    /**
     * Request handler for processing boundary relationship update requests.
     * @param body
     * @return
     */
    public BoundaryRelationshipResponse updateBoundaryRelationship(BoundaryRelationshipRequest body) {

        // Validate update request
        BoundaryRelationshipRequestDTO validatedRelationshipDTORequest = boundaryRelationshipValidator.validateBoundaryRelationshipUpdateRequest(body);

        // Enrich update request
        String oldParentCode = boundaryRelationshipEnricher.enrichBoundaryRelationshipUpdateRequest(body, validatedRelationshipDTORequest);

        // Fetch children boundaries
        List<BoundaryRelationshipDTO> childrenBoundaryRelationships = getChildrenBoundaries(Collections
                .singletonList(validatedRelationshipDTORequest.getBoundaryRelationshipDTO()), BoundaryRelationshipSearchCriteria.builder()
                .tenantId(validatedRelationshipDTORequest.getBoundaryRelationshipDTO().getTenantId())
                .hierarchyType(validatedRelationshipDTORequest.getBoundaryRelationshipDTO().getHierarchyType())
                .includeChildren(Boolean.TRUE)
                .build());

        // Update ancestral materialized path of children boundary relationships
        preProcessNodesForUpdate(validatedRelationshipDTORequest, childrenBoundaryRelationships, oldParentCode);

        // Delegate request to repository
        boundaryRelationshipRepository.update(validatedRelationshipDTORequest);

        // Return response
        return BoundaryRelationshipResponse.builder()
                .responseInfo(ResponseInfoUtil.createResponseInfoFromRequestInfo(body.getRequestInfo(), Boolean.TRUE))
                .tenantBoundary(Collections.singletonList(body.getBoundaryRelationship()))
                .build();
    }

    /**
     * This method updates ancestral materialized path in the node being updated along with its
     * children nodes.
     * @param validatedRelationshipDTORequest
     * @param childrenBoundaryRelationships
     * @param oldParentCode
     */
    private void preProcessNodesForUpdate(BoundaryRelationshipRequestDTO validatedRelationshipDTORequest, List<BoundaryRelationshipDTO> childrenBoundaryRelationships, String oldParentCode) {
        // Add children boundary relationships to the list of nodes to be updated
        List<BoundaryRelationshipDTO> allNodesToBeUpdated = new ArrayList<>(childrenBoundaryRelationships);

        // Add the concerned boundary relationship which is being updated
        allNodesToBeUpdated.add(validatedRelationshipDTORequest.getBoundaryRelationshipDTO());

        // For each node, update ancestral materialized path - replace old parent code with new parent code
        allNodesToBeUpdated.forEach(boundaryRelationship -> {
            boundaryRelationship.setAncestralMaterializedPath(boundaryRelationship.getAncestralMaterializedPath()
                    .replace(oldParentCode,
                            validatedRelationshipDTORequest.getBoundaryRelationshipDTO().getParent()));
        });

        // Set list of nodes to be updated
        validatedRelationshipDTORequest.setBoundaryRelationshipDTOList(allNodesToBeUpdated);

    }

    /**
     * Add parent and children boundaries to searched boundaries list.
     * @param boundaries
     * @param parentBoundaries
     * @param childrenBoundaries
     */
    private void addParentsAndChildrenToBoundariesList(List<BoundaryRelationshipDTO> boundaries, List<BoundaryRelationshipDTO> parentBoundaries, List<BoundaryRelationshipDTO> childrenBoundaries) {
        boundaries.addAll(parentBoundaries);
        boundaries.addAll(childrenBoundaries);
    }

}
