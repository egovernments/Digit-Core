package org.egov.access.persistence.repository.rowmapper;

import org.egov.access.domain.model.Action;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ActionSearchRowMapperTest {

	private final ActionSearchRowMapper mapper = new ActionSearchRowMapper();

	@Test
	public void testGroupsByServiceCodeAndMapsPolicyFields() throws SQLException {
		ResultSet rs = mock(ResultSet.class);
		when(rs.getString("servicecode")).thenReturn("PGR");
		when(rs.getLong("id")).thenReturn(1L);
		when(rs.getString("url")).thenReturn("/pgr-services/v2/request/_search");
		when(rs.getString("name")).thenReturn("Search Complaint");
		when(rs.getString("displayname")).thenReturn("Search Complaint");
		when(rs.getBoolean("enabled")).thenReturn(true);
		when(rs.getString("parentmodule")).thenReturn("PGR");
		when(rs.getInt("ordernumber")).thenReturn(1);
		when(rs.getString("method")).thenReturn("POST");
		when(rs.getString("resource")).thenReturn("[\"complaint\"]");
		when(rs.getString("condition")).thenReturn("{\"==\":[1,1]}");

		mapper.mapRow(rs, 0);

		List<Action> actions = mapper.actionMap.get("PGR");
		assertEquals(1, actions.size());
		assertEquals("POST", actions.get(0).getMethod());
		assertEquals(List.of("complaint"), actions.get(0).getResource());
		assertEquals(Map.of("==", List.of(1, 1)), actions.get(0).getCondition());
	}

	@Test
	public void testTreatsMissingPolicyColumnsAsNullForBackwardCompatibility() throws SQLException {
		ResultSet rs = mock(ResultSet.class);
		when(rs.getString("servicecode")).thenReturn("LEGACY");
		when(rs.getString("method")).thenThrow(new SQLException("column method not found"));
		when(rs.getString("resource")).thenThrow(new SQLException("column resource not found"));
		when(rs.getString("condition")).thenThrow(new SQLException("column condition not found"));

		mapper.mapRow(rs, 0);

		Action action = mapper.actionMap.get("LEGACY").get(0);
		assertNull(action.getMethod());
		assertNull(action.getResource());
		assertNull(action.getCondition());
	}
}
