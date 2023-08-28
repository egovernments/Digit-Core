package org.egov.infra.mdms.repository.querybuilder;

import org.egov.infra.mdms.model.MdmsCriteria;
import org.egov.infra.mdms.utils.QueryUtil;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class MdmsDataQueryBuilder {

    private static String SEARCH_MDMS_DATA_QUERY = "SELECT data.tenantid, data.uniqueidentifier, data.schemacode, data.data, data.isactive, data.createdby, data.lastmodifiedby, data.createdtime, data.lastmodifiedtime" +
            " FROM eg_mdms_data data ";

    private static final String MDMS_DATA_QUERY_ORDER_BY_CLAUSE = " order by data.createdtime desc ";

    /**
     * Method to handle request for fetching MDMS data search query
     * @param mdmsCriteria
     * @param preparedStmtList
     * @return
     */
    public String getMdmsDataSearchQuery(MdmsCriteria mdmsCriteria, List<Object> preparedStmtList) {
        String query = buildQuery(mdmsCriteria, preparedStmtList);
        query = QueryUtil.addOrderByClause(query, MDMS_DATA_QUERY_ORDER_BY_CLAUSE);
        return query;
    }

    /**
     * Method to build query dynamically based on the criteria passed to the method
     * @param mdmsCriteria
     * @param preparedStmtList
     * @return
     */
    private String buildQuery(MdmsCriteria mdmsCriteria, List<Object> preparedStmtList) {
        StringBuilder builder = new StringBuilder(SEARCH_MDMS_DATA_QUERY);
        Map<String, String> schemaCodeFilterMap = mdmsCriteria.getSchemaCodeFilterMap();
        if (!Objects.isNull(mdmsCriteria.getTenantId())) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" data.tenantid LIKE ? ");
            preparedStmtList.add(mdmsCriteria.getTenantId() + "%");
        }
        if (!Objects.isNull(mdmsCriteria.getIds())) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" data.id IN ( ").append(QueryUtil.createQuery(mdmsCriteria.getIds().size())).append(" )");
            QueryUtil.addToPreparedStatement(preparedStmtList, mdmsCriteria.getIds());
        }
        if (!Objects.isNull(mdmsCriteria.getUniqueIdentifier())) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" data.uniqueidentifier = ? ");
            preparedStmtList.add(mdmsCriteria.getUniqueIdentifier());
        }
        if (!Objects.isNull(mdmsCriteria.getSchemaCodeFilterMap())) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" data.schemacode IN ( ").append(QueryUtil.createQuery(schemaCodeFilterMap.keySet().size())).append(" )");
            QueryUtil.addToPreparedStatement(preparedStmtList, schemaCodeFilterMap.keySet());
        }
        if(!Objects.isNull(mdmsCriteria.getIsActive())) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" data.isactive = ? ");
            preparedStmtList.add(mdmsCriteria.getIsActive());
        }
        return builder.toString();
    }

}
