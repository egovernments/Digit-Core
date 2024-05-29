package digit.repository.querybuilder;

import digit.web.models.AllocationSearch;
import digit.web.models.DisburseSearch;
import digit.web.models.ProgramSearch;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Component
public class DisburseQueryBuilder {

	private static final String BASE_DI_QUERY = " SELECT di.id as did, di.tenantid as dtenantId, di.programcode as dprogramCode, di.projectcode as dprojectCode, di.disburseid as ddisburseId, di.targetid as dtargetId, di.transactionid as dtransactionId, di.sanctionid as dsanctionId, di.amountcode as damountCode, di.netamount as dnetAmount, di.grossamount as dgrossAmount, di.statuscode as dstatusCode, di.statusmessage as dstatusMessage, di.additionaldetails as dadditionalDetails, di.createdby as dcreatedBy, di.lastmodifiedby as dlastModifiedBy, di.createdtime as dcreatedTime, di.lastmodifiedtime as dlastModifiedTime ";

	private static final String FROM_TABLES = " FROM eg_di_disburse di ";

	private final String ORDERBY_CREATEDTIME = " ORDER BY di.createdtime DESC ";

	public String getDisburseSearchQuery(DisburseSearch criteria, List<Object> preparedStmtList){
		StringBuilder query = new StringBuilder(BASE_DI_QUERY);
		query.append(FROM_TABLES);

		if(!ObjectUtils.isEmpty(criteria.getTenantId())){
			addClauseIfRequired(query, preparedStmtList);
			query.append(" di.tenantid = ? ");
			preparedStmtList.add(criteria.getTenantId());
		}
		if(!CollectionUtils.isEmpty(criteria.getIds())){
			addClauseIfRequired(query, preparedStmtList);
			query.append(" di.id IN ( ").append(createQuery(criteria.getIds())).append(" ) ");
			addToPreparedStatement(preparedStmtList, criteria.getIds());
		}
		if(!ObjectUtils.isEmpty(criteria.getProgramCode())){
			addClauseIfRequired(query, preparedStmtList);
			query.append(" di.programcode = ? ");
			preparedStmtList.add(criteria.getProgramCode());
		}
		if(!ObjectUtils.isEmpty(criteria.getProjectCode())){
			addClauseIfRequired(query, preparedStmtList);
			query.append(" di.projectcode = ? ");
			preparedStmtList.add(criteria.getProjectCode());
		}
		if(!ObjectUtils.isEmpty(criteria.getDisburseId())){
			addClauseIfRequired(query, preparedStmtList);
			query.append(" di.disburseid = ? ");
			preparedStmtList.add(criteria.getDisburseId());
		}
		if(!ObjectUtils.isEmpty(criteria.getTargetId())){
			addClauseIfRequired(query, preparedStmtList);
			query.append(" di.targetid = ? ");
			preparedStmtList.add(criteria.getTargetId());
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