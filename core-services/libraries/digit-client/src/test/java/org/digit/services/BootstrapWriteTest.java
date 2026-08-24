package org.digit.services;

import tools.jackson.databind.ObjectMapper;
import org.digit.config.ApiProperties;
import org.digit.services.billing.BillingClient;
import org.digit.services.billing.model.BusinessServiceCreate;
import org.digit.services.billing.model.CollectionMode;
import org.digit.services.billing.model.PaymentMode;
import org.digit.services.billing.model.TaxHeadCreate;
import org.digit.services.idgen.IdGenClient;
import org.digit.services.idgen.model.IdGenTemplate;
import org.digit.services.idgen.model.SequenceScope;
import org.digit.services.idgen.model.TemplateConfig;
import org.digit.services.registry.model.RegistrySchemaRequest;
import org.digit.services.workflow.WorkflowClient;
import org.digit.services.workflow.model.WorkflowActionDefinition;
import org.digit.services.workflow.model.WorkflowProcessDefinitionRequest;
import org.digit.services.workflow.model.WorkflowStateDefinition;
import org.digit.util.DigitJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** The bootstrap write surfaces: request shapes, and the validation that runs before sending. */
@ExtendWith(MockitoExtension.class)
class BootstrapWriteTest {

    private static final String BASE = "http://localhost:8080";

    @Mock RestTemplate restTemplate;

    ApiProperties props;
    final ObjectMapper mapper = DigitJson.mapper();

    @BeforeEach
    void setup() {
        props = new ApiProperties();
        props.setIdgenServiceUrl(BASE);
        props.setWorkflowServiceUrl(BASE);
        props.setBillingServiceUrl(BASE);
    }

    // ── idgen ────────────────────────────────────────────────────────────────

    @Test
    void idgenTemplate_serializesPaddingCharacterAsChar() throws Exception {
        IdGenTemplate template = IdGenTemplate.builder()
                .templateCode("PT-ID")
                .config(TemplateConfig.builder()
                        .template("PT-{seq}")
                        .sequence(TemplateConfig.Sequence.builder()
                                .scope(SequenceScope.YEARLY)
                                .start(1)
                                .padding(TemplateConfig.Padding.builder().length(5).character("0").build())
                                .build())
                        .build())
                .build();

        String json = mapper.writeValueAsString(template);

        // The service's field is "character" but its wire name is the short form.
        assertTrue(json.contains("\"char\":\"0\""), json);
        assertFalse(json.contains("\"character\""), json);
        assertTrue(json.contains("\"scope\":\"YEARLY\""), json);
    }

    @Test
    void idgenBulk_enforcesTheServiceCeiling() {
        var client = new IdGenClient(restTemplate, props);
        assertThrows(RuntimeException.class, () -> client.generateIds("PT-ID", 0));
        assertThrows(RuntimeException.class, () -> client.generateIds("PT-ID", 1001));
    }

    @Test
    void idgenDeleteTemplate_requiresAVersion() {
        var client = new IdGenClient(restTemplate, props);
        // Delete removes a single version, so omitting it would be ambiguous.
        assertThrows(RuntimeException.class, () -> client.deleteTemplate("PT-ID", null));
    }

    @Test
    void idgenTemplateSearch_requiresACodeAlongsideAVersion() {
        var client = new IdGenClient(restTemplate, props);
        assertThrows(RuntimeException.class,
                () -> client.searchTemplates(null, "v2", null, null, null));
    }

    // ── workflow ─────────────────────────────────────────────────────────────

    @Test
    void workflowDefinition_postsNestedStatesAndActions() throws Exception {
        var client = new WorkflowClient(restTemplate, props);
        when(restTemplate.postForEntity(anyString(), any(), any(Class.class)))
                .thenReturn(ResponseEntity.ok(null));

        client.createProcessDefinition(validDefinition());

        var urlCaptor = ArgumentCaptor.forClass(String.class);
        var bodyCaptor = ArgumentCaptor.forClass(Object.class);
        verify(restTemplate).postForEntity(urlCaptor.capture(), bodyCaptor.capture(), any(Class.class));
        assertEquals(BASE + "/workflow/v3/process/definition", urlCaptor.getValue());

        String json = mapper.writeValueAsString(bodyCaptor.getValue());
        assertTrue(json.contains("\"states\""), json);
        assertTrue(json.contains("\"actions\""), json);
        // On the way in, nextState is a state code; the response returns ids instead.
        assertTrue(json.contains("\"nextState\":\"APPROVED\""), json);
    }

    @Test
    void workflowDefinition_rejectsWhatTheServiceWouldReject() {
        var client = new WorkflowClient(restTemplate, props);

        // No states.
        assertThrows(RuntimeException.class, () -> client.createProcessDefinition(
                WorkflowProcessDefinitionRequest.builder().code("NOC").name("NOC").build()));

        // Two INITIAL states.
        WorkflowProcessDefinitionRequest twoInitials = validDefinition();
        twoInitials.getStates().get(1).setType("INITIAL");
        assertThrows(RuntimeException.class, () -> client.createProcessDefinition(twoInitials));

        // An unsupported state type.
        WorkflowProcessDefinitionRequest badType = validDefinition();
        badType.getStates().get(0).setType("STARTED");
        assertThrows(RuntimeException.class, () -> client.createProcessDefinition(badType));

        // An action pointing at a state that was never declared.
        WorkflowProcessDefinitionRequest danglingAction = validDefinition();
        danglingAction.getStates().get(0).getActions().get(0).setNextState("NOWHERE");
        assertThrows(RuntimeException.class, () -> client.createProcessDefinition(danglingAction));

        // Duplicate state codes.
        WorkflowProcessDefinitionRequest duplicateStates = validDefinition();
        duplicateStates.getStates().get(1).setCode("SUBMITTED");
        assertThrows(RuntimeException.class, () -> client.createProcessDefinition(duplicateStates));
    }

    // ── billing ──────────────────────────────────────────────────────────────

    @Test
    void billingCatalogue_postsArraysAndSerializesOrderCorrectly() throws Exception {
        var client = new BillingClient(restTemplate, props, mapper);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), any(Class.class)))
                .thenReturn(ResponseEntity.ok(mapper.createArrayNode()));

        client.createTaxHeads(List.of(TaxHeadCreate.builder()
                .code("PT_TAX").name("Property Tax").businessServiceCode("PT")
                .order(1).effectiveFrom(1700000000000L).isActive(true).build()));

        var urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).exchange(urlCaptor.capture(), eq(HttpMethod.POST), any(), any(Class.class));
        assertEquals(BASE + "/billing/v3/tax-heads", urlCaptor.getValue());

        // The service names this "order"; the Java field it binds to is orderNumber.
        String json = mapper.writeValueAsString(List.of(TaxHeadCreate.builder().order(1).build()));
        assertTrue(json.startsWith("["), json);
        assertTrue(json.contains("\"order\":1"), json);
        assertFalse(json.contains("orderNumber"), json);
    }

    @Test
    void billingBusinessService_requiresTheFieldsTheServiceInsistsOn() {
        var client = new BillingClient(restTemplate, props, mapper);

        // Missing currency, billExpiryDays, effectiveFrom and isActive in turn.
        assertThrows(RuntimeException.class, () -> client.createBusinessService(
                BusinessServiceCreate.builder().code("PT").name("Property Tax")
                        .allowedPaymentModes(List.of(PaymentMode.CASH))
                        .billExpiryDays(30).effectiveFrom(1L).isActive(true).build()));

        assertThrows(RuntimeException.class, () -> client.createBusinessService(
                BusinessServiceCreate.builder().code("PT").name("Property Tax").currency("INR")
                        .allowedPaymentModes(List.of(PaymentMode.CASH))
                        .effectiveFrom(1L).isActive(true).build()));

        // An empty payment-mode list.
        assertThrows(RuntimeException.class, () -> client.createBusinessService(
                BusinessServiceCreate.builder().code("PT").name("Property Tax").currency("INR")
                        .allowedPaymentModes(List.of()).billExpiryDays(30)
                        .effectiveFrom(1L).isActive(true).build()));

        // effectiveTo not after effectiveFrom.
        assertThrows(RuntimeException.class, () -> client.createBusinessService(
                BusinessServiceCreate.builder().code("PT").name("Property Tax").currency("INR")
                        .collectionMode(CollectionMode.BOTH)
                        .allowedPaymentModes(List.of(PaymentMode.CASH)).billExpiryDays(30)
                        .effectiveFrom(200L).effectiveTo(100L).isActive(true).build()));
    }

    // ── registry ─────────────────────────────────────────────────────────────

    @Test
    void registrySchemaRequest_sendsHyphenatedExtensionKeys() throws Exception {
        RegistrySchemaRequest request = RegistrySchemaRequest.builder()
                .schemaCode("Trade.License")
                .definition(mapper.createObjectNode().put("type", "object"))
                .xUnique(List.of(List.of("licenceNumber")))
                .build();

        String json = mapper.writeValueAsString(request);

        // Writes are hyphenated; reads come back camelCase, which is why the two shapes differ.
        assertTrue(json.contains("\"x-unique\""), json);
        assertFalse(json.contains("\"xUnique\""), json);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private static WorkflowProcessDefinitionRequest validDefinition() {
        return WorkflowProcessDefinitionRequest.builder()
                .code("NOC")
                .name("NOC flow")
                .sla(86400000L)
                .additionalDetails(Map.of("owner", "revenue"))
                .states(new java.util.ArrayList<>(List.of(
                        WorkflowStateDefinition.builder()
                                .code("SUBMITTED").name("Submitted").type("INITIAL")
                                .actions(new java.util.ArrayList<>(List.of(
                                        WorkflowActionDefinition.builder()
                                                .code("APPROVE").label("Approve")
                                                .nextState("APPROVED")
                                                .roles(List.of("APPROVER"))
                                                .build())))
                                .build(),
                        WorkflowStateDefinition.builder()
                                .code("APPROVED").name("Approved").type("TERMINAL_SUCCESS")
                                .build())))
                .build();
    }
}
