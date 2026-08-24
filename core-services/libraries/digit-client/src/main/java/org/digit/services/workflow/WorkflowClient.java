package org.digit.services.workflow;

import org.digit.config.ApiProperties;
import org.digit.exception.DigitClientException;
import org.digit.services.workflow.model.WorkflowActionDefinition;
import org.digit.services.workflow.model.WorkflowProcessDefinitionListResponse;
import org.digit.services.workflow.model.WorkflowProcessDefinitionRequest;
import org.digit.services.workflow.model.WorkflowAction;
import org.digit.services.workflow.model.WorkflowEscalationConfig;
import org.digit.services.workflow.model.WorkflowProcess;
import org.digit.services.workflow.model.WorkflowProcessInstance;
import org.digit.services.workflow.model.WorkflowProcessInstanceListResponse;
import org.digit.services.workflow.model.WorkflowProcessRequest;
import org.digit.services.workflow.model.WorkflowTransitionSearchCriteria;
import org.digit.services.workflow.model.TransitionCountResponse;
import org.digit.services.workflow.model.WorkflowProcessResponse;
import org.digit.services.workflow.model.WorkflowStateDefinition;
import org.digit.services.workflow.model.WorkflowState;
import org.digit.services.workflow.model.WorkflowTransitionRequest;
import org.digit.services.workflow.model.WorkflowTransitionResponse;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class WorkflowClient {
    private static final ParameterizedTypeReference<List<WorkflowProcess>> PROCESS_LIST =
            new ParameterizedTypeReference<List<WorkflowProcess>>() {};
    private static final ParameterizedTypeReference<List<WorkflowAction>> ACTION_LIST =
            new ParameterizedTypeReference<List<WorkflowAction>>() {};
    private static final ParameterizedTypeReference<List<WorkflowEscalationConfig>> ESCALATION_LIST =
            new ParameterizedTypeReference<List<WorkflowEscalationConfig>>() {};
    private static final ParameterizedTypeReference<List<WorkflowProcessInstance>> PROCESS_INSTANCE_LIST =
            new ParameterizedTypeReference<List<WorkflowProcessInstance>>() {};
    private static final ParameterizedTypeReference<Map<String, Object>> DELETED_RESULT =
            new ParameterizedTypeReference<Map<String, Object>>() {};

    /** The state types the service accepts. */
    private static final Set<String> VALID_STATE_TYPES =
            Set.of("INITIAL", "INTERMEDIATE", "DECISION", "TERMINAL_SUCCESS", "TERMINAL_FAILURE");

    private final RestTemplate restTemplate;
    private final ApiProperties apiProperties;

    public WorkflowClient(RestTemplate restTemplate, ApiProperties apiProperties) {
        this.restTemplate = restTemplate;
        this.apiProperties = apiProperties;
    }

    public WorkflowTransitionResponse executeTransition(WorkflowTransitionRequest transitionRequest) {
        if (transitionRequest == null) {
            throw new DigitClientException("WorkflowTransitionRequest cannot be null");
        }
        if (transitionRequest.getProcessCode() == null || transitionRequest.getProcessCode().trim().isEmpty()) {
            throw new DigitClientException("Process code cannot be null or empty");
        }
        if (transitionRequest.getEntityId() == null || transitionRequest.getEntityId().trim().isEmpty()) {
            throw new DigitClientException("Entity ID cannot be null or empty");
        }
        if (transitionRequest.getAction() == null || transitionRequest.getAction().trim().isEmpty()) {
            throw new DigitClientException("Action cannot be null or empty");
        }
        try {
            log.debug("Executing workflow transition for processCode: {}, entityId: {}, action: {}", transitionRequest.getProcessCode(), transitionRequest.getEntityId(), transitionRequest.getAction());
            String url = this.apiProperties.getWorkflowServiceUrl() + "/workflow/v3/transition";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            ResponseEntity response = this.restTemplate.exchange(url, HttpMethod.POST, new HttpEntity(transitionRequest, headers), WorkflowTransitionResponse.class);
            WorkflowTransitionResponse transitionResponse = (WorkflowTransitionResponse)response.getBody();
            log.debug("Successfully executed workflow transition. Response ID: {}", (transitionResponse != null ? transitionResponse.getId() : "null"));
            return transitionResponse;
        }
        catch (Exception e) {
            throw DigitClientException.wrap("Failed to execute workflow transition", e);
        }
    }

    public WorkflowTransitionResponse executeTransition(String processCode, String entityId, String action, String comment) {
        WorkflowTransitionRequest request = WorkflowTransitionRequest.builder().processCode(processCode).entityId(entityId).action(action).comment(comment).build();
        return this.executeTransition(request);
    }

    public WorkflowTransitionResponse executeTransition(String processCode, String entityId, String action, String comment, Map<String, List<String>> attributes) {
        WorkflowTransitionRequest request = WorkflowTransitionRequest.builder().processCode(processCode).entityId(entityId).action(action).comment(comment).attributes(attributes).build();
        return this.executeTransition(request);
    }

    public List<WorkflowState> listStates(String processCode) {
        if (processCode == null || processCode.trim().isEmpty()) {
            throw new DigitClientException("Process code cannot be null or empty");
        }
        try {
            log.debug("Retrieving states for processCode: {}", processCode);
            String url = this.apiProperties.getWorkflowServiceUrl() + "/workflow/v3/process/" + processCode + "/state";
            ResponseEntity<List<WorkflowState>> response = this.restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), new ParameterizedTypeReference<List<WorkflowState>>(){});
            return response.getBody();
        }
        catch (Exception e) {
            throw DigitClientException.wrap("Failed to retrieve states", e);
        }
    }

    public WorkflowProcessResponse getProcessDefinition(String processCode) {
        if (processCode == null || processCode.trim().isEmpty()) {
            throw new DigitClientException("Process code cannot be null or empty");
        }
        try {
            log.debug("Retrieving workflow process definition with code: {}", processCode);
            String url = this.apiProperties.getWorkflowServiceUrl() + "/workflow/v3/process/definition/" + processCode;
            ResponseEntity response = this.restTemplate.exchange(url, HttpMethod.GET, new HttpEntity(new HttpHeaders()), WorkflowProcessResponse.class);
            log.debug("Successfully retrieved workflow process definition: {}", processCode);
            return (WorkflowProcessResponse)response.getBody();
        }
        catch (Exception e) {
            throw DigitClientException.wrap("Failed to retrieve workflow process definition", e);
        }
    }

    // ── Process definitions ──────────────────────────────────────────────────

    /**
     * Creates a process definition together with its states and actions.
     *
     * <p>Validated locally first, against the same rules the service applies, so a malformed
     * definition fails with a specific message instead of a generic rejection.
     */
    public WorkflowProcessResponse createProcessDefinition(WorkflowProcessDefinitionRequest definition) {
        validateDefinition(definition);
        ResponseEntity<WorkflowProcessResponse> response = this.restTemplate.postForEntity(
                definitionUrl(), definition, WorkflowProcessResponse.class);
        return response.getBody();
    }

    /** Replaces a process definition. */
    public WorkflowProcessResponse updateProcessDefinition(String processCode,
                                                            WorkflowProcessDefinitionRequest definition) {
        if (processCode == null || processCode.isBlank()) {
            throw new DigitClientException("processCode is required");
        }
        validateDefinition(definition);
        ResponseEntity<WorkflowProcessResponse> response = this.restTemplate.exchange(
                definitionUrl() + "/" + processCode, HttpMethod.PUT,
                new HttpEntity<>(definition), WorkflowProcessResponse.class);
        return response.getBody();
    }

    /**
     * Every process definition for the tenant.
     *
     * <p>A separate method from {@link #getProcessDefinition} because the list route wraps its
     * results while the by-code route returns a definition bare.
     */
    public List<WorkflowProcessResponse> listProcessDefinitions() {
        ResponseEntity<WorkflowProcessDefinitionListResponse> response = this.restTemplate.exchange(
                definitionUrl(), HttpMethod.GET, new HttpEntity<>(new HttpHeaders()),
                WorkflowProcessDefinitionListResponse.class);
        WorkflowProcessDefinitionListResponse body = response.getBody();
        return body == null || body.getDefinitions() == null ? List.of() : body.getDefinitions();
    }

    /** A specific version of a process definition. */
    public WorkflowProcessResponse getProcessDefinition(String processCode, String version) {
        if (processCode == null || processCode.isBlank()) {
            throw new DigitClientException("processCode is required");
        }
        String url = definitionUrl() + "/" + processCode
                + (version == null || version.isBlank() ? "" : "?version=" + version);
        ResponseEntity<WorkflowProcessResponse> response = this.restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), WorkflowProcessResponse.class);
        return response.getBody();
    }

    /** Deletes a process definition. */
    public boolean deleteProcessDefinition(String processCode) {
        if (processCode == null || processCode.isBlank()) {
            throw new DigitClientException("processCode is required");
        }
        ResponseEntity<Map<String, Object>> response = this.restTemplate.exchange(
                definitionUrl() + "/" + processCode, HttpMethod.DELETE,
                new HttpEntity<>(new HttpHeaders()),
                new ParameterizedTypeReference<Map<String, Object>>() {});
        return response.getBody() != null && Boolean.TRUE.equals(response.getBody().get("deleted"));
    }

    private String definitionUrl() {
        return this.apiProperties.getWorkflowServiceUrl() + "/workflow/v3/process/definition";
    }

    /**
     * Applies the service's own structural rules before sending: a definition that breaks any of
     * these is rejected server-side, and finding out locally names the offending state or action.
     */
    private static void validateDefinition(WorkflowProcessDefinitionRequest definition) {
        if (definition == null || isBlank(definition.getCode()) || isBlank(definition.getName())) {
            throw new DigitClientException("process code and name are required");
        }
        List<WorkflowStateDefinition> states = definition.getStates();
        if (states == null || states.isEmpty()) {
            throw new DigitClientException("at least one state is required");
        }
        Set<String> stateCodes = new LinkedHashSet<>();
        int initialCount = 0;
        for (WorkflowStateDefinition state : states) {
            if (state == null || isBlank(state.getCode()) || isBlank(state.getName()) || isBlank(state.getType())) {
                throw new DigitClientException("state code, name and type are required");
            }
            if (!VALID_STATE_TYPES.contains(state.getType())) {
                throw new DigitClientException("state " + state.getCode() + " has unsupported type "
                        + state.getType() + "; expected one of " + VALID_STATE_TYPES);
            }
            if (!stateCodes.add(state.getCode())) {
                throw new DigitClientException("duplicate state code " + state.getCode());
            }
            if ("INITIAL".equals(state.getType())) {
                ++initialCount;
            }
        }
        if (initialCount != 1) {
            throw new DigitClientException("exactly one state must have type INITIAL, found " + initialCount);
        }
        for (WorkflowStateDefinition state : states) {
            if (state.getActions() == null) {
                continue;
            }
            Set<String> actionCodes = new LinkedHashSet<>();
            for (WorkflowActionDefinition action : state.getActions()) {
                if (action == null || isBlank(action.getCode()) || isBlank(action.getNextState())) {
                    throw new DigitClientException("action code and nextState are required in state " + state.getCode());
                }
                if (!actionCodes.add(action.getCode())) {
                    throw new DigitClientException("duplicate action code " + action.getCode()
                            + " in state " + state.getCode());
                }
                // nextState is a state *code* on the way in; an unknown one is rejected server-side.
                if (!stateCodes.contains(action.getNextState())) {
                    throw new DigitClientException("action " + action.getCode() + " references unknown nextState "
                            + action.getNextState());
                }
            }
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    // ── Processes (without states) ────────────────────────────────────────────

    /** Creates a process on its own; states and actions are then added separately. */
    public WorkflowProcess createProcess(WorkflowProcessRequest request) {
        if (request == null || isBlank(request.getCode()) || isBlank(request.getName())) {
            throw new DigitClientException("process code and name are required");
        }
        return post(processUrl(), request, WorkflowProcess.class);
    }

    /** Searches processes by any combination of ids, names and codes. */
    public List<WorkflowProcess> searchProcesses(List<String> ids, List<String> names, List<String> codes) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(processUrl());
        addEach(builder, "id", ids);
        addEach(builder, "name", names);
        addEach(builder, "code", codes);
        return getList(builder.toUriString(), PROCESS_LIST);
    }

    public WorkflowProcess getProcessByCode(String processCode) {
        requireText(processCode, "processCode is required");
        return get(processUrl() + "/code/" + processCode, WorkflowProcess.class);
    }

    public WorkflowProcess updateProcess(String processCode, WorkflowProcessRequest request) {
        requireText(processCode, "processCode is required");
        return exchange(processUrl() + "/code/" + processCode, HttpMethod.PUT, request, WorkflowProcess.class);
    }

    public boolean deleteProcess(String processCode) {
        requireText(processCode, "processCode is required");
        return deleted(processUrl() + "/code/" + processCode);
    }

    // ── States ───────────────────────────────────────────────────────────────

    /**
     * Adds a state to a process.
     *
     * <p>{@code type} defaults to {@code INTERMEDIATE} server-side, and whether the state is initial
     * is derived from the type rather than set directly.
     */
    public WorkflowState createState(String processCode, WorkflowStateDefinition state) {
        requireText(processCode, "processCode is required");
        if (state == null || isBlank(state.getCode()) || isBlank(state.getName())) {
            throw new DigitClientException("state code and name are required");
        }
        return post(stateUrl(processCode), state, WorkflowState.class);
    }

    public WorkflowState getState(String processCode, String stateCode) {
        requireText(processCode, "processCode is required");
        requireText(stateCode, "stateCode is required");
        return get(stateUrl(processCode) + "/" + stateCode, WorkflowState.class);
    }

    public WorkflowState updateState(String processCode, String stateCode, WorkflowStateDefinition state) {
        requireText(processCode, "processCode is required");
        requireText(stateCode, "stateCode is required");
        return exchange(stateUrl(processCode) + "/" + stateCode, HttpMethod.PUT, state, WorkflowState.class);
    }

    public boolean deleteState(String processCode, String stateCode) {
        requireText(processCode, "processCode is required");
        requireText(stateCode, "stateCode is required");
        return deleted(stateUrl(processCode) + "/" + stateCode);
    }

    // ── Actions ──────────────────────────────────────────────────────────────

    /** Adds an action to a state. {@code nextState} is the target state's code. */
    public WorkflowAction createAction(String processCode, String stateCode, WorkflowActionDefinition action) {
        requireText(processCode, "processCode is required");
        requireText(stateCode, "stateCode is required");
        if (action == null || isBlank(action.getCode()) || isBlank(action.getNextState())) {
            throw new DigitClientException("action code and nextState are required");
        }
        return post(actionUrl(processCode, stateCode), action, WorkflowAction.class);
    }

    public List<WorkflowAction> listActions(String processCode, String stateCode) {
        requireText(processCode, "processCode is required");
        requireText(stateCode, "stateCode is required");
        return getList(actionUrl(processCode, stateCode), ACTION_LIST);
    }

    public WorkflowAction getAction(String processCode, String stateCode, String actionCode) {
        requireText(actionCode, "actionCode is required");
        return get(actionUrl(processCode, stateCode) + "/" + actionCode, WorkflowAction.class);
    }

    public WorkflowAction updateAction(String processCode, String stateCode, String actionCode,
                                        WorkflowActionDefinition action) {
        requireText(actionCode, "actionCode is required");
        return exchange(actionUrl(processCode, stateCode) + "/" + actionCode, HttpMethod.PUT,
                action, WorkflowAction.class);
    }

    public boolean deleteAction(String processCode, String stateCode, String actionCode) {
        requireText(actionCode, "actionCode is required");
        return deleted(actionUrl(processCode, stateCode) + "/" + actionCode);
    }

    // ── Escalation configuration ─────────────────────────────────────────────

    /** Configures escalation for a state. Note the SLA fields are minutes, not milliseconds. */
    public WorkflowEscalationConfig createEscalationConfig(String processCode, WorkflowEscalationConfig config) {
        requireText(processCode, "processCode is required");
        if (config == null || isBlank(config.getStateCode())) {
            throw new DigitClientException("stateCode is required on an escalation config");
        }
        return post(escalationUrl(processCode), config, WorkflowEscalationConfig.class);
    }

    public List<WorkflowEscalationConfig> listEscalationConfigs(String processCode) {
        requireText(processCode, "processCode is required");
        return getList(escalationUrl(processCode), ESCALATION_LIST);
    }

    public WorkflowEscalationConfig getEscalationConfig(String processCode, String stateCode) {
        requireText(processCode, "processCode is required");
        requireText(stateCode, "stateCode is required");
        return get(escalationUrl(processCode) + "/" + stateCode, WorkflowEscalationConfig.class);
    }

    public WorkflowEscalationConfig updateEscalationConfig(String processCode, String stateCode,
                                                            WorkflowEscalationConfig config) {
        requireText(processCode, "processCode is required");
        requireText(stateCode, "stateCode is required");
        return exchange(escalationUrl(processCode) + "/" + stateCode, HttpMethod.PUT,
                config, WorkflowEscalationConfig.class);
    }

    public boolean deleteEscalationConfig(String processCode, String stateCode) {
        requireText(processCode, "processCode is required");
        requireText(stateCode, "stateCode is required");
        return deleted(escalationUrl(processCode) + "/" + stateCode);
    }

    // ── Automatic escalation ─────────────────────────────────────────────────

    /**
     * Escalates every breached instance of a process now, rather than waiting for the scheduled
     * sweep. Intended for operators and tests.
     */
    public List<WorkflowProcessInstance> escalateNow(String processCode) {
        requireText(processCode, "processCode is required");
        String url = this.apiProperties.getWorkflowServiceUrl()
                + "/workflow/v3/auto/" + processCode + "/_escalate";
        ResponseEntity<WorkflowProcessInstanceListResponse> response = this.restTemplate.postForEntity(
                url, null, WorkflowProcessInstanceListResponse.class);
        WorkflowProcessInstanceListResponse body = response.getBody();
        return body == null || body.getProcessInstances() == null ? List.of() : body.getProcessInstances();
    }

    /** Instances currently eligible for escalation. */
    /**
     * The instances eligible for auto-escalation.
     *
     * <p>Returns a bare list, unlike the other paginated searches here: this endpoint answers with a
     * JSON array rather than a {@code totalCount}/{@code page}/{@code size} envelope. Asking for the
     * envelope made every call throw, including the empty case, because {@code []} cannot bind to an
     * object.
     */
    public List<WorkflowProcessInstance> searchEscalatable(String processCode, Integer page, Integer size) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(
                this.apiProperties.getWorkflowServiceUrl() + "/workflow/v3/auto/_search");
        if (!isBlank(processCode)) {
            builder.queryParam("processCode", processCode);
        }
        if (page != null) {
            builder.queryParam("page", page);
        }
        if (size != null) {
            builder.queryParam("size", size);
        }
        return getList(builder.toUriString(), PROCESS_INSTANCE_LIST);
    }

    // ── Transition search and counts ─────────────────────────────────────────

    /**
     * Searches workflow instances.
     *
     * <p>The service permits at most one of {@code entityId}, {@code currentState}, {@code assignee}
     * and {@code escalated}; supplying none returns the caller's inbox.
     */
    public WorkflowProcessInstanceListResponse searchTransitions(WorkflowTransitionSearchCriteria criteria) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(
                this.apiProperties.getWorkflowServiceUrl() + "/workflow/v3/transition");
        if (criteria != null) {
            int exclusive = 0;
            if (!isBlank(criteria.getEntityId())) { ++exclusive; }
            if (!isBlank(criteria.getCurrentState())) { ++exclusive; }
            if (!isBlank(criteria.getAssignee())) { ++exclusive; }
            if (criteria.getEscalated() != null) { ++exclusive; }
            if (exclusive > 1) {
                throw new DigitClientException(
                        "specify only one of entityId, currentState, assignee or escalated per search");
            }
            addIfText(builder, "entityId", criteria.getEntityId());
            addIfText(builder, "processCode", criteria.getProcessCode());
            addIfText(builder, "version", criteria.getVersion());
            addIfText(builder, "currentState", criteria.getCurrentState());
            addIfText(builder, "assignee", criteria.getAssignee());
            if (criteria.getEscalated() != null) {
                builder.queryParam("escalated", criteria.getEscalated());
            }
            if (criteria.getHistory() != null) {
                builder.queryParam("history", criteria.getHistory());
            }
            if (criteria.getPage() != null) {
                builder.queryParam("page", criteria.getPage());
            }
            if (criteria.getSize() != null) {
                builder.queryParam("size", criteria.getSize());
            }
        }
        return get(builder.toUriString(), WorkflowProcessInstanceListResponse.class);
    }

    /**
     * Counts instances, grouped by state or by pending action according to {@code countType}.
     *
     * <p>{@code version} may only be given alongside a {@code processCode}.
     */
    public TransitionCountResponse countTransitions(String countType, WorkflowTransitionSearchCriteria criteria) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(
                this.apiProperties.getWorkflowServiceUrl() + "/workflow/v3/transition/count");
        addIfText(builder, "countType", countType);
        if (criteria != null) {
            if (!isBlank(criteria.getVersion()) && isBlank(criteria.getProcessCode())) {
                throw new DigitClientException("processCode is required when filtering by version");
            }
            addIfText(builder, "entityId", criteria.getEntityId());
            addIfText(builder, "processCode", criteria.getProcessCode());
            addIfText(builder, "version", criteria.getVersion());
            addIfText(builder, "currentState", criteria.getCurrentState());
            addIfText(builder, "assignee", criteria.getAssignee());
            if (criteria.getEscalated() != null) {
                builder.queryParam("escalated", criteria.getEscalated());
            }
            if (criteria.getHistory() != null) {
                builder.queryParam("history", criteria.getHistory());
            }
        }
        return get(builder.toUriString(), TransitionCountResponse.class);
    }

    /**
     * Performs a transition as the system rather than as a user, bypassing the role and assignee
     * checks a normal transition applies.
     */
    public WorkflowTransitionResponse executeSystemTransition(WorkflowTransitionRequest request) {
        if (request == null || isBlank(request.getProcessCode()) || isBlank(request.getEntityId())) {
            throw new DigitClientException("processCode and entityId are required");
        }
        String url = this.apiProperties.getWorkflowServiceUrl() + "/workflow/v3/system/transition";
        return post(url, request, WorkflowTransitionResponse.class);
    }

    // ── Internals ────────────────────────────────────────────────────────────

    private String processUrl() {
        return this.apiProperties.getWorkflowServiceUrl() + "/workflow/v3/process";
    }

    private String stateUrl(String processCode) {
        return processUrl() + "/" + processCode + "/state";
    }

    private String actionUrl(String processCode, String stateCode) {
        return stateUrl(processCode) + "/" + stateCode + "/action";
    }

    private String escalationUrl(String processCode) {
        return processUrl() + "/" + processCode + "/escalation";
    }

    private <T> T post(String url, Object body, Class<T> type) {
        return this.restTemplate.postForEntity(url, body, type).getBody();
    }

    private <T> T get(String url, Class<T> type) {
        return this.restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), type).getBody();
    }

    private <T> List<T> getList(String url, ParameterizedTypeReference<List<T>> type) {
        List<T> body = this.restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), type).getBody();
        return body == null ? List.of() : body;
    }

    private <T> T exchange(String url, HttpMethod method, Object body, Class<T> type) {
        return this.restTemplate.exchange(url, method, new HttpEntity<>(body), type).getBody();
    }

    private boolean deleted(String url) {
        Map<String, Object> body = this.restTemplate.exchange(
                url, HttpMethod.DELETE, new HttpEntity<>(new HttpHeaders()), DELETED_RESULT).getBody();
        return body != null && Boolean.TRUE.equals(body.get("deleted"));
    }

    private static void addEach(UriComponentsBuilder builder, String name, List<String> values) {
        if (values == null) {
            return;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                builder.queryParam(name, value);
            }
        }
    }

    private static void addIfText(UriComponentsBuilder builder, String name, String value) {
        if (!isBlank(value)) {
            builder.queryParam(name, value);
        }
    }

    private static void requireText(String value, String message) {
        if (isBlank(value)) {
            throw new DigitClientException(message);
        }
    }
}
