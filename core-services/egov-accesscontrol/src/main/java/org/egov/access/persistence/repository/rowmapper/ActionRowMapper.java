package org.egov.access.persistence.repository.rowmapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.access.domain.model.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ActionRowMapper implements RowMapper<Action> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ActionRowMapper.class);

	// This mapper is often constructed directly with `new ActionRowMapper()` (see ActionService),
	// bypassing Spring DI, so it can't rely on an injected ObjectMapper bean.
	private static final ObjectMapper MAPPER = new ObjectMapper();

	@Override
	public Action mapRow(final ResultSet rs, final int rowNum) throws SQLException {
		final Action action = Action.builder().id(rs.getLong("a_id")).name(rs.getString("a_name"))
				.url(rs.getString("a_url")).displayName(rs.getString("a_displayname"))
				.serviceCode(rs.getString("a_servicecode")).parentModule(rs.getString("a_parentmodule"))
				.enabled(rs.getBoolean("a_enabled")).createdBy(rs.getLong("a_createdby"))
				.createdDate(rs.getDate("a_createddate")).lastModifiedBy(rs.getLong("a_lastmodifiedby"))
				.lastModifiedDate(rs.getDate("a_lastmodifieddate")).orderNumber(rs.getInt("a_ordernumber"))
				.queryParams(rs.getString("a_queryparams")).tenantId(rs.getString("ra_tenantId"))
				.method(readColumn(rs, "a_method"))
				.resource(readResource(readColumn(rs, "a_resource")))
				.condition(readCondition(readColumn(rs, "a_condition")))
				.build();

		return action;

	}

	/**
	 * Reads a column that may not exist on older result sets (pre-migration rows/queries that
	 * don't select it) — returns null instead of throwing, keeping this mapper backward compatible.
	 */
	private String readColumn(ResultSet rs, String column) {
		try {
			return rs.getString(column);
		} catch (SQLException e) {
			return null;
		}
	}

	private Object readResource(String json) {
		if (json == null || json.isBlank())
			return null;
		try {
			return MAPPER.readValue(json, Object.class);
		} catch (Exception e) {
			LOGGER.error("Failed to parse action 'resource' column: " + e.getMessage());
			return null;
		}
	}

	private Object readCondition(String json) {
		if (json == null || json.isBlank())
			return null;
		try {
			return MAPPER.readValue(json, Object.class);
		} catch (Exception e) {
			LOGGER.error("Failed to parse action 'condition' column: " + e.getMessage());
			return null;
		}
	}
}