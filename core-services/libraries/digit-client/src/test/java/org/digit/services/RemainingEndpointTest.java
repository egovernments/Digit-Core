package org.digit.services;

import tools.jackson.databind.ObjectMapper;
import org.digit.config.ApiProperties;
import org.digit.services.billing.BillingClient;
import org.digit.services.billing.model.BillStatus;
import org.digit.services.billing.model.BulkBillRequest;
import org.digit.services.billing.model.BusinessServicePatch;
import org.digit.services.billing.model.TaxHeadPatch;
import org.digit.services.billing.model.UpdateBillStatus;
import org.digit.services.notification.NotificationClient;
import org.digit.services.notification.model.Template;
import org.digit.services.notification.model.TemplatePreviewRequest;
import org.digit.services.notification.model.TemplatePreviewResponse;
import org.digit.services.notification.model.TemplateRequest;
import org.digit.services.notification.model.TemplateSearchCriteria;
import org.digit.services.workflow.WorkflowClient;
import org.digit.services.workflow.model.TransitionCountResponse;
import org.digit.services.workflow.model.WorkflowActionDefinition;
import org.digit.services.workflow.model.WorkflowEscalationConfig;
import org.digit.services.workflow.model.WorkflowProcessInstanceListResponse;
import org.digit.services.workflow.model.WorkflowState;
import org.digit.services.workflow.model.WorkflowStateDefinition;
import org.digit.services.workflow.model.WorkflowTransitionSearchCriteria;
import org.digit.util.DigitJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** The endpoints filled in after the coverage audit: billing CRUD, notification templates, workflow. */
@ExtendWith(MockitoExtension.class)
class RemainingEndpointTest {

    private static final String BASE = "http://localhost:8080";

    @Mock RestTemplate restTemplate;

    ApiProperties props;
    final ObjectMapper mapper = DigitJson.mapper();

    @BeforeEach
    void setup() {
        props = new ApiProperties();
        props.setBillingServiceUrl(BASE);
        props.setNotificationServiceUrl(BASE);
        props.setWorkflowServiceUrl(BASE);
    }

    // ── billing ──────────────────────────────────────────────────────────────

    @Test
    void billing_patchesByCodeNotById() {
        var client = new BillingClient(restTemplate, props, mapper);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.PATCH), any(), any(Class.class)))
                .thenReturn(ResponseEntity.ok(mapper.createObjectNode()));

        client.patchTaxHead("PT_TAX", TaxHeadPatch.builder().name("Property Tax").build());

        var url = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).exchange(url.capture(), eq(HttpMethod.PATCH), any(), any(Class.class));
        assertEquals(BASE + "/billing/v3/tax-heads/PT_TAX", url.getValue());
    }

    @Test
    void billing_businessServicePatchOmitsPartialPaymentAllowed() throws Exception {
        // The service's patch record has no such component, so sending it would be rejected.
        String json = mapper.writeValueAsString(
                BusinessServicePatch.builder().name("Property Tax").billExpiryDays(15).build());
        assertTrue(json.contains("\"billExpiryDays\":15"), json);
        assertTrue(!json.contains("partialPaymentAllowed"), json);
    }

    @Test
    void billing_bulkGenerate_requiresABusinessService() {
        var client = new BillingClient(restTemplate, props, mapper);
        assertThrows(RuntimeException.class,
                () -> client.bulkGenerateBills(BulkBillRequest.builder().build()));
    }

    @Test
    void billing_cancelBills_requiresTheTargetStatus() {
        var client = new BillingClient(restTemplate, props, mapper);
        assertThrows(RuntimeException.class, () -> client.cancelBills(
                UpdateBillStatus.builder().businessServiceCode("PT").consumerCode("C-1").build()));

        when(restTemplate.postForEntity(anyString(), any(), any(Class.class)))
                .thenReturn(ResponseEntity.ok(mapper.createArrayNode()));
        client.cancelBills(UpdateBillStatus.builder()
                .businessServiceCode("PT").consumerCode("C-1")
                .statusToBeUpdated(BillStatus.CANCELLED).metadata(Map.of()).build());

        var url = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).postForEntity(url.capture(), any(), any(Class.class));
        assertEquals(BASE + "/billing/v3/bills/cancel", url.getValue());
    }

    // ── notification ─────────────────────────────────────────────────────────

    @Test
    void notification_templateSearch_sendsIdsAsOneCommaSeparatedParam() {
        var client = new NotificationClient(restTemplate, props);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(),
                any(ParameterizedTypeReference.class))).thenReturn(ResponseEntity.ok(List.of()));

        client.searchTemplates(TemplateSearchCriteria.builder()
                .ids(List.of("t-1", "t-2")).type("EMAIL").build());

        var url = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).exchange(url.capture(), eq(HttpMethod.GET), any(),
                any(ParameterizedTypeReference.class));
        // One parameter carrying both ids, not a repeated parameter.
        assertTrue(url.getValue().contains("ids=t-1,t-2"), url.getValue());
        assertTrue(url.getValue().contains("type=EMAIL"), url.getValue());
    }

    @Test
    void notification_preview_postsToThePreviewPath() {
        var client = new NotificationClient(restTemplate, props);
        when(restTemplate.postForEntity(anyString(), any(), eq(TemplatePreviewResponse.class)))
                .thenReturn(ResponseEntity.ok(null));

        client.previewTemplate(TemplatePreviewRequest.builder()
                .templateId("welcome").payload(Map.of("name", "Asha")).build());

        var url = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).postForEntity(url.capture(), any(), eq(TemplatePreviewResponse.class));
        assertEquals(BASE + "/notification/v3/template/preview", url.getValue());
    }

    @Test
    void notification_deleteTemplate_needsBothIdAndVersion() {
        var client = new NotificationClient(restTemplate, props);
        // Delete removes one version, so a bare id would be ambiguous.
        assertThrows(RuntimeException.class, () -> client.deleteTemplate("welcome", null));
    }

    @Test
    void notification_createTemplate_requiresTypeAndContent() {
        var client = new NotificationClient(restTemplate, props);
        assertThrows(RuntimeException.class, () -> client.createTemplate(
                TemplateRequest.builder().templateId("welcome").build()));
    }

    @Test
    void notification_responseCarriesTemplateIdAndVersion() throws Exception {
        // These were missing from the SDK's response models, so they silently vanished.
        var response = mapper.readValue(
                "{\"templateId\":\"welcome\",\"version\":\"v2\",\"status\":\"SENT\"}",
                org.digit.services.notification.model.SendEmailResponse.class);
        assertEquals("welcome", response.getTemplateId());
        assertEquals("v2", response.getVersion());
        assertEquals("SENT", response.getStatus());
    }

    // ── workflow ─────────────────────────────────────────────────────────────

    @Test
    void workflow_stateAndActionPathsNest() {
        var client = new WorkflowClient(restTemplate, props);
        when(restTemplate.postForEntity(anyString(), any(), eq(WorkflowState.class)))
                .thenReturn(ResponseEntity.ok(null));

        client.createState("NOC", WorkflowStateDefinition.builder()
                .code("SUBMITTED").name("Submitted").type("INITIAL").build());

        var url = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).postForEntity(url.capture(), any(), eq(WorkflowState.class));
        assertEquals(BASE + "/workflow/v3/process/NOC/state", url.getValue());
    }

    @Test
    void workflow_escalationConfigPathIncludesTheState() {
        var client = new WorkflowClient(restTemplate, props);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(), eq(WorkflowEscalationConfig.class)))
                .thenReturn(ResponseEntity.ok(null));

        client.updateEscalationConfig("NOC", "SUBMITTED", WorkflowEscalationConfig.builder()
                .stateCode("SUBMITTED").escalationAction("REMIND").stateSlaMinutes(120).build());

        var url = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).exchange(url.capture(), eq(HttpMethod.PUT), any(),
                eq(WorkflowEscalationConfig.class));
        assertEquals(BASE + "/workflow/v3/process/NOC/escalation/SUBMITTED", url.getValue());
    }

    @Test
    void workflow_transitionSearch_rejectsMutuallyExclusiveFilters() {
        var client = new WorkflowClient(restTemplate, props);
        // The service allows at most one of entityId, currentState, assignee, escalated.
        assertThrows(RuntimeException.class, () -> client.searchTransitions(
                WorkflowTransitionSearchCriteria.builder()
                        .entityId("e-1").assignee("u-1").build()));
    }

    @Test
    void workflow_transitionCount_requiresProcessCodeWithVersion() {
        var client = new WorkflowClient(restTemplate, props);
        assertThrows(RuntimeException.class, () -> client.countTransitions("STATE",
                WorkflowTransitionSearchCriteria.builder().version("1").build()));
    }

    @Test
    void workflow_transitionCount_buildsTheCountUrl() {
        var client = new WorkflowClient(restTemplate, props);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(TransitionCountResponse.class)))
                .thenReturn(ResponseEntity.ok(null));

        client.countTransitions("STATE", WorkflowTransitionSearchCriteria.builder()
                .processCode("NOC").version("1").build());

        var url = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).exchange(url.capture(), eq(HttpMethod.GET), any(),
                eq(TransitionCountResponse.class));
        assertTrue(url.getValue().startsWith(BASE + "/workflow/v3/transition/count?"), url.getValue());
        assertTrue(url.getValue().contains("countType=STATE"), url.getValue());
    }

    @Test
    void workflow_autoEscalate_targetsTheProcess() {
        var client = new WorkflowClient(restTemplate, props);
        when(restTemplate.postForEntity(anyString(), any(), eq(WorkflowProcessInstanceListResponse.class)))
                .thenReturn(ResponseEntity.ok(null));

        client.escalateNow("NOC");

        var url = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).postForEntity(url.capture(), any(),
                eq(WorkflowProcessInstanceListResponse.class));
        assertEquals(BASE + "/workflow/v3/auto/NOC/_escalate", url.getValue());
    }

    @Test
    void workflow_actionCreate_requiresNextState() {
        var client = new WorkflowClient(restTemplate, props);
        assertThrows(RuntimeException.class, () -> client.createAction("NOC", "SUBMITTED",
                WorkflowActionDefinition.builder().code("APPROVE").build()));
    }

    @Test
    void workflow_escalationSlaIsMinutes() throws Exception {
        // Unlike process and state SLAs, which are milliseconds.
        String json = mapper.writeValueAsString(WorkflowEscalationConfig.builder()
                .stateCode("SUBMITTED").stateSlaMinutes(120).processSlaMinutes(1440).build());
        assertTrue(json.contains("\"stateSlaMinutes\":120"), json);
        assertTrue(json.contains("\"processSlaMinutes\":1440"), json);
    }

    @Test
    void workflow_systemTransition_usesItsOwnPath() {
        var client = new WorkflowClient(restTemplate, props);
        when(restTemplate.postForEntity(anyString(), any(),
                eq(org.digit.services.workflow.model.WorkflowTransitionResponse.class)))
                .thenReturn(ResponseEntity.ok(null));

        client.executeSystemTransition(org.digit.services.workflow.model.WorkflowTransitionRequest.builder()
                .processCode("NOC").entityId("e-1").action("AUTO_APPROVE").build());

        var url = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).postForEntity(url.capture(), any(),
                eq(org.digit.services.workflow.model.WorkflowTransitionResponse.class));
        assertEquals(BASE + "/workflow/v3/system/transition", url.getValue());
    }
}
