package digit.repository.querybuilder;

import digit.web.models.EstimateSearch;
import digit.web.models.ProgramSearch;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Component
public class EstimateQueryBuilder {

	private static final String BASE_ES_QUERY = " SELECT es.id as eid, es.tenantid as etenantId, es.programcode as eprogramCode, es.projectcode as eprojectCode, es.estimateid as eestimateId, es.description as edescription, es.netamount as enetAmount, es.grossamount as egrossAmount, es.statuscode as estatusCode, es.statusmessage as estatusMessage, es.additionaldetails as eadditionalDetails, es.createdby as ecreatedBy, es.lastmodifiedby as elastModifiedBy, es.createdtime as ecreatedTime, es.lastmodifiedtime as elastModifiedTime ";

	private static final String FROM_TABLES = " FROM eg_es_estimate es ";

	private final String ORDERBY_CREATEDTIME = " ORDER BY es.createdtime DESC ";

	public String getEstimateSearchQuery(EstimateSearch criteria, List<Object> preparedStmtList){
		StringBuilder query = new StringBuilder(BASE_ES_QUERY);
		query.append(FROM_TABLES);

		if(!ObjectUtils.isEmpty(criteria.getTenantId())){
			addClauseIfRequired(query, preparedStmtList);
			query.append(" es.tenantid = ? ");
			preparedStmtList.add(criteria.getTenantId());
		}
		if(!CollectionUtils.isEmpty(criteria.getIds())){
			addClauseIfRequired(query, preparedStmtList);
			query.append(" es.id IN ( ").append(createQuery(criteria.getIds())).append(" ) ");
			addToPreparedStatement(preparedStmtList, criteria.getIds());
		}
		if(!ObjectUtils.isEmpty(criteria.getProgramCode())){
			addClauseIfRequired(query, preparedStmtList);
			query.append(" es.programCode = ? ");
			preparedStmtList.add(criteria.getProgramCode());
		}
		if(!ObjectUtils.isEmpty(criteria.getProjectCode())){
			addClauseIfRequired(query, preparedStmtList);
			query.append(" es.projectcode = ? ");
			preparedStmtList.add(criteria.getProjectCode());
		}
		if(!ObjectUtils.isEmpty(criteria.getEstimateId())){
			addClauseIfRequired(query, preparedStmtList);
			query.append(" es.estimateid = ? ");
			preparedStmtList.add(criteria.getEstimateId());
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