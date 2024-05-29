package digit.repository.querybuilder;

import digit.web.models.AgencySearch;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Component
public class AgencyQueryBuilder {

	private static final String BASE_AG_QUERY = " SELECT ag.id as aid, ag.tenantid as atenantId, ag.agencyid as aagencyId, ag.agencytype as aagencyType, ag.programcode as aprogramCode, ag.orgnumber as aorgNumber, ag.createdby as acreatedBy, ag.lastmodifiedby as alastModifiedBy, ag.createdtime as acreatedTime, ag.lastmodifiedtime as alastModifiedTime ";

	private static final String FROM_TABLES = " FROM eg_ag_agency ag ";

	private final String ORDERBY_CREATEDTIME = " ORDER BY ag.createdtime DESC ";

	public String getAgencySearchQuery(AgencySearch criteria, List<Object> preparedStmtList) {
		StringBuilder query = new StringBuilder(BASE_AG_QUERY);
		query.append(FROM_TABLES);

		if (!ObjectUtils.isEmpty(criteria.getTenantId())) {
			addClauseIfRequired(query, preparedStmtList);
			query.append(" ag.tenantid = ? ");
			preparedStmtList.add(criteria.getTenantId());
		}
		if (!ObjectUtils.isEmpty(criteria.getAgencyType())) {
			addClauseIfRequired(query, preparedStmtList);
			query.append(" ag.agencytype = ? ");
			preparedStmtList.add(criteria.getAgencyType());
		}
		if (!CollectionUtils.isEmpty(criteria.getIds())) {
			addClauseIfRequired(query, preparedStmtList);
			query.append(" ag.id IN ( ").append(createQuery(criteria.getIds())).append(" ) ");
			addToPreparedStatement(preparedStmtList, criteria.getIds());
		}
		if (!ObjectUtils.isEmpty(criteria.getAgencyId())) {
			addClauseIfRequired(query, preparedStmtList);
			query.append(" ag.agencyid = ? ");
			preparedStmtList.add(criteria.getAgencyId());
		}
		if (!ObjectUtils.isEmpty(criteria.getProgramCode())) {
			addClauseIfRequired(query, preparedStmtList);
			query.append(" ag.programcode = ? ");
			preparedStmtList.add(criteria.getProgramCode());
		}
		if (!ObjectUtils.isEmpty(criteria.getOrgNumber())) {
			addClauseIfRequired(query, preparedStmtList);
			query.append(" ag.orgnumber = ? ");
			preparedStmtList.add(criteria.getOrgNumber());
		}

		// order applications based on their createdtime in latest first manner
		query.append(ORDERBY_CREATEDTIME);

		return query.toString();
	}

	private void addClauseIfRequired(StringBuilder query, List<Object> preparedStmtList) {
		if (preparedStmtList.isEmpty()) {
			query.append(" WHERE ");
		} else {
			query.append(" AND ");
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
		ids.forEach(id -> {
			preparedStmtList.add(id);
		});
	}
}