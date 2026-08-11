package org.egov.access.persistence.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.access.domain.model.Action;
import org.egov.access.domain.model.MdmsAction;
import org.egov.access.web.contract.action.ActionRequest;
import org.egov.access.web.contract.action.ActionSearchResponse;
import org.json.JSONArray;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ActionMdmsProjectionTest {

    private final ActionRepository repository = new ActionRepository();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void projectsAndSerializesOpaquePolicyMetadataWithoutChangingItsJsonShape() throws Exception {
        JSONArray mdmsActions = new JSONArray("["
                + "{\"id\":2008,\"url\":\"/pgr-services/v2/request/_search\","
                + "\"method\":\"POST\","
                + "\"resource\":{\"complaint\":{\"attributes\":[\"citizen.mobileNumber\"]}},"
                + "\"condition\":{\"and\":[{\"==\":[{\"var\":\"user.tenantId\"},\"pg\"]},true]}},"
                + "{\"id\":2009,\"url\":\"/pgr-services/v2/request/_count\","
                + "\"method\":\"POST\",\"resource\":[\"complaint\"],\"condition\":false}"
                + "]");

        List<Action> actions = repository.convertToAction(requestFor("pg.citya"), mdmsActions);

        assertThat(actions).hasSize(2);
        assertThat(actions).allSatisfy(action -> assertThat(action).isInstanceOf(MdmsAction.class));
        MdmsAction searchAction = (MdmsAction) actions.get(0);
        MdmsAction countAction = (MdmsAction) actions.get(1);
        assertThat(searchAction.getMethod()).isEqualTo("POST");
        assertThat(searchAction.getResource()).isInstanceOf(Map.class);
        assertThat(searchAction.getCondition()).isInstanceOf(Map.class);
        assertThat(countAction.getResource()).isEqualTo(List.of("complaint"));
        assertThat(countAction.getCondition()).isEqualTo(false);
        assertThat(actions).allSatisfy(action -> assertThat(action.getTenantId()).isEqualTo("pg.citya"));

        ActionSearchResponse response = new ActionSearchResponse();
        response.setActions(actions);
        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(response));

        assertThat(json.at("/actions/0/method").asText()).isEqualTo("POST");
        assertThat(json.at("/actions/0/resource/complaint/attributes/0").asText())
                .isEqualTo("citizen.mobileNumber");
        assertThat(json.at("/actions/0/condition/and/0/==/0/var").asText())
                .isEqualTo("user.tenantId");
        assertThat(json.at("/actions/1/resource").isArray()).isTrue();
        assertThat(json.at("/actions/1/condition").isBoolean()).isTrue();
        assertThat(json.at("/actions/1/condition").asBoolean()).isFalse();
    }

    @Test
    public void leavesPolicyMetadataNullForLegacyMdmsActions() throws Exception {
        JSONArray mdmsActions = new JSONArray("[{\"id\":1,\"url\":\"/legacy\"}]");

        Action action = repository.convertToAction(requestFor("pg"), mdmsActions).get(0);

        assertThat(action).isInstanceOf(MdmsAction.class);
        MdmsAction mdmsAction = (MdmsAction) action;
        assertThat(mdmsAction.getMethod()).isNull();
        assertThat(mdmsAction.getResource()).isNull();
        assertThat(mdmsAction.getCondition()).isNull();
        assertThat(action.getDisplayName()).isEmpty();
        assertThat(action.isEnabled()).isFalse();

        ActionSearchResponse response = new ActionSearchResponse();
        response.setActions(List.of(action));
        JsonNode wireAction = objectMapper.readTree(objectMapper.writeValueAsString(response)).at("/actions/0");
        assertThat(wireAction.has("method")).isFalse();
        assertThat(wireAction.has("resource")).isFalse();
        assertThat(wireAction.has("condition")).isFalse();
    }

    @Test
    public void sharedActionKeepsItsExistingDatabaseAndWireContract() throws Exception {
        Action action = Action.builder()
                .id(1L)
                .name("Legacy DB action")
                .url("/legacy")
                .enabled(true)
                .build();

        JsonNode wireAction = objectMapper.readTree(objectMapper.writeValueAsString(action));

        assertThat(wireAction.get("id").asLong()).isEqualTo(1L);
        assertThat(wireAction.get("name").asText()).isEqualTo("Legacy DB action");
        assertThat(wireAction.get("url").asText()).isEqualTo("/legacy");
        assertThat(wireAction.get("enabled").asBoolean()).isTrue();
        List<String> fieldNames = new ArrayList<>();
        wireAction.fieldNames().forEachRemaining(fieldNames::add);
        assertThat(fieldNames).containsExactlyInAnyOrder(
                "id", "name", "url", "displayName", "orderNumber", "queryParams",
                "parentModule", "enabled", "serviceCode", "tenantId", "createdDate", "createdBy",
                "lastModifiedDate", "lastModifiedBy", "path", "navigationURL", "leftIcon", "rightIcon");
    }

    private ActionRequest requestFor(String tenantId) {
        ActionRequest request = new ActionRequest();
        request.setTenantId(tenantId);
        return request;
    }
}
