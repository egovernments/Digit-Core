package org.digit.services;

import tools.jackson.databind.ObjectMapper;
import org.digit.services.billing.model.Bill;
import org.digit.services.billing.model.Demand;
import org.digit.services.billing.model.DemandCreate;
import org.digit.services.billing.model.DemandStatus;
import org.digit.services.boundary.model.BoundaryHierarchy;
import org.digit.services.boundary.model.BoundaryHierarchyRequest;
import org.digit.services.boundary.model.BoundaryRelationship;
import org.digit.services.boundary.model.BoundaryRelationshipRequest;
import org.digit.services.individual.model.Identifier;
import org.digit.services.individual.model.Individual;
import org.digit.services.mdms.model.Mdms;
import org.digit.services.workflow.model.WorkflowProcessResponse;
import org.digit.util.DigitJson;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Asserts the wire shapes the clients must produce and consume.
 *
 * <p>Every payload below is written to match the current service DTO it names, so that a rename on
 * either side fails here. Because unknown fields are now ignored at runtime, these value assertions
 * are the only thing that catches a field quietly ceasing to populate.
 */
class WireContractTest {

    private final ObjectMapper mapper = DigitJson.mapper();

    // ── Requests: the shapes that used to be rejected outright ───────────────

    @Test
    void boundaryHierarchyRequest_usesHierarchyKey() throws Exception {
        // The service binds "hierarchy"; "boundaryHierarchy" bound an empty object and always 400'd.
        String json = mapper.writeValueAsString(BoundaryHierarchyRequest.builder()
                .boundaryHierarchy(BoundaryHierarchy.builder().hierarchyType("REVENUE").build())
                .build());
        assertTrue(json.contains("\"hierarchy\""), json);
        assertFalse(json.contains("\"boundaryHierarchy\""), json);
    }

    @Test
    void boundaryRelationshipRequest_usesRelationshipKey() throws Exception {
        String json = mapper.writeValueAsString(BoundaryRelationshipRequest.builder()
                .boundaryRelationship(BoundaryRelationship.builder().code("STATE1").build())
                .build());
        assertTrue(json.contains("\"relationship\""), json);
        assertFalse(json.contains("\"boundaryRelationship\""), json);
    }

    @Test
    void individualCreate_doesNotSendTenantId() throws Exception {
        // The service's DTO has no tenantId and parses writes strictly, so sending one 400'd.
        String json = mapper.writeValueAsString(Individual.builder()
                .givenName("Asha")
                .mobileNumber("9812000101")
                .identifiers(List.of(Identifier.builder().identifierType("PAN").identifierId("ABCDE1234F").build()))
                .build());
        assertFalse(json.contains("tenantId"), json);
        assertTrue(json.contains("\"givenName\":\"Asha\""), json);
        assertTrue(json.contains("\"identifiers\""), json);
    }

    @Test
    void demandCreate_usesArrearDemandIdsKey() throws Exception {
        String json = mapper.writeValueAsString(DemandCreate.builder()
                .consumerCode("C-1")
                .arrearDemandIds(List.of("d-1"))
                .status(DemandStatus.ACTIVE)
                .build());
        assertTrue(json.contains("\"arrearDemandIds\""), json);
        assertFalse(json.contains("arrearSourceDemandIds"), json);
    }

    // ── Responses: fields that previously threw or silently vanished ──────────

    @Test
    void demandResponse_populatesFieldsTheSdkUsedToLack() throws Exception {
        // Mirrors org.digit.billing.model.Demand: quoted decimals, billExpiryDays present only when
        // set, isDemandPaid spelled with its "is" prefix, auditDetail always present.
        String json = """
                {"id":"4b4a008e-682e-4c8e-9d0a-8f7dbf4aa771",
                 "businessServiceCode":"PT","periodFrom":1700000000000,"periodTo":1800000000000,
                 "consumerCode":"C-1","billExpiryDays":30,
                 "arrearDemandIds":["a-1"],
                 "lineItems":[{"id":"6269d90a-1279-40cf-b29d-0be04564e9b5","taxHeadCode":"PT_TAX","amount":"400","collectedAmount":"0"}],
                 "status":"FROZEN","totalAmount":"400","totalCollectedAmount":"0",
                 "isDemandPaid":false,"version":1,
                 "auditDetail":{"createdBy":"u-1","createdTime":1787141191641,"modifiedBy":"u-1","modifiedTime":1787141191641}}
                """;
        Demand demand = mapper.readValue(json, Demand.class);

        assertEquals(30, demand.getBillExpiryDays());
        assertEquals(new BigDecimal("400"), demand.getTotalAmount());
        assertEquals(List.of("a-1"), demand.getArrearDemandIds());
        // Would be null if the five real statuses were still missing from the enum.
        assertEquals(DemandStatus.FROZEN, demand.getStatus());
        assertFalse(demand.isDemandPaid());
        assertNotNull(demand.getAuditDetail());
        assertEquals("u-1", demand.getAuditDetail().getCreatedBy());
        assertEquals(new BigDecimal("400"), demand.getLineItems().get(0).getAmount());
    }

    @Test
    void billResponse_populatesAuditDetail() throws Exception {
        String json = """
                {"id":"ea9f37e0-9b34-4ce1-a7bc-7d5e52aed36a","consumerCode":"C-1","businessServiceCode":"PT",
                 "billNumber":"PB/2026/00001","billIssueAt":1787141191641,"billExpiryAt":1789733191641,
                 "status":"ACTIVE","totalAmount":"400","totalCollectedAmount":"0",
                 "auditDetail":{"createdBy":"u-1","createdTime":1787141191641,"modifiedBy":"u-1","modifiedTime":1787141191641}}
                """;
        Bill bill = mapper.readValue(json, Bill.class);

        assertNotNull(bill.getAuditDetail());
        assertEquals("u-1", bill.getAuditDetail().getCreatedBy());
        assertEquals(new BigDecimal("400"), bill.getTotalAmount());
        assertEquals(1787141191641L, bill.getBillIssueAt());
    }

    @Test
    void processDefinition_populatesStatesAndActions() throws Exception {
        // Mirrors ProcessDefinitionDetail: the process fields are unwrapped to the top level, only
        // states is nested, an action's wire name is "code", and false booleans are absent entirely.
        String json = """
                {"id":"p-1","code":"NOC","name":"NOC flow","version":"1","sla":86400000,"slotPercentage":50,
                 "auditDetail":{"createdBy":"u-1","createdTime":1787141191641},
                 "states":[
                   {"id":"s-1","code":"SUBMITTED","name":"Submitted","type":"INITIAL","isInitial":true,
                    "actions":[{"id":"a-1","code":"APPROVE","label":"Approve","nextState":"s-2","roles":["APPROVER"]}]},
                   {"id":"s-2","code":"APPROVED","name":"Approved","type":"TERMINAL_SUCCESS"}]}
                """;
        WorkflowProcessResponse definition = mapper.readValue(json, WorkflowProcessResponse.class);

        assertEquals("NOC", definition.getCode());
        assertEquals(50, definition.getSlotPercentage());
        assertNotNull(definition.getStates());
        assertEquals(2, definition.getStates().size());
        assertEquals("SUBMITTED", definition.getStates().get(0).getCode());
        assertTrue(definition.getStates().get(0).isInitial());
        // Absent in the payload because the service omits false booleans.
        assertFalse(definition.getStates().get(1).isInitial());
        // The action's Java field server-side is "name"; the wire name is "code".
        assertEquals("APPROVE", definition.getStates().get(0).getActions().get(0).getCode());
        assertEquals(List.of("APPROVER"), definition.getStates().get(0).getActions().get(0).getRoles());
    }

    @Test
    void generateIdResponse_readsLowercaseVersion() throws Exception {
        var response = mapper.readValue(
                "{\"templateCode\":\"EmployeeCode\",\"version\":\"v1\",\"id\":\"EMP-2026-00001\"}",
                org.digit.services.idgen.model.GenerateIDResponse.class);
        // Would be null if the field regressed to the capitalised "Version" key.
        assertEquals("v1", response.getVersion());
        assertEquals("EMP-2026-00001", response.getId());
    }

    @Test
    void mdms_leavesIsActiveUnsetOnEveryConstructionPath() throws Exception {
        // The three paths used to disagree: new Mdms() gave true, the builder gave null because
        // @Builder ignores field initializers, and a parsed response gave null because Jackson binds
        // through the all-args creator, where initializers never run. Defaulting it here would send
        // isActive=true on writes and, worse, report a response that omits the key as active.
        assertNull(new Mdms().getIsActive());
        assertNull(Mdms.builder().schemaCode("Trade.License").build().getIsActive());
        assertNull(mapper.readValue("{\"schemaCode\":\"Trade.License\"}", Mdms.class).getIsActive());

        String json = mapper.writeValueAsString(Mdms.builder().schemaCode("Trade.License").build());
        assertFalse(json.contains("isActive"), json);
    }

    @Test
    void workflowDocument_usesAdditionalAttributesKey() throws Exception {
        var document = org.digit.services.workflow.model.Document.builder()
                .fileStoreId("f-1")
                .additionalDetails(java.util.Map.of("k", "v"))
                .build();
        String json = mapper.writeValueAsString(document);
        // The service reads this metadata under "additionalAttributes"; it was silently dropped before.
        assertTrue(json.contains("\"additionalAttributes\""), json);
        assertFalse(json.contains("\"additionalDetails\""), json);
    }
}
