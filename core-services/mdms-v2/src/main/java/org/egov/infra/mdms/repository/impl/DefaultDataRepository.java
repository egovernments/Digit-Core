package org.egov.infra.mdms.repository.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@Slf4j
public class DefaultDataRepository {

	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public DefaultDataRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void copySchemaData(String defaultTenantId, String targetTenantId, List<String> schemaCodes) {
		// Construct the query with parameters
		String sql = "INSERT INTO eg_mdms_data (id, tenantid, uniqueidentifier, schemacode, data, isactive, createdby, lastmodifiedby, createdtime, lastmodifiedtime) " +
				"SELECT uuid_generate_v4(), ?, uniqueidentifier, schemacode, data, isactive, createdby, lastmodifiedby, ?, NULL::bigint " +
				"FROM eg_mdms_data " +
				"WHERE tenantid = ? " +
				"AND schemacode IN (" + createQuery(schemaCodes) + ")";

		// Get the current time in milliseconds
		long currentEpochMillis = System.currentTimeMillis();

		List<Object> preparedStmtList = new ArrayList<>();
		preparedStmtList.add(targetTenantId);
		preparedStmtList.add(currentEpochMillis);
		preparedStmtList.add(defaultTenantId);
		addToPreparedStatement(preparedStmtList, schemaCodes);

		// Execute the query
		jdbcTemplate.update(sql, preparedStmtList.toArray());
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
		ids.forEach(id -> {
			preparedStmtList.add(id);
		});
	}
}
