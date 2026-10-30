package digit.service;

import digit.config.ApplicationProperties;
import digit.errors.ErrorCodes;
import digit.repository.BoundaryIdentifierRepository;
import digit.service.enrichment.BoundaryIdentifierEnricher;
import digit.service.validator.BoundaryIdentifierValidator;
import digit.util.IdGenUtil;
import digit.web.models.BoundaryIdentifier;
import digit.web.models.BoundaryIdentifierGenerateCriteria;
import digit.web.models.BoundaryIdentifierMapResponse;
import digit.web.models.BoundaryIdentifierRequest;
import digit.web.models.BoundaryIdentifierResponse;
import digit.web.models.BoundaryIdentifierSearchCriteria;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.utils.ResponseInfoUtil;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Service
@Slf4j
public class BoundaryIdentifierService {

    // How many times idgen is asked again for ids that turned out to be already assigned.
    private static final int MAX_IDGEN_ATTEMPTS = 3;

    private final BoundaryIdentifierValidator validator;

    private final BoundaryIdentifierRepository repository;

    private final IdGenUtil idGenUtil;

    private final ApplicationProperties config;

    public BoundaryIdentifierService(BoundaryIdentifierValidator validator, BoundaryIdentifierRepository repository,
                                     IdGenUtil idGenUtil, ApplicationProperties config) {
        this.validator = validator;
        this.repository = repository;
        this.idGenUtil = idGenUtil;
        this.config = config;
    }

    /**
     * This method is used to process a boundary identifier creation request. The request either carries
     * the identifiers to create, or GenerateCriteria to auto-generate identifiers for a boundaryType.
     * @param request is the request object
     * @return response
     */
    public BoundaryIdentifierResponse createBoundaryIdentifier(BoundaryIdentifierRequest request) {

        if (request != null && request.getGenerateCriteria() != null) {
            return generateBoundaryIdentifier(request);
        }

        // validate the request
        validator.validateCreateRequest(request);

        // enrich the request
        BoundaryIdentifierEnricher.enrichCreateRequest(request);

        // delegating the request to repository to further persist in db
        repository.create(request);

        return buildResponse(request.getBoundaryIdentifier(), request.getRequestInfo());
    }

    /**
     * Creates an identifier from egov-idgen for each listed boundary code, or for every boundary of the
     * requested boundaryType in the hierarchy that has no identifier yet. All ids are generated before
     * anything is published, so an idgen failure creates nothing.
     * @param request is the request object
     * @return response with the created identifiers (empty if every boundary of the type already has one)
     */
    private BoundaryIdentifierResponse generateBoundaryIdentifier(BoundaryIdentifierRequest request) {

        // validate the request
        validator.validateGenerateRequest(request);

        BoundaryIdentifierGenerateCriteria criteria = request.getGenerateCriteria();
        String tenantId = criteria.getTenantId();
        String hierarchyType = criteria.getHierarchyType();

        // the listed codes (already validated), or boundaries of the type that still need an identifier
        List<String> codes = CollectionUtils.isEmpty(criteria.getCodes())
                ? repository.searchCodesWithoutIdentifier(criteria)
                : new ArrayList<>(criteria.getCodes());
        log.info("Generating boundary identifiers for {} boundaries (boundaryType {}) in tenant {}, hierarchyType {}",
                codes.size(), criteria.getBoundaryType(), tenantId, hierarchyType);

        // generate ids batch-wise
        List<List<BoundaryIdentifier>> batches = new ArrayList<>();
        int batchSize = config.getBoundaryIdentifierGenerateBatchSize();
        for (int from = 0; from < codes.size(); from += batchSize) {
            List<String> batchCodes = codes.subList(from, Math.min(from + batchSize, codes.size()));
            List<String> identifierIds = generateUnassignedIdentifierIds(request.getRequestInfo(), tenantId, hierarchyType, batchCodes.size());

            List<BoundaryIdentifier> batch = new ArrayList<>();
            for (int i = 0; i < batchCodes.size(); i++) {
                batch.add(BoundaryIdentifier.builder().tenantId(tenantId).hierarchyType(hierarchyType).code(batchCodes.get(i)).identifierId(identifierIds.get(i)).build());
            }
            batches.add(batch);
        }

        // enrich and persist batch-wise
        List<BoundaryIdentifier> created = new ArrayList<>();
        batches.forEach(batch -> {
            BoundaryIdentifierRequest batchRequest = BoundaryIdentifierRequest.builder()
                    .requestInfo(request.getRequestInfo())
                    .boundaryIdentifier(batch)
                    .build();
            BoundaryIdentifierEnricher.enrichCreateRequest(batchRequest);
            repository.create(batchRequest);
            created.addAll(batch);
        });

        return buildResponse(created, request.getRequestInfo());
    }

    /**
     * Gets count ids from egov-idgen, replacing any that are already assigned (e.g. an identifierId
     * created manually that happens to match the idgen format).
     * @param requestInfo
     * @param tenantId
     * @param hierarchyType
     * @param count
     * @return count ids that are not assigned yet in the hierarchy
     */
    private List<String> generateUnassignedIdentifierIds(RequestInfo requestInfo, String tenantId, String hierarchyType, int count) {

        Set<String> identifierIds = new LinkedHashSet<>();
        for (int attempt = 0; attempt < MAX_IDGEN_ATTEMPTS && identifierIds.size() < count; attempt++) {
            List<String> generated = idGenUtil.getBoundaryIdentifierIds(requestInfo, tenantId, count - identifierIds.size());
            Set<String> alreadyAssigned = repository.searchExistingIdentifierIds(tenantId, hierarchyType, generated);
            generated.stream().filter(id -> !alreadyAssigned.contains(id)).forEach(identifierIds::add);
        }

        if (identifierIds.size() < count) {
            throw new CustomException(ErrorCodes.IDGEN_UNIQUE_IDS_EXHAUSTED_CODE, ErrorCodes.IDGEN_UNIQUE_IDS_EXHAUSTED_MSG + config.getBoundaryIdentifierIdGenName());
        }
        return new ArrayList<>(identifierIds);
    }

    /**
     * This method is used to search boundary identifiers of a tenant and hierarchyType, optionally by
     * boundary codes and/or identifierIds. Without limit every matching record is returned.
     * @param criteria
     * @param requestInfo
     * @return response
     */
    public BoundaryIdentifierMapResponse searchBoundaryIdentifier(BoundaryIdentifierSearchCriteria criteria, RequestInfo requestInfo) {

        // validate the criteria
        validator.validateSearchCriteria(criteria);

        // search for boundary identifiers
        List<BoundaryIdentifier> boundaryIdentifiers = repository.search(criteria);

        return buildMapResponse(boundaryIdentifiers, requestInfo);
    }

    /**
     * This method is used to process a bulk boundary identifier update request
     * @param request is the request object
     * @return response
     */
    public BoundaryIdentifierResponse updateBoundaryIdentifier(BoundaryIdentifierRequest request) {

        // validate the request and fetch the records being updated
        List<BoundaryIdentifier> existingRecords = validator.validateUpdateRequest(request);

        // enrich the request
        BoundaryIdentifierEnricher.enrichUpdateRequest(request, existingRecords);

        // delegating the request to repository to update the records in db
        repository.update(request);

        return buildResponse(request.getBoundaryIdentifier(), request.getRequestInfo());
    }

    /**
     * Builds the search response as boundary code -> identifierId, in the order of the given list
     * @param boundaryIdentifiers
     * @param requestInfo
     * @return
     */
    private BoundaryIdentifierMapResponse buildMapResponse(List<BoundaryIdentifier> boundaryIdentifiers, RequestInfo requestInfo) {
        Map<String, String> identifierIdByCode = new LinkedHashMap<>();
        boundaryIdentifiers.forEach(identifier -> identifierIdByCode.put(identifier.getCode(), identifier.getIdentifierId()));
        return BoundaryIdentifierMapResponse.builder()
                .responseInfo(ResponseInfoUtil.createResponseInfoFromRequestInfo(requestInfo, Boolean.TRUE))
                .boundaryIdentifier(identifierIdByCode)
                .build();
    }

    private BoundaryIdentifierResponse buildResponse(List<BoundaryIdentifier> boundaryIdentifiers, RequestInfo requestInfo) {
        return BoundaryIdentifierResponse.builder()
                .responseInfo(ResponseInfoUtil.createResponseInfoFromRequestInfo(requestInfo, Boolean.TRUE))
                .boundaryIdentifier(boundaryIdentifiers)
                .build();
    }
}
