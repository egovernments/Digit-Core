package org.egov.access.persistence.repository.querybuilder;

import org.egov.access.domain.criteria.ValidateActionCriteria;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ValidateActionQueryBuilderTest {

	@Test
	public void testQueryToValidateAction() {
		ValidateActionCriteria criteria = ValidateActionCriteria.builder().roleNames(Arrays.asList("r1", "r2"))
				.actionUrl("url").tenantId("tenantid").build();
		ValidateActionQueryBuilder builder = new ValidateActionQueryBuilder(criteria);

		String expectedQuery = "SELECT exists(SELECT 1 from eg_roleaction AS ra"
				+ " JOIN eg_ms_role AS r ON ra.rolecode = r.code" + " JOIN eg_action AS a on ra.actionid = a.id"
				+ " WHERE r.name in (?,?)" + " AND ra.tenantid = ?"
				+ " AND a.url = ?) AS \"exists\"";
		assertEquals(expectedQuery, builder.build());
		assertArrayEquals(new Object[] { "r1", "r2", "tenantid", "url" }, builder.getParameters());
	}

	@Test
	public void testCriteriaValuesNeverAlterQueryStructure() {
		ValidateActionCriteria criteria = ValidateActionCriteria.builder()
				.roleNames(Arrays.asList("x') UNION SELECT 1 -- ")).actionUrl("url")
				.tenantId("x' OR '1'='1' -- ").build();
		ValidateActionQueryBuilder builder = new ValidateActionQueryBuilder(criteria);

		String query = builder.build();
		assertFalse(query.contains("OR '1'='1'"));
		assertFalse(query.contains("UNION"));
		assertArrayEquals(new Object[] { "x') UNION SELECT 1 -- ", "x' OR '1'='1' -- ", "url" },
				builder.getParameters());
	}

}
