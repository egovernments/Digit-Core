package digit.repository.querybuilder;

import digit.web.models.SanctionSearch;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Component
public class SanctionQueryBuilder {

	private static final String BASE_SA_QUERY = " SELECT sa.id as sid, sa.tenantid as stenantId, sa.programcode as sprogramCode, sa.projectcode as sprojectCode, sa.sanctionid as ssanctionId, sa.netamount as snetAmount, sa.grossamount as sgrossAmount, sa.availableamount as savailableAmount, sa.statuscode as sstatusCode, sa.statusmessage as sstatusMessage, sa.additionaldetails as sadditionalDetails, sa.createdby as screatedBy, sa.lastmodifiedby as slastModifiedBy, sa.createdtime as screatedTime, sa.lastmodifiedtime as slastModifiedTime ";

	private static final String FROM_TABLES = " FROM eg_sa_sanction sa ";

	private final String ORDERBY_CREATEDTIME = " ORDER BY sa.createdtime DESC ";

	public String getSanctionSearchQuery(SanctionSearch criteria, List<Object> preparedStmtList) {
		StringBuilder query = new StringBuilder(BASE_SA_QUERY);
		query.append(FROM_TABLES);

		if (!ObjectUtils.isEmpty(criteria.getTenantId())) {
			addClauseIfRequired(query, preparedStmtList);
			query.append(" sa.tenantid = ? ");
			preparedStmtList.add(criteria.getTenantId());
		}
		if (!CollectionUtils.isEmpty(criteria.getIds())) {
			addClauseIfRequired(query, preparedStmtList);
			query.append(" sa.id IN ( ").append(createQuery(criteria.getIds())).append(" ) ");
			addToPreparedStatement(preparedStmtList, criteria.getIds());
		}
		if (!ObjectUtils.isEmpty(criteria.getProgramCode())) {
			addClauseIfRequired(query, preparedStmtList);
			query.append(" sa.programcode = ? ");
			preparedStmtList.add(criteria.getProgramCode());
		}
		if (!ObjectUtils.isEmpty(criteria.getProjectCode())) {
			addClauseIfRequired(query, preparedStmtList);
			query.append(" sa.projectcode = ? ");
			preparedStmtList.add(criteria.getProjectCode());
		}
		if (!ObjectUtils.isEmpty(criteria.getSanctionId())) {
			addClauseIfRequired(query, preparedStmtList);
			query.append(" sa.sanctionid = ? ");
			preparedStmtList.add(criteria.getSanctionId());
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