package org.egov.infra.mdms.repository.impl;

import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@Slf4j
public class DefaultSchemaRepository {

	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public DefaultSchemaRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void copySchemaDefinitions(String defaultTenantId, String targetTenantId, List<String> schemaCodes) {
		// Construct the query with parameters
		String sql = "INSERT INTO eg_mdms_schema_definition (id, tenantid, code, description, definition, isactive, createdby, lastmodifiedby, createdtime, lastmodifiedtime) " +
				"SELECT uuid_generate_v4(), ?, code, description, definition, isactive, createdby, lastmodifiedby, ?, NULL::bigint " +
				"FROM eg_mdms_schema_definition " +
				"WHERE tenantid = ? " +
				"AND code IN (" + createQuery(schemaCodes) + ")" +
				"ON CONFLICT (tenantid, code) DO NOTHING;";

		// Get the current time in milliseconds
		long currentEpochMillis = System.currentTimeMillis();

		List<Object> preparedStmtList = new ArrayList<>();
		preparedStmtList.add(targetTenantId);
		preparedStmtList.add(currentEpochMillis);
		preparedStmtList.add(defaultTenantId);
		addToPreparedStatement(preparedStmtList, schemaCodes);

		try {
			// Execute the query
			jdbcTemplate.update(sql, preparedStmtList.toArray());
		} catch (DataIntegrityViolationException e) {
			// extract information from the exception
			String message = e.getMessage();
			log.error("Detailed error: {}", message);

			// Re-throw or handle the conflict as needed
			throw new CustomException("Conflict during insert operation", e.getMessage());
		}
	}

	private String createQuery(List<String> ids) {
		StringBuilder builder = new StringBuilder();
		int length = ids.size();
		for (int i = 0; i < length; i++) {
			builder.append(" ?");
			if (i != length - 1)
				builder.append(",");
		}
		return builder.toString();
	}

	private void addToPreparedStatement(List<Object> preparedStmtList, List<String> ids) {
		preparedStmtList.addAll(ids);
	}
}


