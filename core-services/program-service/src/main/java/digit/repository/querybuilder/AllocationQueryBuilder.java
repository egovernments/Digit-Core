package digit.repository.querybuilder;

import digit.web.models.AllocationSearch;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Component
public class AllocationQueryBuilder {

	private static final String BASE_AL_QUERY = " SELECT al.id as aid, al.tenantid as atenantId, al.programcode as aprogramCode, al.projectcode as aprojectCode, al.sanctionid as asanctionId, al.allocationid as aallocationId, al.netamount as anetAmount, al.grossamount as agrossAmount, al.allocationtype as aallocationType, al.statuscode as astatusCode, al.statusmessage as astatusMessage, al.additionaldetails as aadditionalDetails, al.createdby as acreatedBy, al.lastmodifiedby as alastModifiedBy, al.createdtime as acreatedTime, al.lastmodifiedtime as alastModifiedTime ";

	private static final String FROM_TABLES = " FROM eg_al_allocation al ";

	private final String ORDERBY_CREATEDTIME = " ORDER BY al.createdtime DESC ";

	public String getAllocationSearchQuery(AllocationSearch criteria, List<Object> preparedStmtList) {
		StringBuilder query = new StringBuilder(BASE_AL_QUERY);
		query.append(FROM_TABLES);

		if (!ObjectUtils.isEmpty(criteria.getTenantId())) {
			addClauseIfRequired(query, preparedStmtList);
			query.append(" al.tenantid = ? ");
			preparedStmtList.add(criteria.getTenantId());
		}
		if (!CollectionUtils.isEmpty(criteria.getIds())) {
			addClauseIfRequired(query, preparedStmtList);
			query.append(" al.id IN ( ").append(createQuery(criteria.getIds())).append(" ) ");
			addToPreparedStatement(preparedStmtList, criteria.getIds());
		}
		if (!ObjectUtils.isEmpty(criteria.getProgramCode())) {
			addClauseIfRequired(query, preparedStmtList);
			query.append(" al.programcode = ? ");
			preparedStmtList.add(criteria.getProgramCode());
		}
		if (!ObjectUtils.isEmpty(criteria.getProjectCode())) {
			addClauseIfRequired(query, preparedStmtList);
			query.append(" al.projectcode = ? ");
			preparedStmtList.add(criteria.getProjectCode());
		}
		if (!ObjectUtils.isEmpty(criteria.getSanctionId())) {
			addClauseIfRequired(query, preparedStmtList);
			query.append(" al.sanctionid = ? ");
			preparedStmtList.add(criteria.getSanctionId());
		}
		if (!ObjectUtils.isEmpty(criteria.getAllocationId())) {
			addClauseIfRequired(query, preparedStmtList);
			query.append(" al.allocationid = ? ");
			preparedStmtList.add(criteria.getAllocationId());
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