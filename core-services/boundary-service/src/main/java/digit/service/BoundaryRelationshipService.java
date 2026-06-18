package digit.service;

import digit.errors.ErrorCodes;
import digit.repository.BoundaryRelationshipRepository;
import digit.service.enrichment.BoundaryRelationshipEnricher;
import digit.service.validator.BoundaryRelationshipValidator;
import digit.util.HierarchyUtil;
import digit.web.models.*;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.utils.ResponseInfoUtil;
import org.egov.tracer.model.CustomException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BoundaryRelationshipService {

    private BoundaryRelationshipValidator boundaryRelationshipValidator;

    private BoundaryRelationshipEnricher boundaryRelationshipEnricher;

    private BoundaryRelationshipRepository boundaryRelationshipRepository;

    private HierarchyUtil hierarchyUtil;

    public BoundaryRelationshipService(BoundaryRelationshipValidator boundaryRelationshipValidator, BoundaryRelationshipEnricher boundaryRelationshipEnricher,
                                       BoundaryRelationshipRepository boundaryRelationshipRepository, HierarchyUtil hierarchyUtil) {
        this.boundaryRelationshipValidator = boundaryRelationshipValidator;
        this.boundaryRelationshipEnricher = boundaryRelationshipEnricher;
        this.boundaryRelationshipRepository = boundaryRelationshipRepository;
        this.hierarchyUtil = hierarchyUtil;
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
     * the single create), so a business failure on one record does not abort the others. The
     * records that pass validation are persisted synchronously within a single transaction, and the
     * response reports the outcome of every record: the successfully created relationships and,
     * separately, the failed ones with a reason for each.</p>
     *
     * @param body bulk create request
     * @return per-record success/failure response
     */
    public BulkBoundaryRelationshipResponse createBulkBoundaryRelationship(BulkBoundaryRelationshipRequest body) {

        RequestInfo requestInfo = body.getRequestInfo();
        List<BoundaryRelation> validatedRelationships = new ArrayList<>();
        List<FailedBoundaryRelationship> failedRelationships = new ArrayList<>();

        // Track records seen in this batch to reject intra-batch duplicates. The per-record
        // duplicate check queries the database, which cannot see other records in the same request;
        // without this, two identical records would both validate and the atomic batch insert would
        // hit a primary key violation, failing the whole batch.
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

        // Persist the validated records. Business failures were already isolated per-record above.
        // Fast path: one atomic batch insert. If that aborts, classify:
        //  - transient (deadlock / serialization / lost connection): the DB was unavailable for the
        //    whole transaction, so no row could have been inserted anyway -> report the whole batch
        //    retryable and let the consumer re-attempt the entire job.
        //  - otherwise it is a row-level error (a constraint/data problem a single record carried that
        //    validation didn't catch). Fall back to inserting each record in its OWN transaction, so a
        //    single un-insertable row no longer fails the rest: the good rows commit and only the
        //    offending row(s) are reported per-record.
        List<BoundaryRelation> successfulRelationships = validatedRelationships;
        if (!CollectionUtils.isEmpty(validatedRelationships)) {
            try {
                boundaryRelationshipRepository.createBulk(validatedRelationships);
            } catch (TransientDataAccessException | RecoverableDataAccessException e) {
                successfulRelationships = new ArrayList<>();
                markPersistFailure(validatedRelationships, failedRelationships, ErrorCodes.BULK_RELATIONSHIP_PERSIST_TRANSIENT_CODE, withCause(ErrorCodes.BULK_RELATIONSHIP_PERSIST_TRANSIENT_MSG, e));
            } catch (Exception e) {
                // Row-level fallback: isolate per record so one bad row doesn't sink the batch.
                successfulRelationships = new ArrayList<>();
                for (BoundaryRelation relationship : validatedRelationships) {
                    try {
                        boundaryRelationshipRepository.createOne(relationship);
                        successfulRelationships.add(relationship);
                    } catch (TransientDataAccessException | RecoverableDataAccessException te) {
                        // Transient while isolating this row -> retryable. The consumer redelivers the
                        // job; rows already committed in this fallback are idempotent no-ops on retry.
                        failedRelationships.add(FailedBoundaryRelationship.builder()
                                .boundaryRelationship(relationship)
                                .errorCode(ErrorCodes.BULK_RELATIONSHIP_PERSIST_TRANSIENT_CODE)
                                .errorMessage(withCause(ErrorCodes.BULK_RELATIONSHIP_PERSIST_TRANSIENT_MSG, te))
                                .build());
                    } catch (Exception ce) {
                        // This specific row genuinely cannot be persisted -> permanent, for this record only.
                        failedRelationships.add(FailedBoundaryRelationship.builder()
                                .boundaryRelationship(relationship)
                                .errorCode(ErrorCodes.BULK_RELATIONSHIP_PERSIST_FAILED_CODE)
                                .errorMessage(withCause(ErrorCodes.BULK_RELATIONSHIP_PERSIST_FAILED_MSG, ce))
                                .build());
                    }
                }
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
     * Records the whole validated set as failed with the given (transient or permanent) persistence
     * error code when the atomic batch insert could not be committed.
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
     * Combines the stable, human-readable error message for a code with the specific underlying cause
     * (when present), so failure records carry both the documented category and the concrete detail.
     */
    private String withCause(String message, Throwable cause) {
        return (cause != null && cause.getMessage() != null) ? message + " : " + cause.getMessage() : message;
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
