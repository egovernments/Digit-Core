package digit.service.validator;

import digit.config.ApplicationProperties;
import digit.errors.ErrorCodes;
import digit.repository.BoundaryIdentifierRepository;
import digit.web.models.*;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Validates boundary identifier requests. Within a tenant and hierarchyType the code <-> identifierId
 * mapping is one-to-one, which is what every rule below enforces. Errors of different kinds are collected
 * and thrown together so a bulk caller can fix all of them in one round trip.
 */
@Component
public class BoundaryIdentifierValidator {

    private final BoundaryIdentifierRepository boundaryIdentifierRepository;

    private final ApplicationProperties config;

    public BoundaryIdentifierValidator(BoundaryIdentifierRepository boundaryIdentifierRepository, ApplicationProperties config) {
        this.boundaryIdentifierRepository = boundaryIdentifierRepository;
        this.config = config;
    }

    /**
     * Validates the create request
     * @1. Request is non-empty, within the size limit and every record has tenantId, hierarchyType, code and identifierId
     * @2. No boundary code and no identifierId repeats within the request (per tenantId + hierarchyType)
     * @3. Every boundary code exists in the hierarchy
     * @4. No boundary code already has an identifier in the hierarchy
     * @5. No identifierId is already assigned in the hierarchy
     * @param request
     */
    public void validateCreateRequest(BoundaryIdentifierRequest request) {

        validateRequestStructure(request);

        validateNoDuplicatesInRequest(request.getBoundaryIdentifier());

        Map<String, String> errors = new LinkedHashMap<>();

        groupByScope(request.getBoundaryIdentifier()).forEach((scope, identifiers) -> {
            Set<String> codes = collect(identifiers, BoundaryIdentifier::getCode);
            Set<String> identifierIds = collect(identifiers, BoundaryIdentifier::getIdentifierId);

            // boundary codes must exist in the hierarchy and must not already be mapped
            validateCodesCanBeMapped(scope, codes, errors);

            // identifierIds must not already be assigned
            Set<String> alreadyAssigned = searchByIdentifierIds(scope, identifierIds).stream()
                    .map(existing -> existing.getIdentifierId() + "=" + existing.getCode())
                    .collect(Collectors.toCollection(TreeSet::new));
            if (!alreadyAssigned.isEmpty()) {
                addError(errors, ErrorCodes.IDENTIFIER_ID_ALREADY_ASSIGNED_CODE, ErrorCodes.IDENTIFIER_ID_ALREADY_ASSIGNED_MSG, scope, alreadyAssigned);
            }
        });

        throwIfErrors(errors);
    }

    /**
     * Validates the update request. Records are matched to existing ones by tenantId + hierarchyType + code.
     * @1. Request is non-empty, within the size limit and every record has tenantId, hierarchyType, code and identifierId
     * @2. No boundary code and no identifierId repeats within the request (per tenantId + hierarchyType)
     * @3. Every boundary code already has an identifier in the hierarchy
     * @4. No new identifierId is currently assigned to a different boundary code in the hierarchy
     * @param request
     * @return the existing db records for the codes in the request, used for enrichment
     */
    public List<BoundaryIdentifier> validateUpdateRequest(BoundaryIdentifierRequest request) {

        if (request != null && request.getGenerateCriteria() != null) {
            throw new CustomException(ErrorCodes.BOUNDARY_IDENTIFIER_GENERATE_NOT_ALLOWED_CODE, ErrorCodes.BOUNDARY_IDENTIFIER_GENERATE_NOT_ALLOWED_MSG);
        }

        validateRequestStructure(request);

        validateNoDuplicatesInRequest(request.getBoundaryIdentifier());

        Map<String, String> errors = new LinkedHashMap<>();
        List<BoundaryIdentifier> existingRecords = new ArrayList<>();

        groupByScope(request.getBoundaryIdentifier()).forEach((scope, identifiers) -> {
            Set<String> codes = collect(identifiers, BoundaryIdentifier::getCode);
            Set<String> identifierIds = collect(identifiers, BoundaryIdentifier::getIdentifierId);

            // every code must already be mapped
            List<BoundaryIdentifier> existingForCodes = searchByCodes(scope, codes);
            existingRecords.addAll(existingForCodes);
            Set<String> missingCodes = new TreeSet<>(codes);
            existingForCodes.forEach(existing -> missingCodes.remove(existing.getCode()));
            if (!missingCodes.isEmpty()) {
                addError(errors, ErrorCodes.BOUNDARY_IDENTIFIER_NOT_FOUND_CODE, ErrorCodes.BOUNDARY_IDENTIFIER_NOT_FOUND_MSG, scope, missingCodes);
            }

            // an identifierId may stay on the same code, but must not belong to any other code
            Map<String, String> requestedCodeByIdentifierId = identifiers.stream()
                    .collect(Collectors.toMap(BoundaryIdentifier::getIdentifierId, BoundaryIdentifier::getCode));
            Set<String> assignedElsewhere = searchByIdentifierIds(scope, identifierIds).stream()
                    .filter(existing -> !existing.getCode().equals(requestedCodeByIdentifierId.get(existing.getIdentifierId())))
                    .map(existing -> existing.getIdentifierId() + "=" + existing.getCode())
                    .collect(Collectors.toCollection(TreeSet::new));
            if (!assignedElsewhere.isEmpty()) {
                addError(errors, ErrorCodes.IDENTIFIER_ID_ALREADY_ASSIGNED_CODE, ErrorCodes.IDENTIFIER_ID_ALREADY_ASSIGNED_MSG, scope, assignedElsewhere);
            }
        });

        throwIfErrors(errors);

        return existingRecords;
    }

    /**
     * Validates the auto-generate create request. GenerateCriteria carries tenantId, hierarchyType and exactly one of
     * @1. boundaryType: the manual create rules (code exists in the hierarchy, not yet mapped) are part of
     *     the query that selects the codes, so nothing more is checked here
     * @2. codes: validated like manual create - no blanks or repeats, within the size limit, every code
     *     exists in the hierarchy and none already has an identifier
     * In both modes identifierId uniqueness is checked against the ids returned by egov-idgen.
     * @param request
     */
    public void validateGenerateRequest(BoundaryIdentifierRequest request) {

        if (!CollectionUtils.isEmpty(request.getBoundaryIdentifier())) {
            throw new CustomException(ErrorCodes.BOUNDARY_IDENTIFIER_CREATE_MODE_CODE, ErrorCodes.BOUNDARY_IDENTIFIER_CREATE_MODE_MSG);
        }

        validateRequestInfo(request.getRequestInfo());

        BoundaryIdentifierGenerateCriteria criteria = request.getGenerateCriteria();
        boolean byBoundaryType = StringUtils.hasText(criteria.getBoundaryType());
        boolean byCodes = !CollectionUtils.isEmpty(criteria.getCodes());
        if (!StringUtils.hasText(criteria.getTenantId()) || !StringUtils.hasText(criteria.getHierarchyType()) || byBoundaryType == byCodes) {
            throw new CustomException(ErrorCodes.INVALID_GENERATE_CRITERIA_CODE, ErrorCodes.INVALID_GENERATE_CRITERIA_MSG);
        }

        if (byCodes) {
            validateGenerateCodes(new GenericPair<>(criteria.getTenantId(), criteria.getHierarchyType()), criteria.getCodes());
        }
    }

    /**
     * Validates the search criteria
     * @param criteria
     */
    public void validateSearchCriteria(BoundaryIdentifierSearchCriteria criteria) {
        if (criteria == null || !StringUtils.hasText(criteria.getTenantId()) || !StringUtils.hasText(criteria.getHierarchyType())) {
            throw new CustomException(ErrorCodes.BOUNDARY_IDENTIFIER_SEARCH_CRITERIA_MISSING_CODE, ErrorCodes.BOUNDARY_IDENTIFIER_SEARCH_CRITERIA_MISSING_MSG);
        }
    }

    private void validateGenerateCodes(GenericPair<String, String> scope, List<String> codes) {

        if (codes.size() > maxRequestSize()) {
            throw new CustomException(ErrorCodes.BOUNDARY_IDENTIFIER_REQUEST_SIZE_EXCEEDED_CODE, ErrorCodes.BOUNDARY_IDENTIFIER_REQUEST_SIZE_EXCEEDED_MSG + maxRequestSize());
        }

        if (codes.stream().anyMatch(code -> !StringUtils.hasText(code))) {
            throw new CustomException(ErrorCodes.INVALID_GENERATE_CRITERIA_CODE, ErrorCodes.INVALID_GENERATE_CRITERIA_MSG);
        }

        Set<String> seen = new HashSet<>();
        Set<String> duplicateCodes = codes.stream().filter(code -> !seen.add(code)).collect(Collectors.toCollection(TreeSet::new));
        if (!duplicateCodes.isEmpty()) {
            throw new CustomException(ErrorCodes.DUPLICATE_BOUNDARY_CODE_IN_REQUEST_CODE, ErrorCodes.DUPLICATE_BOUNDARY_CODE_IN_REQUEST_MSG + describe(scope) + " " + duplicateCodes);
        }

        Map<String, String> errors = new LinkedHashMap<>();
        validateCodesCanBeMapped(scope, seen, errors);
        throwIfErrors(errors);
    }

    /**
     * Adds an error for codes that are not part of the hierarchy and for codes that already have an identifier
     * @param scope tenantId and hierarchyType
     * @param codes
     * @param errors
     */
    private void validateCodesCanBeMapped(GenericPair<String, String> scope, Set<String> codes, Map<String, String> errors) {

        // boundary codes must exist in the hierarchy
        Set<String> codesInHierarchy = boundaryIdentifierRepository.searchCodesInHierarchy(scope.getFirst(), scope.getSecond(), codes);
        Set<String> missingBoundaryCodes = new TreeSet<>(codes);
        missingBoundaryCodes.removeAll(codesInHierarchy);
        if (!missingBoundaryCodes.isEmpty()) {
            addError(errors, ErrorCodes.BOUNDARY_NOT_FOUND_CODE, ErrorCodes.BOUNDARY_NOT_FOUND_MSG, scope, missingBoundaryCodes);
        }

        // boundary codes must not already be mapped
        Set<String> alreadyMappedCodes = searchByCodes(scope, codes).stream()
                .map(BoundaryIdentifier::getCode).collect(Collectors.toCollection(TreeSet::new));
        if (!alreadyMappedCodes.isEmpty()) {
            addError(errors, ErrorCodes.BOUNDARY_IDENTIFIER_ALREADY_EXISTS_CODE, ErrorCodes.BOUNDARY_IDENTIFIER_ALREADY_EXISTS_MSG, scope, alreadyMappedCodes);
        }
    }

    /**
     * Bean validation is not active in this deployment, so the request shape is checked here.
     * @param request
     */
    private void validateRequestStructure(BoundaryIdentifierRequest request) {

        if (request == null || CollectionUtils.isEmpty(request.getBoundaryIdentifier())) {
            throw new CustomException(ErrorCodes.BOUNDARY_IDENTIFIER_REQUEST_EMPTY_CODE, ErrorCodes.BOUNDARY_IDENTIFIER_REQUEST_EMPTY_MSG);
        }

        int maxSize = maxRequestSize();
        if (request.getBoundaryIdentifier().size() > maxSize) {
            throw new CustomException(ErrorCodes.BOUNDARY_IDENTIFIER_REQUEST_SIZE_EXCEEDED_CODE, ErrorCodes.BOUNDARY_IDENTIFIER_REQUEST_SIZE_EXCEEDED_MSG + maxSize);
        }

        validateRequestInfo(request.getRequestInfo());

        List<Integer> invalidIndexes = new ArrayList<>();
        List<BoundaryIdentifier> identifiers = request.getBoundaryIdentifier();
        for (int i = 0; i < identifiers.size(); i++) {
            BoundaryIdentifier identifier = identifiers.get(i);
            if (identifier == null || !StringUtils.hasText(identifier.getTenantId()) || !StringUtils.hasText(identifier.getHierarchyType())
                    || !StringUtils.hasText(identifier.getCode()) || !StringUtils.hasText(identifier.getIdentifierId())) {
                invalidIndexes.add(i);
            }
        }
        if (!invalidIndexes.isEmpty()) {
            throw new CustomException(ErrorCodes.INVALID_BOUNDARY_IDENTIFIER_CODE, ErrorCodes.INVALID_BOUNDARY_IDENTIFIER_MSG + invalidIndexes);
        }
    }

    /**
     * The user uuid is needed for audit details enrichment
     * @param requestInfo
     */
    private void validateRequestInfo(RequestInfo requestInfo) {
        if (requestInfo == null || requestInfo.getUserInfo() == null || !StringUtils.hasText(requestInfo.getUserInfo().getUuid())) {
            throw new CustomException(ErrorCodes.BOUNDARY_IDENTIFIER_REQUEST_INFO_MISSING_CODE, ErrorCodes.BOUNDARY_IDENTIFIER_REQUEST_INFO_MISSING_MSG);
        }
    }

    /**
     * Checks that no code and no identifierId repeats within the request, per tenantId + hierarchyType
     * @param identifiers
     */
    private void validateNoDuplicatesInRequest(List<BoundaryIdentifier> identifiers) {

        Map<String, String> errors = new LinkedHashMap<>();

        groupByScope(identifiers).forEach((scope, scopedIdentifiers) -> {
            Set<String> duplicateCodes = findDuplicates(scopedIdentifiers, BoundaryIdentifier::getCode);
            if (!duplicateCodes.isEmpty()) {
                addError(errors, ErrorCodes.DUPLICATE_BOUNDARY_CODE_IN_REQUEST_CODE, ErrorCodes.DUPLICATE_BOUNDARY_CODE_IN_REQUEST_MSG, scope, duplicateCodes);
            }

            Set<String> duplicateIdentifierIds = findDuplicates(scopedIdentifiers, BoundaryIdentifier::getIdentifierId);
            if (!duplicateIdentifierIds.isEmpty()) {
                addError(errors, ErrorCodes.DUPLICATE_IDENTIFIER_ID_IN_REQUEST_CODE, ErrorCodes.DUPLICATE_IDENTIFIER_ID_IN_REQUEST_MSG, scope, duplicateIdentifierIds);
            }
        });

        throwIfErrors(errors);
    }

    /**
     * Max records per request, capped at the search page limit so that the per-scope lookups used
     * for validation always fit in a single page.
     */
    private int maxRequestSize() {
        return Math.min(config.getBoundaryIdentifierMaxSize(), config.getMaxDefaultLimit());
    }

    private List<BoundaryIdentifier> searchByCodes(GenericPair<String, String> scope, Set<String> codes) {
        return boundaryIdentifierRepository.search(BoundaryIdentifierSearchCriteria.builder()
                .tenantId(scope.getFirst())
                .hierarchyType(scope.getSecond())
                .codes(new ArrayList<>(codes))
                .limit(codes.size())
                .build());
    }

    private List<BoundaryIdentifier> searchByIdentifierIds(GenericPair<String, String> scope, Set<String> identifierIds) {
        return boundaryIdentifierRepository.search(BoundaryIdentifierSearchCriteria.builder()
                .tenantId(scope.getFirst())
                .hierarchyType(scope.getSecond())
                .identifierIds(new ArrayList<>(identifierIds))
                .limit(identifierIds.size())
                .build());
    }

    /**
     * Groups the identifiers by (tenantId, hierarchyType), keeping request order
     */
    private static Map<GenericPair<String, String>, List<BoundaryIdentifier>> groupByScope(List<BoundaryIdentifier> identifiers) {
        return identifiers.stream().collect(Collectors.groupingBy(
                identifier -> new GenericPair<>(identifier.getTenantId(), identifier.getHierarchyType()), LinkedHashMap::new, Collectors.toList()));
    }

    private static Set<String> collect(List<BoundaryIdentifier> identifiers, Function<BoundaryIdentifier, String> field) {
        return identifiers.stream().map(field).collect(Collectors.toSet());
    }

    private static Set<String> findDuplicates(List<BoundaryIdentifier> identifiers, Function<BoundaryIdentifier, String> field) {
        Set<String> seen = new HashSet<>();
        return identifiers.stream().map(field).filter(value -> !seen.add(value)).collect(Collectors.toCollection(TreeSet::new));
    }

    private static String describe(GenericPair<String, String> scope) {
        return "tenantId " + scope.getFirst() + ", hierarchyType " + scope.getSecond();
    }

    private static void addError(Map<String, String> errors, String code, String message, GenericPair<String, String> scope, Set<String> values) {
        String detail = describe(scope) + " " + values;
        errors.merge(code, message + detail, (existing, added) -> existing + "; " + detail);
    }

    private static void throwIfErrors(Map<String, String> errors) {
        if (!ObjectUtils.isEmpty(errors)) {
            throw new CustomException(errors);
        }
    }
}
