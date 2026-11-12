package org.egov.access.persistence.repository.querybuilder;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Arrays;
import java.util.Collections;

import org.egov.access.domain.criteria.ActionSearchCriteria;
import org.junit.jupiter.api.Test;

public class ActionFinderQueryBuilderTest {

	private static final String BASE_QUERY = "SELECT distinct a.id AS a_id, a.name AS a_name, a.url AS "
			+ "a_url, a.servicecode AS a_servicecode, a.queryparams AS a_queryparams, a.parentmodule AS a_parentmodule, a.displayname AS a_displayname, a.enabled AS a_enabled, "
			+ " a.createdby AS a_createdby, a.createddate AS a_createddate, a.lastmodifiedby"
			+ " AS a_lastmodifiedby, a.lastmodifieddate AS a_lastmodifieddate,a.ordernumber AS a_ordernumber, ra.tenantid AS ra_tenantid, "
			+ "ra.actionid AS ra_action, ra.rolecode AS ra_rolecode FROM eg_action AS a JOIN eg_roleaction AS ra ON a.id = ra.actionid";

	@Test
	public void testQueryToFindRoles() {
		ActionSearchCriteria criteria = ActionSearchCriteria.builder().tenantId("ap.public")
				.roleCodes(Arrays.asList("CITIZEN", "EMPLOYEE")).build();
		ActionFinderQueryBuilder builder = new ActionFinderQueryBuilder(criteria);

		String expectedQuery = BASE_QUERY + " WHERE ra.tenantid = ? AND ra.rolecode in (?,?) ORDER BY a.name";
		assertEquals(expectedQuery, builder.build());
		assertArrayEquals(new Object[] { "ap.public", "CITIZEN", "EMPLOYEE" }, builder.getParameters());
	}

	@Test
	public void testQueryToFindRolesWhenNoTenantId() {
		ActionSearchCriteria criteria = ActionSearchCriteria.builder().roleCodes(Collections.singletonList("CITIZEN"))
				.build();
		ActionFinderQueryBuilder builder = new ActionFinderQueryBuilder(criteria);

		String expectedQuery = BASE_QUERY + " WHERE ra.rolecode in (?) ORDER BY a.name";
		assertEquals(expectedQuery, builder.build());
		assertArrayEquals(new Object[] { "CITIZEN" }, builder.getParameters());
	}

	@Test
	public void testQueryToFindRolesWhenNoRoleCodes() {
		ActionSearchCriteria criteria = ActionSearchCriteria.builder().tenantId("ap.public").build();
		ActionFinderQueryBuilder builder = new ActionFinderQueryBuilder(criteria);

		String expectedQuery = BASE_QUERY + " WHERE ra.tenantid = ? ORDER BY a.name";
		assertEquals(expectedQuery, builder.build());
		assertArrayEquals(new Object[] { "ap.public" }, builder.getParameters());
	}

	@Test
	public void testQueryToFindRolesWhenNoRoleCodesAndNoTenantId() {
		ActionSearchCriteria criteria = ActionSearchCriteria.builder().build();
		ActionFinderQueryBuilder builder = new ActionFinderQueryBuilder(criteria);

		String expectedQuery = BASE_QUERY + " ORDER BY a.name";
		assertEquals(expectedQuery, builder.build());
		assertArrayEquals(new Object[0], builder.getParameters());
	}

	@Test
	public void testCriteriaValuesNeverAlterQueryStructure() {
		ActionSearchCriteria criteria = ActionSearchCriteria.builder().tenantId("x' OR '1'='1' -- ")
				.roleCodes(Collections.singletonList("x') UNION SELECT 1 -- ")).build();
		ActionFinderQueryBuilder builder = new ActionFinderQueryBuilder(criteria);

		String query = builder.build();
		assertFalse(query.contains("OR '1'='1'"));
		assertFalse(query.contains("UNION"));
		assertArrayEquals(new Object[] { "x' OR '1'='1' -- ", "x') UNION SELECT 1 -- " }, builder.getParameters());
	}

}
