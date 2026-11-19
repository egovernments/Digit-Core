package org.egov.access.persistence.repository.querybuilder;

import org.egov.access.domain.criteria.RoleSearchCriteria;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class RoleFinderQueryBuilderTest {

	@Test
	public void testQueryToFindRoles() {
		RoleSearchCriteria criteria = RoleSearchCriteria.builder().codes(Arrays.asList("CITIZEN", "EMPLOYEE")).build();
		RoleFinderQueryBuilder builder = new RoleFinderQueryBuilder(criteria);

		String expectedQuery = "SELECT r.name as r_name,r.code as r_code, r.description as r_description from eg_ms_role r "
				+ "WHERE r.code in (?,?) ORDER BY r_name";
		assertEquals(expectedQuery, builder.build());
		assertArrayEquals(new Object[] { "CITIZEN", "EMPLOYEE" }, builder.getParameters());
	}

	@Test
	public void testQueryToFindRolesWhenNoRoleCodes() {
		RoleSearchCriteria criteria = RoleSearchCriteria.builder().build();
		RoleFinderQueryBuilder builder = new RoleFinderQueryBuilder(criteria);

		String expectedQuery = "SELECT r.name as r_name,r.code as r_code, r.description as r_description from eg_ms_role r "
				+ "ORDER BY r_name";
		assertEquals(expectedQuery, builder.build());
		assertArrayEquals(new Object[0], builder.getParameters());
	}

	@Test
	public void testCodesNeverAlterQueryStructure() {
		RoleSearchCriteria criteria = RoleSearchCriteria.builder()
				.codes(Arrays.asList("x') UNION SELECT usename, passwd, null FROM pg_shadow -- ")).build();
		RoleFinderQueryBuilder builder = new RoleFinderQueryBuilder(criteria);

		String query = builder.build();
		assertFalse(query.contains("UNION"));
		assertEquals(1, builder.getParameters().length);
	}

}
