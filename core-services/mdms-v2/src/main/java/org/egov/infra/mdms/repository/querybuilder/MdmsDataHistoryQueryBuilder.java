package org.egov.infra.mdms.repository.querybuilder;

import org.egov.infra.mdms.config.ApplicationConfig;
import org.egov.infra.mdms.model.MdmsHistoryCriteria;
import org.egov.infra.mdms.utils.QueryUtil;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

import static org.egov.infra.mdms.errors.ErrorCodes.INVALID_HISTORY_TENANT;
import static org.egov.infra.mdms.errors.ErrorCodes.INVALID_HISTORY_TENANT_MSG;

@Component
public class MdmsDataHistoryQueryBuilder {

    @Autowired
    private ApplicationConfig config;

    private static final String SEARCH_MDMS_DATA_HISTORY_QUERY = "SELECT history.historyid, history.version, history.operation, history.id, history.tenantid, " +
            "history.uniqueidentifier, history.schemacode, history.data, history.isactive, history.createdby, history.lastmodifiedby, " +
            "history.createdtime, history.lastmodifiedtime, history.changedtime, history.dbuser, history.clientaddress, " +
            "history.applicationname, history.transactionid FROM eg_mdms_data_history history ";

    private static final String MDMS_DATA_HISTORY_ORDER_BY_CLAUSE = " order by history.historyid desc ";

    public String getMdmsDataHistorySearchQuery(MdmsHistoryCriteria criteria, List<Object> preparedStmtList) {
        String query = buildQuery(criteria, preparedStmtList);
        query = QueryUtil.addOrderByClause(query, MDMS_DATA_HISTORY_ORDER_BY_CLAUSE);
        query = getPaginatedQuery(query, criteria, preparedStmtList);
        return query;
    }

    private String buildQuery(MdmsHistoryCriteria criteria, List<Object> preparedStmtList) {
        StringBuilder builder = new StringBuilder(SEARCH_MDMS_DATA_HISTORY_QUERY);

        // tenantId is mandatory - without it the query would return the history of every tenant
        if (!StringUtils.hasText(criteria.getTenantId()))
            throw new CustomException(INVALID_HISTORY_TENANT, INVALID_HISTORY_TENANT_MSG);

        QueryUtil.addClauseIfRequired(builder, preparedStmtList);
        builder.append(" history.tenantid = ? ");
        preparedStmtList.add(criteria.getTenantId());

        if (!Objects.isNull(criteria.getSchemaCode())) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" history.schemacode = ? ");
            preparedStmtList.add(criteria.getSchemaCode());
        }
        if (!CollectionUtils.isEmpty(criteria.getUniqueIdentifiers())) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" history.uniqueidentifier IN ( ").append(QueryUtil.createQuery(criteria.getUniqueIdentifiers().size())).append(" )");
            QueryUtil.addToPreparedStatement(preparedStmtList, criteria.getUniqueIdentifiers());
        }
        if (!CollectionUtils.isEmpty(criteria.getIds())) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" history.id IN ( ").append(QueryUtil.createQuery(criteria.getIds().size())).append(" )");
            QueryUtil.addToPreparedStatement(preparedStmtList, criteria.getIds());
        }
        if (!Objects.isNull(criteria.getFromTime())) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" history.changedtime >= ? ");
            preparedStmtList.add(criteria.getFromTime());
        }
        if (!Objects.isNull(criteria.getToTime())) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" history.changedtime <= ? ");
            preparedStmtList.add(criteria.getToTime());
        }

        return builder.toString();
    }

    private String getPaginatedQuery(String query, MdmsHistoryCriteria criteria, List<Object> preparedStmtList) {
        StringBuilder paginatedQuery = new StringBuilder(query);

        paginatedQuery.append(" OFFSET ? ");
        preparedStmtList.add(ObjectUtils.isEmpty(criteria.getOffset()) ? config.getDefaultOffset() : criteria.getOffset());

        paginatedQuery.append(" LIMIT ? ");
        preparedStmtList.add(ObjectUtils.isEmpty(criteria.getLimit()) ? config.getDefaultLimit() : criteria.getLimit());

        return paginatedQuery.toString();
    }

}
