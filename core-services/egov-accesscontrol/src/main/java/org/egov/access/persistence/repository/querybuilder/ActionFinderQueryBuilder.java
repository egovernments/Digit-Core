package org.egov.access.persistence.repository.querybuilder;

import org.egov.access.domain.criteria.ActionSearchCriteria;

import java.util.ArrayList;
import java.util.List;

public class ActionFinderQueryBuilder implements BaseQueryBuilder {
	private ActionSearchCriteria criteria;
	private List<String> filters = new ArrayList<>();
	private List<Object> parameters = new ArrayList<>();
	private String BASE_QUERY = "SELECT distinct a.id AS a_id, a.name AS a_name, a.url AS "
			+ "a_url, a.servicecode AS a_servicecode, a.queryparams AS a_queryparams, a.parentmodule AS a_parentmodule, a.displayname AS a_displayname, a.enabled AS a_enabled, "
			+ " a.createdby AS a_createdby, a.createddate AS a_createddate, a.lastmodifiedby"
			+ " AS a_lastmodifiedby, a.lastmodifieddate AS a_lastmodifieddate,a.ordernumber AS a_ordernumber, ra.tenantid AS ra_tenantid, "
			+ "ra.actionid AS ra_action, ra.rolecode AS ra_rolecode FROM eg_action AS a JOIN eg_roleaction AS ra ON a.id = ra.actionid";

	public ActionFinderQueryBuilder(ActionSearchCriteria criteria) {
		this.criteria = criteria;
	}

	@Override
	public String build() {
		filterTenantId();
		filterRoleCodes();
		String filterQuery = filters.isEmpty() ? "" : " WHERE " + String.join(" AND ", filters);
		return BASE_QUERY + filterQuery + " ORDER BY a.name";
	}

	@Override
	public Object[] getParameters() {
		return parameters.toArray();
	}

	private void filterTenantId() {
		if (shouldFilterTenantId()) {
			filters.add("ra.tenantid = ?");
			parameters.add(criteria.getTenantId());
		}
	}

	private boolean shouldFilterTenantId() {
		return criteria.getTenantId() != null && !criteria.getTenantId().isEmpty();
	}

	private boolean shouldFilterRoleCodes() {
		return criteria != null && criteria.getRoleCodes() != null && !criteria.getRoleCodes().isEmpty();
	}

	private void filterRoleCodes() {
		if (shouldFilterRoleCodes()) {
			List<String> placeholders = new ArrayList<>();
			for (String roleName : criteria.getRoleCodes()) {
				placeholders.add("?");
				parameters.add(roleName);
			}
			filters.add("ra.rolecode in (" + String.join(",", placeholders) + ")");
		}
	}
}
