package org.egov.access.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.egov.access.domain.model.Action;
import org.egov.access.web.contract.action.ActionRequest;
import org.egov.access.web.contract.action.Module;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ActionRepositoryTest {

	@Autowired
	private ActionRepository actionRepository;

	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Test
	@Sql(scripts = { "/sql/clearAction.sql" })
	public void testshouldcreateactions() {

		ActionRequest actionRequest = new ActionRequest();

		actionRequest.setRequestInfo(getRequestInfo());
		actionRequest.setActions(getActions());

		List<Action> actions = actionRepository.createAction(actionRequest);
		assertThat(actions.size()).isEqualTo(2);
	}

	@Test
	@Sql(scripts = { "/sql/clearAction.sql" })
	public void testCreateActionsIfThereisActionsAsEmpty() {

		ActionRequest actionRequest = new ActionRequest();

		List<Action> actionList = new ArrayList<Action>();

		actionRequest.setActions(actionList);

		List<Action> actions = actionRepository.createAction(actionRequest);

		assertThat(actions.size()).isEqualTo(0);
	}

	@Test(expected = NullPointerException.class)
	@Sql(scripts = { "/sql/clearAction.sql" })
	public void testCreateActionsIfThereisNoActions() {

		ActionRequest actionRequest = new ActionRequest();

		List<Action> actions = actionRepository.createAction(actionRequest);

		assertThat(actions).isEqualTo(null);
	}

	@Test
	@Sql(scripts = { "/sql/clearAction.sql", "/sql/insertActionData.sql" })
	public void TestCheckActionNameExit() {

		boolean exist = actionRepository.checkActionNameExit("Get all ReceivingMode");
		assertThat(exist == true);

	}

	@Test
	@Sql(scripts = { "/sql/clearAction.sql", "/sql/insertActionData.sql" })
	public void TestCheckActionNameDoesNotExit() {

		boolean exist = actionRepository.checkActionNameExit("TestActionName");
		assertThat(exist == false);

	}

	@Test
	@Sql(scripts = { "/sql/clearAction.sql", "/sql/insertActionData.sql" })
	public void TestCheckCombinationOfUrlAndqueryparamsExist() {

		boolean exist = actionRepository.checkCombinationOfUrlAndqueryparamsExist("/pgr/receivingmode", "tenantId=");
		assertThat(exist == true);
	}

	@Test
	@Sql(scripts = { "/sql/clearAction.sql", "/sql/insertActionData.sql" })
	public void TestCheckCombinationOfUrlAndqueryparamsNotExist() {

		boolean exist = actionRepository.checkCombinationOfUrlAndqueryparamsExist("/test/testreceivingmode", "test=");
		assertThat(exist == false);
	}

	@Test
	@Sql(scripts = { "/sql/clearAction.sql", "/sql/insertActionData.sql" })
	public void testShouldUpdateActions() {

		ActionRequest actionRequest = new ActionRequest();

		actionRequest.setRequestInfo(getRequestInfo());

		List<Action> actionList = new ArrayList<Action>();

		Action action = new Action();

		action.setName("Get all ReceivingMode");

		action.setUrl("/test/getAllReceivingMode");
		actionList.add(action);

		actionRequest.setActions(actionList);

		List<Action> actions = actionRepository.updateAction(actionRequest);

		assertThat(actions.size()).isEqualTo(1);
		assertThat(actions.get(0).getUrl().equals("/test/getAllReceivingMode"));
	}

	@Test
	@Sql(scripts = { "/sql/clearAction.sql", "/sql/insertActionData.sql" })
	public void testShouldGetmodules() {

		ActionRequest actionRequest = new ActionRequest();

		actionRequest.setRequestInfo(getRequestInfo());

		actionRequest.setTenantId("ap.public");

		List<String> roleCodes = new ArrayList<String>();

		roleCodes.add("SUPERUSER");

		actionRequest.setRoleCodes(roleCodes);

		actionRequest.setEnabled(false);

		List<Module> modules = actionRepository.getAllActionsBasedOnRoles(actionRequest).getModules();

		assertThat(modules.size()).isEqualTo(0);
	}

	@Test
	@Sql(scripts = { "/sql/clearAction.sql" })
	public void testShouldPersistAndReturnPolicyFieldsOnCreateAndUpdate() {

		ActionRequest actionRequest = new ActionRequest();
		actionRequest.setRequestInfo(getRequestInfo());

		Action action = new Action();
		action.setName("AssignComplaint");
		action.setUrl("/pgr-services/v2/request/_update");
		action.setDisplayName("Assign Complaint");
		action.setTenantId("default");
		action.setServiceCode("PGR");
		action.setMethod("POST");
		action.setResource(Map.of("complaint", Map.of("attributes", Map.of(
				"citizen.mobileNumber", Map.of(
						"condition", Map.of("==", List.of(1, 1)),
						"onDeny", Map.of("strategy", "REDACT"))))));
		action.setCondition(Map.of("==", List.of(1, 1)));

		List<Action> actionList = new ArrayList<>();
		actionList.add(action);
		actionRequest.setActions(actionList);

		List<Action> created = actionRepository.createAction(actionRequest);
		assertThat(created.size()).isEqualTo(1);

		Map<String, Object> row = namedParameterJdbcTemplate.queryForMap(
				"select method, resource, condition from eg_action where name = :name",
				Map.of("name", "AssignComplaint"));

		assertThat(row.get("method")).isEqualTo("POST");
		assertThat((String) row.get("resource")).contains("complaint");
		assertThat((String) row.get("condition")).contains("==");

		Action updated = new Action();
		updated.setName("AssignComplaint");
		updated.setUrl("/pgr-services/v2/request/_update");
		updated.setDisplayName("Assign Complaint");
		updated.setServiceCode("PGR");
		updated.setMethod("PUT");
		updated.setResource(Map.of("complaint", Map.of("attributes", Map.of(
				"citizen.mobileNumber", Map.of(
						"condition", Map.of("!=", List.of(1, 2)),
						"onDeny", Map.of("strategy", "MASK_SHOW_LAST_N", "n", 2))))));
		updated.setCondition(Map.of("!=", List.of(1, 2)));

		List<Action> updateList = new ArrayList<>();
		updateList.add(updated);
		actionRequest.setActions(updateList);
		actionRepository.updateAction(actionRequest);

		Map<String, Object> updatedRow = namedParameterJdbcTemplate.queryForMap(
				"select method, resource, condition from eg_action where name = :name",
				Map.of("name", "AssignComplaint"));

		assertThat(updatedRow.get("method")).isEqualTo("PUT");
		assertThat((String) updatedRow.get("resource")).contains("MASK_SHOW_LAST_N");
		assertThat((String) updatedRow.get("condition")).contains("!=");
	}

	@Test
	@Sql(scripts = { "/sql/clearAction.sql" })
	public void testShouldLeavePolicyFieldsNullForLegacyActionsWithoutThem() {

		ActionRequest actionRequest = new ActionRequest();
		actionRequest.setRequestInfo(getRequestInfo());
		actionRequest.setActions(getActions());

		actionRepository.createAction(actionRequest);

		Map<String, Object> row = namedParameterJdbcTemplate.queryForMap(
				"select method, resource, condition from eg_action where name = :name",
				Map.of("name", "ActionOne"));

		assertThat(row.get("method")).isNull();
		assertThat(row.get("resource")).isNull();
		assertThat(row.get("condition")).isNull();
	}

	private List<Action> getActions() {

		List<Action> actionList = new ArrayList<Action>();

		Action action1 = new Action();

		action1.setName("ActionOne");
		action1.setUrl("/actionone");
		action1.setDisplayName("ActionOne");
		action1.setTenantId("default");
		action1.setServiceCode("ACTION");

		Action action2 = new Action();

		action2.setName("test");
		action2.setUrl("/test");
		action2.setDisplayName("TEST");
		action2.setTenantId("default");
		action2.setServiceCode("TEST");

		actionList.add(action1);
		actionList.add(action2);

		return actionList;
	}

	private RequestInfo getRequestInfo() {

		RequestInfo request = new RequestInfo();

		User user = new User();

		user.setId(1l);
		request.setUserInfo(user);

		return request;
	}

}