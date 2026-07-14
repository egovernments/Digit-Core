package org.egov.access.persistence.repository.rowmapper;

import org.egov.access.domain.model.Action;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ActionRowMapperTest {

	private final ActionRowMapper mapper = new ActionRowMapper();

	@Test
	public void testMapsLegacyFlatStringArrayResourceShape() throws SQLException {
		ResultSet rs = mock(ResultSet.class);
		when(rs.getLong("a_id")).thenReturn(1L);
		when(rs.getString("a_name")).thenReturn("Assign Complaint");
		when(rs.getString("a_url")).thenReturn("/pgr-services/v2/request/_update");
		when(rs.getString("a_displayname")).thenReturn("Assign Complaint");
		when(rs.getString("a_servicecode")).thenReturn("PGR");
		when(rs.getString("a_parentmodule")).thenReturn("PGR");
		when(rs.getBoolean("a_enabled")).thenReturn(true);
		when(rs.getLong("a_createdby")).thenReturn(1L);
		when(rs.getDate("a_createddate")).thenReturn(new Date(0));
		when(rs.getLong("a_lastmodifiedby")).thenReturn(1L);
		when(rs.getDate("a_lastmodifieddate")).thenReturn(new Date(0));
		when(rs.getInt("a_ordernumber")).thenReturn(1);
		when(rs.getString("ra_tenantId")).thenReturn("default");
		when(rs.getString("a_method")).thenReturn("POST");
		when(rs.getString("a_resource")).thenReturn("[\"complaint\"]");
		when(rs.getString("a_condition")).thenReturn("{\"==\":[1,1]}");

		Action action = mapper.mapRow(rs, 0);

		assertEquals("POST", action.getMethod());
		assertEquals(List.of("complaint"), action.getResource());
		assertNotNull(action.getCondition());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testMapsStructuredResourceObjectShapeWithFieldVisibilityRules() throws SQLException {
		ResultSet rs = mock(ResultSet.class);
		when(rs.getString("a_name")).thenReturn("Search PGR Request");
		when(rs.getString("a_resource")).thenReturn(
				"{\"complaint\":{\"attributes\":{\"citizen.mobileNumber\":{"
						+ "\"condition\":{\"==\":[1,1]},"
						+ "\"onDeny\":{\"strategy\":\"MASK_SHOW_LAST_N\",\"n\":2}}}}}");

		Action action = mapper.mapRow(rs, 0);

		Map<String, Object> resource = (Map<String, Object>) action.getResource();
		Map<String, Object> complaint = (Map<String, Object>) resource.get("complaint");
		Map<String, Object> attributes = (Map<String, Object>) complaint.get("attributes");
		Map<String, Object> rule = (Map<String, Object>) attributes.get("citizen.mobileNumber");

		assertNotNull(rule.get("condition"));
		assertEquals("MASK_SHOW_LAST_N", ((Map<String, Object>) rule.get("onDeny")).get("strategy"));
	}

	@Test
	public void testTreatsMissingPolicyColumnsAsNullForBackwardCompatibility() throws SQLException {
		ResultSet rs = mock(ResultSet.class);
		when(rs.getString("a_name")).thenReturn("Legacy Action");
		when(rs.getString("a_url")).thenReturn("/legacy");
		when(rs.getString("a_method")).thenThrow(new SQLException("column a_method not found"));
		when(rs.getString("a_resource")).thenThrow(new SQLException("column a_resource not found"));
		when(rs.getString("a_condition")).thenThrow(new SQLException("column a_condition not found"));

		Action action = mapper.mapRow(rs, 0);

		assertNull(action.getMethod());
		assertNull(action.getResource());
		assertNull(action.getCondition());
	}

	@Test
	public void testMalformedResourceAndConditionJsonFailSafeToNull() throws SQLException {
		ResultSet rs = mock(ResultSet.class);
		when(rs.getString("a_name")).thenReturn("Broken Action");
		when(rs.getString("a_resource")).thenReturn("not-json");
		when(rs.getString("a_condition")).thenReturn("not-json");

		Action action = mapper.mapRow(rs, 0);

		assertNull(action.getResource());
		assertNull(action.getCondition());
	}
}
