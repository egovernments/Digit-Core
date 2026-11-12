package digit.repository.impl;

import digit.config.ApplicationProperties;
import digit.kafka.Producer;
import digit.web.models.BoundaryRelation;
import digit.web.models.BulkBoundaryRelationshipRequestDTO;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.request.RequestInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link BoundaryRelationshipRepositoryImpl#createBulk}.
 *
 * <p>The batch change must publish the WHOLE validated list as EXACTLY ONE Kafka message (previously it
 * looped and published one message per record). These tests pin that contract: one push per bulk call,
 * carrying an array whose size equals the input size, on the dedicated bulk topic
 * (boundary-relationship-bulk-create-job, separate from the single-create topic), with the enriched
 * ancestralMaterializedPath preserved and the batch keyed by the shared parent.</p>
 */
@ExtendWith(MockitoExtension.class)
class BoundaryRelationshipRepositoryImplBulkTest {

    private static final String TOPIC = "boundary-relationship-bulk-create-job";

    @Mock
    private Producer producer;

    @Mock
    private ApplicationProperties applicationProperties;

    @InjectMocks
    private BoundaryRelationshipRepositoryImpl repository;

    private RequestInfo requestInfo() {
        return RequestInfo.builder().apiId("boundary").ver("1.0").build();
    }

    private BoundaryRelation relation(String code, String parent, String amp) {
        return BoundaryRelation.builder()
                .id("id-" + code)
                .code(code)
                .tenantId("mz")
                .hierarchyType("ADMIN")
                .boundaryType("Village")
                .parent(parent)
                .ancestralMaterializedPath(amp)
                .auditDetails(AuditDetails.builder()
                        .createdBy("u1").createdTime(1L).lastModifiedBy("u1").lastModifiedTime(1L).build())
                .build();
    }

    @Test
    void createBulk_publishesExactlyOneMessageCarryingTheWholeList() {
        lenient().when(applicationProperties.getBulkCreateBoundaryRelationshipJobTopic()).thenReturn(TOPIC);

        List<BoundaryRelation> input = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            input.add(relation("V" + i, "P1", "R1|P1"));
        }

        repository.createBulk(input, requestInfo());

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> valueCaptor = ArgumentCaptor.forClass(Object.class);

        // EXACTLY ONE push (was N=5 with the old per-record loop).
        verify(producer, times(1)).push(topicCaptor.capture(), keyCaptor.capture(), valueCaptor.capture());
        verify(producer, never()).push(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any());

        assertEquals(TOPIC, topicCaptor.getValue(), "bulk batch must go to the dedicated boundary-relationship-bulk-create-job topic, NOT save-boundary-relationship");
        assertEquals("P1", keyCaptor.getValue(), "batch keyed by the shared parent code");

        Object value = valueCaptor.getValue();
        assertNotNull(value);
        assertEquals(BulkBoundaryRelationshipRequestDTO.class, value.getClass(), "payload must be the batch DTO");
        BulkBoundaryRelationshipRequestDTO payload = (BulkBoundaryRelationshipRequestDTO) value;

        assertEquals(5, payload.getBoundaryRelationship().size(), "array size must equal input size");
        assertNotNull(payload.getRequestInfo(), "RequestInfo retained for persister version filter");

        // ancestralMaterializedPath is @JsonIgnore on BoundaryRelation but must survive on the DTO.
        assertEquals("R1|P1", payload.getBoundaryRelationship().get(0).getAncestralMaterializedPath());
        assertEquals("V0", payload.getBoundaryRelationship().get(0).getCode());
        assertEquals("id-V0", payload.getBoundaryRelationship().get(0).getId());
        assertNotNull(payload.getBoundaryRelationship().get(0).getAuditDetails());
    }

    @Test
    void createBulk_mixedParents_fallsBackToKeylessPush() {
        lenient().when(applicationProperties.getBulkCreateBoundaryRelationshipJobTopic()).thenReturn(TOPIC);

        List<BoundaryRelation> input = new ArrayList<>();
        input.add(relation("V0", "P1", "R1|P1"));
        input.add(relation("V1", "P2", "R1|P2"));

        repository.createBulk(input, requestInfo());

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(producer, times(1)).push(org.mockito.ArgumentMatchers.eq(TOPIC), keyCaptor.capture(), org.mockito.ArgumentMatchers.any());
        assertEquals(null, keyCaptor.getValue(), "mixed parents must fall back to keyless (null key)");
    }

    @Test
    void createBulk_emptyList_publishesNothing() {
        repository.createBulk(new ArrayList<>(), requestInfo());
        verify(producer, never()).push(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any());
        verify(producer, never()).push(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any());
    }
}
