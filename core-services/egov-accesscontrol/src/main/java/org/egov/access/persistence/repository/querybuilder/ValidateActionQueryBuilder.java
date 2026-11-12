package org.egov.access.persistence.repository.querybuilder;

import org.egov.access.domain.criteria.ValidateActionCriteria;

import java.util.ArrayList;
import java.util.List;

public class ValidateActionQueryBuilder implements BaseQueryBuilder {
	private ValidateActionCriteria criteria;
	private List<String> filters = new ArrayList<>();
	private List<Object> parameters = new ArrayList<>();

	private String BASE_QUERY = "SELECT exists(SELECT 1 from eg_roleaction AS ra"
			+ " JOIN eg_ms_role AS r ON ra.rolecode = r.code" + " JOIN eg_action AS a on ra.actionid = a.id";

	public ValidateActionQueryBuilder(ValidateActionCriteria criteria) {
		this.criteria = criteria;
	}

	private void filterRoleNames() {
		List<String> placeholders = new ArrayList<>();
		for (String roleName : criteria.getRoleNames()) {
			placeholders.add("?");
			parameters.add(roleName);
		}
		filters.add("r.name in (" + String.join(",", placeholders) + ")");
	}

	private void filterTenantId() {
		filters.add("ra.tenantid = ?");
		parameters.add(criteria.getTenantId());
	}

	private void filterActionUrl() {
		filters.add("a.url = ?");
		parameters.add(criteria.getActionUrl());
	}

	@Override
	public String build() {
		filterRoleNames();
		filterTenantId();
		filterActionUrl();
		return BASE_QUERY + " WHERE " + String.join(" AND ", filters) + ") AS \"exists\"";
	}

	@Override
	public Object[] getParameters() {
		return parameters.toArray();
	}
}
