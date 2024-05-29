package digit.repository.querybuilder;

import digit.web.models.ProgramSearch;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Component
public class ProgramQueryBuilder {

	private static final String BASE_PG_QUERY = " SELECT pg.id as pid, pg.tenantid as ptenantId, pg.programcode as pprogramCode, pg.programid as pprogramId, pg.name as pname, pg.description as pdescription, pg.startdate as pstartDate, pg.enddate as pendDate, pg.objective as pobjective, pg.criteria as pcriteria, pg.statuscode as pstatusCode, pg.statusmessage as pstatusMessage, pg.additionaldetails as padditionalDetails, pg.createdby as pcreatedBy, pg.lastmodifiedby as plastModifiedBy, pg.createdtime as pcreatedTime, pg.lastmodifiedtime as plastModifiedTime ";

	private static final String FROM_TABLES = " FROM eg_pg_program pg ";

	private final String ORDERBY_CREATEDTIME = " ORDER BY pg.createdtime DESC ";

	public String getProgramSearchQuery(ProgramSearch criteria, List<Object> preparedStmtList){
		StringBuilder query = new StringBuilder(BASE_PG_QUERY);
		query.append(FROM_TABLES);

		if(!ObjectUtils.isEmpty(criteria.getTenantId())){
			addClauseIfRequired(query, preparedStmtList);
			query.append(" pg.tenantid = ? ");
			preparedStmtList.add(criteria.getTenantId());
		}
		if(!ObjectUtils.isEmpty(criteria.getStatusCode())){
			addClauseIfRequired(query, preparedStmtList);
			query.append(" pg.statuscode = ? ");
			preparedStmtList.add(criteria.getStatusCode());
		}
		if(!CollectionUtils.isEmpty(criteria.getIds())){
			addClauseIfRequired(query, preparedStmtList);
			query.append(" pg.id IN ( ").append(createQuery(criteria.getIds())).append(" ) ");
			addToPreparedStatement(preparedStmtList, criteria.getIds());
		}
		if(!ObjectUtils.isEmpty(criteria.getProgramId())){
			addClauseIfRequired(query, preparedStmtList);
			query.append(" pg.programid = ? ");
			preparedStmtList.add(criteria.getProgramId());
		}
		if(!ObjectUtils.isEmpty(criteria.getProgramCode())){
			addClauseIfRequired(query, preparedStmtList);
			query.append(" pg.programcode = ? ");
			preparedStmtList.add(criteria.getProgramCode());
		}

		// order applications based on their createdtime in latest first manner
		query.append(ORDERBY_CREATEDTIME);

		return query.toString();
	}

	private void addClauseIfRequired(StringBuilder query, List<Object> preparedStmtList){
		if(preparedStmtList.isEmpty()){
			query.append(" WHERE ");
		}else{
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