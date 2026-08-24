package org.digit.live;

import org.digit.services.workflow.WorkflowClient;
import org.digit.services.workflow.model.WorkflowProcessResponse;
import org.digit.services.workflow.model.WorkflowStateDetail;
import org.digit.services.workflow.model.WorkflowTransitionSearchCriteria;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Every read endpoint on WorkflowClient.
 *
 * <p>The by-code reads chain off the list read rather than hardcoding a process code, so the suite
 * works against any tenant's data and a rename in the environment cannot make it quietly vacuous.
 */
class LiveWorkflowReadTest extends LiveReadSupport {

    private final WorkflowClient client = new WorkflowClient(LiveEnv.restTemplate(), LiveEnv.properties());

    @Override
    String service() {
        return "workflow";
    }

    @Test
    void listProcessDefinitions() {
        List<WorkflowProcessResponse> definitions = client.listProcessDefinitions();
        assertNotNull(definitions);
        assertKeptEveryField(definitions);
    }

    @Test
    void getProcessDefinitionByCode() {
        String code = firstProcessCode();
        WorkflowProcessResponse definition = client.getProcessDefinition(code);
        assertNotNull(definition);
        assertEquals(code, definition.getCode());
        assertKeptEveryField(definition);
    }

    @Test
    void getProcessDefinitionByCodeAndVersion() {
        WorkflowProcessResponse latest = client.getProcessDefinition(firstProcessCode());
        assumeTrue(latest.getVersion() != null, "process has no version to pin");

        WorkflowProcessResponse pinned =
                client.getProcessDefinition(latest.getCode(), latest.getVersion());
        assertNotNull(pinned);
        assertEquals(latest.getVersion(), pinned.getVersion());
        assertKeptEveryField(pinned);
    }

    @Test
    void getProcessByCode() {
        String code = firstProcessCode();
        var process = client.getProcessByCode(code);
        assertNotNull(process);
        assertKeptEveryField(process);
    }

    @Test
    void searchProcesses() {
        var processes = client.searchProcesses(null, null, null);
        assertNotNull(processes);
        assertKeptEveryField(processes);
    }

    @Test
    void listStates() {
        var states = client.listStates(firstProcessCode());
        assertNotNull(states);
        assertKeptEveryField(states);
    }

    @Test
    void getState() {
        String code = firstProcessCode();
        String stateCode = firstStateCode(code);
        var state = client.getState(code, stateCode);
        assertNotNull(state);
        assertEquals(stateCode, state.getCode());
        assertKeptEveryField(state);
    }

    @Test
    void listActions() {
        String code = firstProcessCode();
        var actions = client.listActions(code, firstStateCode(code));
        assertNotNull(actions);
        assertKeptEveryField(actions);
    }

    @Test
    void getAction() {
        String code = firstProcessCode();
        String stateCode = firstStateCode(code);
        var actions = client.listActions(code, stateCode);
        assumeTrue(actions != null && !actions.isEmpty(), "state has no actions");
        String actionCode = actions.get(0).getCode();

        var action = client.getAction(code, stateCode, actionCode);
        assertNotNull(action);
        assertEquals(actionCode, action.getCode());
        assertKeptEveryField(action);
    }

    @Test
    void listEscalationConfigs() {
        var configs = client.listEscalationConfigs(firstProcessCode());
        assertNotNull(configs);
        assertKeptEveryField(configs);
    }

    @Test
    void searchEscalatable() {
        var response = client.searchEscalatable(firstProcessCode(), 1, 5);
        assertNotNull(response);
        assertKeptEveryField(response);
    }

    @Test
    void searchTransitions() {
        var response = client.searchTransitions(WorkflowTransitionSearchCriteria.builder()
                .processCode(firstProcessCode())
                .build());
        assertNotNull(response);
        assertKeptEveryField(response);
    }

    // ── chaining helpers ─────────────────────────────────────────────────────

    private String firstProcessCode() {
        List<WorkflowProcessResponse> definitions = client.listProcessDefinitions();
        assumeTrue(definitions != null && !definitions.isEmpty(), "no process definitions here");
        String code = definitions.get(0).getCode();
        assumeTrue(code != null, "first process definition has no code");
        return code;
    }

    private String firstStateCode(String processCode) {
        WorkflowProcessResponse definition = client.getProcessDefinition(processCode);
        List<WorkflowStateDetail> states = definition.getStates();
        assumeTrue(states != null && !states.isEmpty(), "process has no states");
        return states.get(0).getCode();
    }
}
