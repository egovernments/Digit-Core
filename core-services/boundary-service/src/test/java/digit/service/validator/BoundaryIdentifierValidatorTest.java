package digit.service.validator;

import digit.config.ApplicationProperties;
import digit.errors.ErrorCodes;
import digit.repository.BoundaryIdentifierRepository;
import digit.web.models.*;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.tracer.model.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BoundaryIdentifierValidatorTest {

    private static final String TENANT = "ba";
    private static final String HIERARCHY = "ADMIN";

    private BoundaryIdentifierRepository identifierRepository;
    private BoundaryIdentifierValidator validator;

    @BeforeEach
    void setUp() {
        identifierRepository = mock(BoundaryIdentifierRepository.class);
        ApplicationProperties config = new ApplicationProperties();
        config.setBoundaryIdentifierMaxSize(100);
        config.setMaxDefaultLimit(300);
        validator = new BoundaryIdentifierValidator(identifierRepository, config);

        when(identifierRepository.searchCodesInHierarchy(any(), any(), anyCollection())).thenReturn(Collections.emptySet());
        when(identifierRepository.search(any())).thenReturn(Collections.emptyList());
    }

    private static BoundaryIdentifier identifier(String code, String identifierId) {
        return BoundaryIdentifier.builder().tenantId(TENANT).hierarchyType(HIERARCHY).code(code).identifierId(identifierId).build();
    }

    private static BoundaryIdentifierRequest request(BoundaryIdentifier... identifiers) {
        RequestInfo requestInfo = RequestInfo.builder().userInfo(User.builder().uuid("user-1").build()).build();
        return BoundaryIdentifierRequest.builder().requestInfo(requestInfo).boundaryIdentifier(Arrays.asList(identifiers)).build();
    }

    private void codesInHierarchy(String... codes) {
        when(identifierRepository.searchCodesInHierarchy(eq(TENANT), eq(HIERARCHY), anyCollection())).thenReturn(Set.of(codes));
    }

    private void existingByCodes(BoundaryIdentifier... existing) {
        when(identifierRepository.search(argThat(c -> c != null && c.getCodes() != null))).thenReturn(Arrays.asList(existing));
    }

    private void existingByIdentifierIds(BoundaryIdentifier... existing) {
        when(identifierRepository.search(argThat(c -> c != null && c.getIdentifierIds() != null))).thenReturn(Arrays.asList(existing));
    }

    // ---------- create ----------

    @Test
    void createAcceptsValidBulkRequest() {
        codesInHierarchy("B1", "B2");
        assertDoesNotThrow(() -> validator.validateCreateRequest(request(identifier("B1", "ID1"), identifier("B2", "ID2"))));
    }

    @Test
    void createRejectsEmptyRequest() {
        CustomException ex = assertThrows(CustomException.class, () -> validator.validateCreateRequest(request()));
        assertEquals(ErrorCodes.BOUNDARY_IDENTIFIER_REQUEST_EMPTY_CODE, ex.getCode());
    }

    @Test
    void createRejectsMissingFields() {
        BoundaryIdentifier noHierarchy = identifier("B3", "ID3");
        noHierarchy.setHierarchyType(null);
        CustomException ex = assertThrows(CustomException.class,
                () -> validator.validateCreateRequest(request(identifier("B1", "ID1"), identifier("B2", " "), noHierarchy)));
        assertEquals(ErrorCodes.INVALID_BOUNDARY_IDENTIFIER_CODE, ex.getCode());
        assertTrue(ex.getMessage().contains("[1, 2]"));
    }

    @Test
    void createRejectsDuplicateCodeAndIdentifierIdInRequest() {
        CustomException ex = assertThrows(CustomException.class, () -> validator.validateCreateRequest(
                request(identifier("B1", "ID1"), identifier("B1", "ID2"), identifier("B3", "ID2"))));
        assertTrue(ex.getErrors().containsKey(ErrorCodes.DUPLICATE_BOUNDARY_CODE_IN_REQUEST_CODE));
        assertTrue(ex.getErrors().containsKey(ErrorCodes.DUPLICATE_IDENTIFIER_ID_IN_REQUEST_CODE));
    }

    @Test
    void createAllowsSameCodeInDifferentHierarchies() {
        BoundaryIdentifier otherHierarchy = identifier("B1", "ID2");
        otherHierarchy.setHierarchyType("HEALTH");
        when(identifierRepository.searchCodesInHierarchy(eq(TENANT), any(), anyCollection())).thenReturn(Set.of("B1"));
        assertDoesNotThrow(() -> validator.validateCreateRequest(request(identifier("B1", "ID1"), otherHierarchy)));
    }

    @Test
    void createRejectsCodeNotInHierarchy() {
        codesInHierarchy("B1");
        CustomException ex = assertThrows(CustomException.class,
                () -> validator.validateCreateRequest(request(identifier("B1", "ID1"), identifier("B2", "ID2"))));
        String message = ex.getErrors().get(ErrorCodes.BOUNDARY_NOT_FOUND_CODE);
        assertTrue(message.contains("[B2]"));
        assertTrue(message.contains("hierarchyType " + HIERARCHY));
    }

    @Test
    void createRejectsCodeThatAlreadyHasIdentifier() {
        codesInHierarchy("B1");
        existingByCodes(identifier("B1", "OLD"));
        CustomException ex = assertThrows(CustomException.class, () -> validator.validateCreateRequest(request(identifier("B1", "ID1"))));
        assertTrue(ex.getErrors().containsKey(ErrorCodes.BOUNDARY_IDENTIFIER_ALREADY_EXISTS_CODE));
    }

    @Test
    void createRejectsIdentifierIdAlreadyInDb() {
        codesInHierarchy("B1");
        existingByIdentifierIds(identifier("OTHER", "ID1"));
        CustomException ex = assertThrows(CustomException.class, () -> validator.validateCreateRequest(request(identifier("B1", "ID1"))));
        assertTrue(ex.getErrors().get(ErrorCodes.IDENTIFIER_ID_ALREADY_ASSIGNED_CODE).contains("ID1=OTHER"));
    }

    // ---------- update ----------

    @Test
    void updateReturnsExistingRecords() {
        BoundaryIdentifier existing = identifier("B1", "OLD");
        existingByCodes(existing);
        List<BoundaryIdentifier> result = validator.validateUpdateRequest(request(identifier("B1", "NEW")));
        assertEquals(List.of(existing), result);
    }

    @Test
    void updateRejectsCodeWithoutIdentifier() {
        CustomException ex = assertThrows(CustomException.class, () -> validator.validateUpdateRequest(request(identifier("B1", "NEW"))));
        assertTrue(ex.getErrors().get(ErrorCodes.BOUNDARY_IDENTIFIER_NOT_FOUND_CODE).contains("[B1]"));
    }

    @Test
    void updateAllowsKeepingSameIdentifierOnSameCode() {
        existingByCodes(identifier("B1", "ID1"));
        existingByIdentifierIds(identifier("B1", "ID1"));
        assertDoesNotThrow(() -> validator.validateUpdateRequest(request(identifier("B1", "ID1"))));
    }

    @Test
    void updateRejectsIdentifierIdAssignedToAnotherCode() {
        existingByCodes(identifier("B1", "ID1"));
        existingByIdentifierIds(identifier("B2", "ID2"));
        CustomException ex = assertThrows(CustomException.class, () -> validator.validateUpdateRequest(request(identifier("B1", "ID2"))));
        assertTrue(ex.getErrors().get(ErrorCodes.IDENTIFIER_ID_ALREADY_ASSIGNED_CODE).contains("ID2=B2"));
    }

    @Test
    void updateRejectsDuplicateIdentifierIdInRequest() {
        CustomException ex = assertThrows(CustomException.class,
                () -> validator.validateUpdateRequest(request(identifier("B1", "ID1"), identifier("B2", "ID1"))));
        assertTrue(ex.getErrors().containsKey(ErrorCodes.DUPLICATE_IDENTIFIER_ID_IN_REQUEST_CODE));
    }

    @Test
    void updateRejectsGenerateCriteria() {
        BoundaryIdentifierRequest request = request(identifier("B1", "ID1"));
        request.setGenerateCriteria(BoundaryIdentifierGenerateCriteria.builder().tenantId(TENANT).hierarchyType(HIERARCHY).boundaryType("Ward").build());
        CustomException ex = assertThrows(CustomException.class, () -> validator.validateUpdateRequest(request));
        assertEquals(ErrorCodes.BOUNDARY_IDENTIFIER_GENERATE_NOT_ALLOWED_CODE, ex.getCode());
    }

    // ---------- generate ----------

    private static BoundaryIdentifierRequest generateByType(String hierarchyType) {
        BoundaryIdentifierRequest request = request();
        request.setGenerateCriteria(BoundaryIdentifierGenerateCriteria.builder().tenantId(TENANT).hierarchyType(hierarchyType).boundaryType("Ward").build());
        return request;
    }

    private static BoundaryIdentifierRequest generateByCodes(String... codes) {
        BoundaryIdentifierRequest request = request();
        request.setGenerateCriteria(BoundaryIdentifierGenerateCriteria.builder().tenantId(TENANT).hierarchyType(HIERARCHY).codes(Arrays.asList(codes)).build());
        return request;
    }

    @Test
    void generateAcceptsTenantHierarchyAndBoundaryType() {
        assertDoesNotThrow(() -> validator.validateGenerateRequest(generateByType(HIERARCHY)));
    }

    @Test
    void generateRequiresHierarchyType() {
        CustomException ex = assertThrows(CustomException.class, () -> validator.validateGenerateRequest(generateByType(null)));
        assertEquals(ErrorCodes.INVALID_GENERATE_CRITERIA_CODE, ex.getCode());
    }

    @Test
    void generateRequiresBoundaryTypeOrCodes() {
        BoundaryIdentifierRequest request = generateByType(HIERARCHY);
        request.getGenerateCriteria().setBoundaryType(null);
        CustomException ex = assertThrows(CustomException.class, () -> validator.validateGenerateRequest(request));
        assertEquals(ErrorCodes.INVALID_GENERATE_CRITERIA_CODE, ex.getCode());
    }

    @Test
    void generateRejectsManualListAlongsideCriteria() {
        BoundaryIdentifierRequest request = generateByType(HIERARCHY);
        request.setBoundaryIdentifier(List.of(identifier("B1", "ID1")));
        CustomException ex = assertThrows(CustomException.class, () -> validator.validateGenerateRequest(request));
        assertEquals(ErrorCodes.BOUNDARY_IDENTIFIER_CREATE_MODE_CODE, ex.getCode());
    }

    @Test
    void generateByCodesAcceptsCodesInHierarchyWithoutIdentifier() {
        codesInHierarchy("B1", "B2");
        assertDoesNotThrow(() -> validator.validateGenerateRequest(generateByCodes("B1", "B2")));
    }

    @Test
    void generateRejectsBothBoundaryTypeAndCodes() {
        BoundaryIdentifierRequest request = generateByCodes("B1");
        request.getGenerateCriteria().setBoundaryType("Ward");
        CustomException ex = assertThrows(CustomException.class, () -> validator.validateGenerateRequest(request));
        assertEquals(ErrorCodes.INVALID_GENERATE_CRITERIA_CODE, ex.getCode());
    }

    @Test
    void generateByCodesRejectsDuplicateCodes() {
        CustomException ex = assertThrows(CustomException.class, () -> validator.validateGenerateRequest(generateByCodes("B1", "B1")));
        assertEquals(ErrorCodes.DUPLICATE_BOUNDARY_CODE_IN_REQUEST_CODE, ex.getCode());
    }

    @Test
    void generateByCodesRejectsUnknownAndAlreadyMappedCodes() {
        codesInHierarchy("B1");
        existingByCodes(identifier("B1", "OLD"));
        CustomException ex = assertThrows(CustomException.class, () -> validator.validateGenerateRequest(generateByCodes("B1", "B2")));
        assertTrue(ex.getErrors().get(ErrorCodes.BOUNDARY_NOT_FOUND_CODE).contains("[B2]"));
        assertTrue(ex.getErrors().get(ErrorCodes.BOUNDARY_IDENTIFIER_ALREADY_EXISTS_CODE).contains("[B1]"));
    }

    // ---------- search ----------

    @Test
    void searchRequiresTenantIdAndHierarchyType() {
        CustomException noTenant = assertThrows(CustomException.class,
                () -> validator.validateSearchCriteria(BoundaryIdentifierSearchCriteria.builder().hierarchyType(HIERARCHY).build()));
        assertEquals(ErrorCodes.BOUNDARY_IDENTIFIER_SEARCH_CRITERIA_MISSING_CODE, noTenant.getCode());

        CustomException noHierarchy = assertThrows(CustomException.class,
                () -> validator.validateSearchCriteria(BoundaryIdentifierSearchCriteria.builder().tenantId(TENANT).build()));
        assertEquals(ErrorCodes.BOUNDARY_IDENTIFIER_SEARCH_CRITERIA_MISSING_CODE, noHierarchy.getCode());

        assertDoesNotThrow(() -> validator.validateSearchCriteria(BoundaryIdentifierSearchCriteria.builder().tenantId(TENANT).hierarchyType(HIERARCHY).build()));
    }
}
