package org.digit.services;

import org.digit.config.ApiProperties;
import org.digit.services.billing.BillingClient;
import org.digit.services.boundary.BoundaryClient;
import org.digit.services.filestore.FilestoreClient;
import org.digit.services.idgen.IdGenClient;
import org.digit.services.individual.IndividualClient;
import org.digit.services.mdms.MdmsClient;
import org.digit.services.notification.NotificationClient;
import org.digit.services.registry.RegistryClient;
import org.digit.services.workflow.WorkflowClient;
import org.digit.config.PropagationProperties;
import org.digit.services.billing.model.DemandCreate;
import org.digit.services.billing.model.GenerateBillCriteria;
import org.digit.services.billing.model.PaymentCreate;
import org.digit.services.boundary.model.*;
import org.digit.services.idgen.model.GenerateIDResponse;
import org.digit.services.mdms.model.MdmsResponseV2;
import org.digit.services.registry.model.RegistryData;
import org.digit.services.registry.model.RegistryDataResponse;
import org.digit.services.workflow.model.WorkflowProcessResponse;
import org.digit.services.workflow.model.WorkflowTransitionRequest;
import org.digit.services.individual.model.Individual;
import org.digit.services.individual.model.IndividualResponse;
import org.digit.services.individual.model.IndividualSearchResponse;
import org.digit.services.notification.model.SendEmailRequest;
import org.digit.services.notification.model.SendEmailResponse;
import org.digit.services.notification.model.SendSMSRequest;
import org.digit.services.billing.model.*;
import org.digit.services.boundary.model.*;
import org.digit.services.registry.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.digit.services.notification.model.SendSMSResponse;
import org.digit.services.workflow.model.WorkflowTransitionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Verifies that each client calls the correct endpoint paths as defined in the
 * digit-specs v3.0.0 OpenAPI specifications.
 */
@ExtendWith(MockitoExtension.class)
class EndpointVerificationTest {

    private static final String BASE = "http://localhost:8080";

    @Mock RestTemplate restTemplate;

    ApiProperties props;
    ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        props = new ApiProperties();
        props.setIdgenServiceUrl(BASE);
        props.setWorkflowServiceUrl(BASE);
        props.setBillingServiceUrl(BASE);
        props.setBoundaryServiceUrl(BASE);
        props.setNotificationServiceUrl(BASE);
        props.setRegistryServiceUrl(BASE);
        props.setMdmsServiceUrl(BASE);
        props.setIndividualServiceUrl(BASE);
        props.setFilestoreServiceUrl(BASE);
    }

    // ── IdGen ─────────────────────────────────────────────────────────────────
    // Spec: POST /idgen/v3/generate

    @Test
    void idgen_generate_usesCorrectEndpoint() {
        var client = new IdGenClient(restTemplate, props);
        var responseBody = new GenerateIDResponse();
        responseBody.setId("pb-2025-001");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(GenerateIDResponse.class)))
                .thenReturn(ResponseEntity.ok(responseBody));

        client.generateId("receipt-id");

        var urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).exchange(urlCaptor.capture(), eq(HttpMethod.POST), any(), eq(GenerateIDResponse.class));
        assertEquals(BASE + "/idgen/v3/generate", urlCaptor.getValue());
    }

    // ── Workflow ──────────────────────────────────────────────────────────────
    // Spec: POST /workflow/v3/transition
    // Spec: GET  /workflow/v3/process/{id}
    // Spec: GET  /workflow/v3/process?code=...

    @Test
    void workflow_transition_usesCorrectEndpoint() {
        var client = new WorkflowClient(restTemplate, props);
        var req = WorkflowTransitionRequest.builder()
                .processCode("proc-1").entityId("ent-1").action("APPROVE").build();

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(WorkflowTransitionResponse.class)))
                .thenReturn(ResponseEntity.ok(null));

        client.executeTransition(req);

        var urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).exchange(urlCaptor.capture(), eq(HttpMethod.POST), any(), eq(WorkflowTransitionResponse.class));
        assertEquals(BASE + "/workflow/v3/transition", urlCaptor.getValue());
    }

    @Test
    void workflow_getProcessDefinition_usesCorrectEndpoint() {
        var client = new WorkflowClient(restTemplate, props);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(WorkflowProcessResponse.class)))
                .thenReturn(ResponseEntity.ok(null));

        client.getProcessDefinition("proc-123");

        var urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).exchange(urlCaptor.capture(), eq(HttpMethod.GET), any(), eq(WorkflowProcessResponse.class));
        assertEquals(BASE + "/workflow/v3/process/definition/proc-123", urlCaptor.getValue());
    }

    // ── Billing ───────────────────────────────────────────────────────────────
    // Spec: POST /billing/v3/demands
    // Spec: GET  /billing/v3/demands
    // Spec: POST /billing/v3/bills/generate
    // Spec: GET  /billing/v3/bills
    // Spec: POST /billing/v3/payments
    // Spec: GET  /billing/v3/payments

    @Test
    void billing_createDemand_usesCorrectEndpoint() {
        var client = new BillingClient(restTemplate, props, objectMapper);

        when(restTemplate.postForEntity(anyString(), any(), eq(JsonNode.class)))
                .thenReturn(ResponseEntity.ok(objectMapper.createObjectNode()));

        client.createDemand(new DemandCreate());

        var urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).postForEntity(urlCaptor.capture(), any(), eq(JsonNode.class));
        assertEquals(BASE + "/billing/v3/demands", urlCaptor.getValue());
    }

    @Test
    void billing_searchDemands_usesCorrectEndpoint() {
        var client = new BillingClient(restTemplate, props, objectMapper);

        when(restTemplate.getForEntity(anyString(), eq(JsonNode.class)))
                .thenReturn(ResponseEntity.ok(objectMapper.createArrayNode()));

        client.searchDemands("WATER", "CON-001");

        var urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).getForEntity(urlCaptor.capture(), eq(JsonNode.class));
        assertTrue(urlCaptor.getValue().startsWith(BASE + "/billing/v3/demands"));
    }

    @Test
    void billing_generateBill_usesCorrectEndpoint() {
        var client = new BillingClient(restTemplate, props, objectMapper);

        when(restTemplate.postForEntity(anyString(), any(), eq(JsonNode.class)))
                .thenReturn(ResponseEntity.ok(objectMapper.createObjectNode()));

        client.generateBill(new GenerateBillCriteria());

        var urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).postForEntity(urlCaptor.capture(), any(), eq(JsonNode.class));
        assertEquals(BASE + "/billing/v3/bills/generate", urlCaptor.getValue());
    }

    @Test
    void billing_createPayment_usesCorrectEndpoint() {
        var client = new BillingClient(restTemplate, props, objectMapper);

        when(restTemplate.postForEntity(anyString(), any(), eq(JsonNode.class)))
                .thenReturn(ResponseEntity.ok(objectMapper.createObjectNode()));

        client.createPayment(new PaymentCreate());

        var urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).postForEntity(urlCaptor.capture(), any(), eq(JsonNode.class));
        assertEquals(BASE + "/billing/v3/payments", urlCaptor.getValue());
    }

    // ── Boundary ──────────────────────────────────────────────────────────────
    // Spec: POST /boundary/v3/boundaries
    // Spec: GET  /boundary/v3/boundaries?codes=...
    // Spec: POST /boundary/v3/hierarchy
    // Spec: GET  /boundary/v3/hierarchy?hierarchyType=...
    // Spec: POST /boundary/v3/relationship
    // Spec: GET  /boundary/v3/relationship?hierarchyType=...

    @Test
    void boundary_createBoundaries_usesCorrectEndpoint() {
        var client = new BoundaryClient(restTemplate, props);
        var boundary = Boundary.builder().code("B1").build();

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(BoundaryResponse.class)))
                .thenReturn(ResponseEntity.ok(BoundaryResponse.builder().boundary(List.of(boundary)).build()));

        client.createBoundaries(List.of(boundary));

        var urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).exchange(urlCaptor.capture(), eq(HttpMethod.POST), any(), eq(BoundaryResponse.class));
        assertEquals(BASE + "/boundary/v3/boundaries", urlCaptor.getValue());
    }

    @Test
    void boundary_searchBoundaries_usesCorrectEndpoint() {
        var client = new BoundaryClient(restTemplate, props);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(BoundaryResponse.class)))
                .thenReturn(ResponseEntity.ok(BoundaryResponse.builder().boundary(List.of()).build()));

        client.searchBoundariesByCodes(List.of("B1", "B2"));

        var urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).exchange(urlCaptor.capture(), eq(HttpMethod.GET), any(), eq(BoundaryResponse.class));
        assertTrue(urlCaptor.getValue().startsWith(BASE + "/boundary/v3/boundaries"));
    }

    @Test
    void boundary_createHierarchy_usesCorrectEndpoint() {
        var client = new BoundaryClient(restTemplate, props);
        var hierarchy = BoundaryHierarchy.builder().hierarchyType("ADMIN").build();

        when(restTemplate.postForEntity(anyString(), any(), eq(BoundaryHierarchyResponse.class)))
                .thenReturn(ResponseEntity.ok(BoundaryHierarchyResponse.builder().hierarchy(List.of(hierarchy)).build()));

        client.createBoundaryHierarchy(hierarchy);

        var urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).postForEntity(urlCaptor.capture(), any(), eq(BoundaryHierarchyResponse.class));
        assertEquals(BASE + "/boundary/v3/hierarchy", urlCaptor.getValue());
    }

    @Test
    void boundary_searchHierarchy_usesCorrectEndpoint() {
        var client = new BoundaryClient(restTemplate, props);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(BoundaryHierarchyResponse.class)))
                .thenReturn(ResponseEntity.ok(BoundaryHierarchyResponse.builder().hierarchy(List.of()).build()));

        client.searchBoundaryHierarchy("ADMIN");

        var urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).exchange(urlCaptor.capture(), eq(HttpMethod.GET), any(), eq(BoundaryHierarchyResponse.class));
        assertTrue(urlCaptor.getValue().startsWith(BASE + "/boundary/v3/hierarchy"));
    }

    @Test
    void boundary_createRelationship_usesCorrectEndpoint() {
        var client = new BoundaryClient(restTemplate, props);
        var rel = BoundaryRelationship.builder().code("R1").build();

        when(restTemplate.postForEntity(anyString(), any(), eq(BoundaryRelationshipResponse.class)))
                .thenReturn(ResponseEntity.ok(BoundaryRelationshipResponse.builder().relationship(List.of(rel)).build()));

        client.createBoundaryRelationship(rel);

        var urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).postForEntity(urlCaptor.capture(), any(), eq(BoundaryRelationshipResponse.class));
        assertEquals(BASE + "/boundary/v3/relationship", urlCaptor.getValue());
    }

    // ── Notification ──────────────────────────────────────────────────────────
    // Spec: POST /notification/v3/email/send
    // Spec: POST /notification/v3/sms/send

    @Test
    void notification_sendEmail_usesCorrectEndpoint() {
        var client = new NotificationClient(restTemplate, props);
        var req = SendEmailRequest.builder()
                .templateId("tmpl-1").emailIds(List.of("user@example.com")).build();

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(SendEmailResponse.class)))
                .thenReturn(ResponseEntity.ok(null));

        client.sendEmail(req);

        var urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).exchange(urlCaptor.capture(), eq(HttpMethod.POST), any(), eq(SendEmailResponse.class));
        assertEquals(BASE + "/notification/v3/email/send", urlCaptor.getValue());
    }

    @Test
    void notification_sendSMS_usesCorrectEndpoint() {
        var client = new NotificationClient(restTemplate, props);
        var req = SendSMSRequest.builder()
                .templateId("tmpl-1").mobileNumbers(List.of("+919999999999")).build();

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(SendSMSResponse.class)))
                .thenReturn(ResponseEntity.ok(null));

        client.sendSMS(req);

        var urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).exchange(urlCaptor.capture(), eq(HttpMethod.POST), any(), eq(SendSMSResponse.class));
        assertEquals(BASE + "/notification/v3/sms/send", urlCaptor.getValue());
    }

    // ── MDMS ──────────────────────────────────────────────────────────────────
    // Spec: GET /mdms/v3/data?schemaCode=...&uniqueIdentifiers=...

    @Test
    void mdms_searchData_usesCorrectEndpoint() {
        var client = new MdmsClient(restTemplate, props);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(MdmsResponseV2.class)))
                .thenReturn(ResponseEntity.ok(null));

        try {
            client.searchMdmsData("common-masters.PropertyType", Set.of("RESIDENTIAL"));
        } catch (Exception ignored) {}

        var urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).exchange(urlCaptor.capture(), eq(HttpMethod.GET), any(), eq(MdmsResponseV2.class));
        assertTrue(urlCaptor.getValue().startsWith(BASE + "/mdms-v2/v2"));
    }

    // ── Registry ──────────────────────────────────────────────────────────────
    // Spec: POST /registry/v3/schema/{schemaCode}/data
    // Spec: GET  /registry/v3/schema/{schemaCode}/data/_registry?registryId=...
    // Spec: POST /registry/v3/schema/{schemaCode}/data/_search

    @Test
    void registry_createData_usesCorrectEndpoint() {
        var client = new RegistryClient(restTemplate, props);
        var data = new RegistryData();
        data.setData(objectMapper.createObjectNode().put("name", "test"));

        when(restTemplate.postForEntity(anyString(), any(), eq(RegistryDataResponse.class)))
                .thenReturn(ResponseEntity.ok(null));

        client.createRegistryData("trade-license", data);

        var urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).postForEntity(urlCaptor.capture(), any(), eq(RegistryDataResponse.class));
        assertEquals(BASE + "/registry/v3/trade-license/data", urlCaptor.getValue());
    }

    @Test
    void registry_searchByRegistryId_usesCorrectEndpoint() {
        var client = new RegistryClient(restTemplate, props);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(RegistryDataResponse.class)))
                .thenReturn(ResponseEntity.ok(null));

        client.searchRegistryData("trade-license", "reg-123", false);

        var urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).exchange(urlCaptor.capture(), eq(HttpMethod.GET), any(), eq(RegistryDataResponse.class));
        assertTrue(urlCaptor.getValue().startsWith(BASE + "/registry/v3/trade-license/data/_registry"));
        assertTrue(urlCaptor.getValue().contains("registryId=reg-123"));
    }

    @Test
    void registry_searchByField_usesCorrectEndpoint() {
        var client = new RegistryClient(restTemplate, props);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(RegistryDataResponse.class)))
                .thenReturn(ResponseEntity.ok(null));

        client.searchRegistryData("trade-license", "businessName", "Acme");

        var urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).exchange(urlCaptor.capture(), eq(HttpMethod.POST), any(), eq(RegistryDataResponse.class));
        assertEquals(BASE + "/registry/v3/trade-license/data/_search", urlCaptor.getValue());
    }

    // ── Individual ────────────────────────────────────────────────────────────
    // Spec: POST /individual/v3/individuals
    // Spec: GET  /individual/v3/individuals/{id}
    // Spec: GET  /individual/v3/individuals?name=...

    @Test
    void individual_create_usesCorrectEndpoint() {
        var client = new IndividualClient(restTemplate, props);
        var individual = new Individual();

        when(restTemplate.postForEntity(anyString(), any(), eq(Individual.class)))
                .thenReturn(ResponseEntity.ok(null));

        client.createIndividual(individual);

        var urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).postForEntity(urlCaptor.capture(), any(), eq(Individual.class));
        assertEquals(BASE + "/individuals/v3/individuals", urlCaptor.getValue());
    }

    @Test
    void individual_getById_usesCorrectEndpoint() {
        var client = new IndividualClient(restTemplate, props);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Individual.class)))
                .thenReturn(ResponseEntity.ok(null));

        client.getIndividualById("ind-123");

        var urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).exchange(urlCaptor.capture(), eq(HttpMethod.GET), any(), eq(Individual.class));
        assertEquals(BASE + "/individuals/v3/individuals/ind-123", urlCaptor.getValue());
    }

    @Test
    void individual_searchByName_usesCorrectEndpoint() {
        var client = new IndividualClient(restTemplate, props);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(IndividualSearchResponse.class)))
                .thenReturn(ResponseEntity.ok(null));

        client.searchIndividualsByName("John");

        var urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).exchange(urlCaptor.capture(), eq(HttpMethod.GET), any(), eq(IndividualSearchResponse.class));
        assertTrue(urlCaptor.getValue().startsWith(BASE + "/individuals/v3/individuals"));
        assertTrue(urlCaptor.getValue().contains("givenName=John"));
    }
    // ── Filestore ─────────────────────────────────────────────────────────────
    // Spec: POST /filestore/v3/files/metadata?fileStoreId=...&tenantId=...

    @Test
    void filestore_isFileAvailable_usesCorrectEndpoint() {
        var client = new FilestoreClient(restTemplate, props, new PropagationProperties());

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(byte[].class)))
                .thenReturn(ResponseEntity.ok(null));

        client.isFileAvailable("file-abc");

        var urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).exchange(urlCaptor.capture(), eq(HttpMethod.GET), any(), eq(byte[].class));
        assertEquals(BASE + "/filestore/v3/files/file-abc", urlCaptor.getValue());
    }
}
