package digit.service;

import digit.config.ApplicationProperties;
import digit.errors.ErrorCodes;
import digit.repository.BoundaryIdentifierRepository;
import digit.service.validator.BoundaryIdentifierValidator;
import digit.util.IdGenUtil;
import digit.web.models.BoundaryIdentifier;
import digit.web.models.BoundaryIdentifierGenerateCriteria;
import digit.web.models.BoundaryIdentifierRequest;
import digit.web.models.BoundaryIdentifierResponse;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.tracer.model.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BoundaryIdentifierServiceGenerateTest {

    private static final String TENANT = "ba";
    private static final String HIERARCHY = "ADMIN";

    private BoundaryIdentifierRepository repository;
    private IdGenUtil idGenUtil;
    private BoundaryIdentifierService service;

    @BeforeEach
    void setUp() {
        repository = mock(BoundaryIdentifierRepository.class);
        idGenUtil = mock(IdGenUtil.class);
        ApplicationProperties config = new ApplicationProperties();
        config.setBoundaryIdentifierGenerateBatchSize(2);
        config.setBoundaryIdentifierIdGenName("boundary.identifier");
        service = new BoundaryIdentifierService(mock(BoundaryIdentifierValidator.class), repository, idGenUtil, config);

        when(repository.searchExistingIdentifierIds(any(), any(), any())).thenReturn(Collections.emptySet());
    }

    private static BoundaryIdentifierRequest generateRequest() {
        RequestInfo requestInfo = RequestInfo.builder().userInfo(User.builder().uuid("user-1").build()).build();
        return BoundaryIdentifierRequest.builder()
                .requestInfo(requestInfo)
                .generateCriteria(BoundaryIdentifierGenerateCriteria.builder().tenantId(TENANT).hierarchyType(HIERARCHY).boundaryType("Ward").build())
                .build();
    }

    private static Map<String, String> identifierIdByCode(BoundaryIdentifierResponse response) {
        return response.getBoundaryIdentifier().stream().collect(Collectors.toMap(BoundaryIdentifier::getCode, BoundaryIdentifier::getIdentifierId));
    }

    @Test
    void generatesForEveryCodeAndPublishesPerBatch() {
        when(repository.searchCodesWithoutIdentifier(any())).thenReturn(List.of("B1", "B2", "B3"));
        when(idGenUtil.getBoundaryIdentifierIds(any(), eq(TENANT), eq(2))).thenReturn(List.of("BND-1", "BND-2"));
        when(idGenUtil.getBoundaryIdentifierIds(any(), eq(TENANT), eq(1))).thenReturn(List.of("BND-3"));

        BoundaryIdentifierResponse response = service.createBoundaryIdentifier(generateRequest());

        assertEquals(Map.of("B1", "BND-1", "B2", "BND-2", "B3", "BND-3"), identifierIdByCode(response));
        response.getBoundaryIdentifier().forEach(identifier -> {
            assertNotNull(identifier.getId());
            assertEquals(HIERARCHY, identifier.getHierarchyType());
            assertEquals("user-1", identifier.getAuditDetails().getCreatedBy());
        });

        ArgumentCaptor<BoundaryIdentifierRequest> published = ArgumentCaptor.forClass(BoundaryIdentifierRequest.class);
        verify(repository, times(2)).create(published.capture());
        assertEquals(2, published.getAllValues().get(0).getBoundaryIdentifier().size());
        assertEquals(1, published.getAllValues().get(1).getBoundaryIdentifier().size());
    }

    @Test
    void usesBatchesOfHundredForIdgenAndKafkaByDefault() {
        ApplicationProperties config = new ApplicationProperties();
        config.setBoundaryIdentifierGenerateBatchSize(100);
        service = new BoundaryIdentifierService(mock(BoundaryIdentifierValidator.class), repository, idGenUtil, config);

        List<String> codes = IntStream.range(0, 250).mapToObj(i -> "B" + i).toList();
        when(repository.searchCodesWithoutIdentifier(any())).thenReturn(codes);
        when(idGenUtil.getBoundaryIdentifierIds(any(), eq(TENANT), anyInt())).thenAnswer(invocation ->
                IntStream.range(0, invocation.<Integer>getArgument(2)).mapToObj(i -> UUID.randomUUID().toString()).toList());

        BoundaryIdentifierResponse response = service.createBoundaryIdentifier(generateRequest());

        assertEquals(250, response.getBoundaryIdentifier().size());
        verify(idGenUtil, times(2)).getBoundaryIdentifierIds(any(), eq(TENANT), eq(100));
        verify(idGenUtil, times(1)).getBoundaryIdentifierIds(any(), eq(TENANT), eq(50));
        ArgumentCaptor<BoundaryIdentifierRequest> published = ArgumentCaptor.forClass(BoundaryIdentifierRequest.class);
        verify(repository, times(3)).create(published.capture());
        assertEquals(List.of(100, 100, 50), published.getAllValues().stream().map(batch -> batch.getBoundaryIdentifier().size()).toList());
    }

    @Test
    void replacesGeneratedIdThatIsAlreadyAssigned() {
        when(repository.searchCodesWithoutIdentifier(any())).thenReturn(List.of("B1", "B2"));
        when(idGenUtil.getBoundaryIdentifierIds(any(), eq(TENANT), eq(2))).thenReturn(List.of("BND-1", "BND-2"));
        when(idGenUtil.getBoundaryIdentifierIds(any(), eq(TENANT), eq(1))).thenReturn(List.of("BND-3"));
        when(repository.searchExistingIdentifierIds(TENANT, HIERARCHY, List.of("BND-1", "BND-2"))).thenReturn(Set.of("BND-1"));

        BoundaryIdentifierResponse response = service.createBoundaryIdentifier(generateRequest());

        assertEquals(Map.of("B1", "BND-2", "B2", "BND-3"), identifierIdByCode(response));
    }

    @Test
    void failsWhenIdgenKeepsReturningAssignedIds() {
        when(repository.searchCodesWithoutIdentifier(any())).thenReturn(List.of("B1"));
        when(idGenUtil.getBoundaryIdentifierIds(any(), eq(TENANT), eq(1))).thenReturn(List.of("TAKEN"));
        when(repository.searchExistingIdentifierIds(any(), any(), any())).thenReturn(Set.of("TAKEN"));

        CustomException ex = assertThrows(CustomException.class, () -> service.createBoundaryIdentifier(generateRequest()));
        assertEquals(ErrorCodes.IDGEN_UNIQUE_IDS_EXHAUSTED_CODE, ex.getCode());
        verify(repository, never()).create(any());
    }

    @Test
    void publishesNothingWhenIdgenFailsOnALaterBatch() {
        when(repository.searchCodesWithoutIdentifier(any())).thenReturn(List.of("B1", "B2", "B3"));
        when(idGenUtil.getBoundaryIdentifierIds(any(), eq(TENANT), eq(2))).thenReturn(List.of("BND-1", "BND-2"));
        when(idGenUtil.getBoundaryIdentifierIds(any(), eq(TENANT), eq(1))).thenThrow(new CustomException(ErrorCodes.IDGEN_ERROR_CODE, "down"));

        assertThrows(CustomException.class, () -> service.createBoundaryIdentifier(generateRequest()));
        verify(repository, never()).create(any());
    }

    @Test
    void generatesForListedCodesWithoutQueryingByType() {
        BoundaryIdentifierRequest request = generateRequest();
        request.getGenerateCriteria().setBoundaryType(null);
        request.getGenerateCriteria().setCodes(List.of("C1", "C2"));
        when(idGenUtil.getBoundaryIdentifierIds(any(), eq(TENANT), eq(2))).thenReturn(List.of("BND-1", "BND-2"));

        BoundaryIdentifierResponse response = service.createBoundaryIdentifier(request);

        assertEquals(Map.of("C1", "BND-1", "C2", "BND-2"), identifierIdByCode(response));
        verify(repository, never()).searchCodesWithoutIdentifier(any());
        verify(repository, times(1)).create(any());
    }

    @Test
    void returnsEmptyWhenEveryBoundaryAlreadyHasAnIdentifier() {
        when(repository.searchCodesWithoutIdentifier(any())).thenReturn(Collections.emptyList());

        BoundaryIdentifierResponse response = service.createBoundaryIdentifier(generateRequest());

        assertTrue(response.getBoundaryIdentifier().isEmpty());
        verifyNoInteractions(idGenUtil);
        verify(repository, never()).create(any());
    }
}
