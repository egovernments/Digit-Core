package digit.repository.querybuilder;

import digit.web.models.ProgramSearch;
import digit.web.models.ProjectSearch;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Component
public class ProjectQueryBuilder {

	private static final String BASE_PJ_QUERY = " SELECT pj.id as pid, pj.tenantid as ptenantId, pj.programcode as pprogramCode, pj.agencyid as pagencyId, pj.projectcode as pprojectCode, pj.projectid as pprojectId, pj.name as pname, pj.description as pdescription, pj.statuscode as pstatusCode, pj.statusmessage as pstatusMessage, pj.additionaldetails as padditionalDetails, pj.createdby as pcreatedBy, pj.lastmodifiedby as plastModifiedBy, pj.createdtime as pcreatedTime, pj.lastmodifiedtime as plastModifiedTime ";

	private static final String FROM_TABLES = " FROM eg_pj_project pj ";

	private final String ORDERBY_CREATEDTIME = " ORDER BY pj.createdtime DESC ";

	public String getProjectSearchQuery(ProjectSearch criteria, List<Object> preparedStmtList){
		StringBuilder query = new StringBuilder(BASE_PJ_QUERY);
		query.append(FROM_TABLES);

		if(!ObjectUtils.isEmpty(criteria.getTenantId())){
			addClauseIfRequired(query, preparedStmtList);
			query.append(" pj.tenantid = ? ");
			preparedStmtList.add(criteria.getTenantId());
		}
		if(!CollectionUtils.isEmpty(criteria.getIds())){
			addClauseIfRequired(query, preparedStmtList);
			query.append(" pj.id IN ( ").append(createQuery(criteria.getIds())).append(" ) ");
			addToPreparedStatement(preparedStmtList, criteria.getIds());
		}
		if(!ObjectUtils.isEmpty(criteria.getProgramCode())){
			addClauseIfRequired(query, preparedStmtList);
			query.append(" pj.programcode = ? ");
			preparedStmtList.add(criteria.getProgramCode());
		}
		if(!ObjectUtils.isEmpty(criteria.getAgencyId())){
			addClauseIfRequired(query, preparedStmtList);
			query.append(" pj.agencyid = ? ");
			preparedStmtList.add(criteria.getAgencyId());
		}
		if(!ObjectUtils.isEmpty(criteria.getProjectCode())){
			addClauseIfRequired(query, preparedStmtList);
			query.append(" pj.projectcode = ? ");
			preparedStmtList.add(criteria.getProjectCode());
		}
		if(!ObjectUtils.isEmpty(criteria.getProjectId())){
			addClauseIfRequired(query, preparedStmtList);
			query.append(" pj.projectid = ? ");
			preparedStmtList.add(criteria.getProjectId());
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