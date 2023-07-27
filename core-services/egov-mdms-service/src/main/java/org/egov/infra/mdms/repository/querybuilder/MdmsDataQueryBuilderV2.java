package org.egov.infra.mdms.repository.querybuilder;

import com.google.gson.Gson;
import org.egov.infra.mdms.model.MdmsCriteria;
import org.egov.infra.mdms.model.MdmsCriteriaV2;
import org.egov.infra.mdms.utils.QueryUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;

import static org.egov.infra.mdms.utils.MDMSConstants.DOT_REGEX;
import static org.egov.infra.mdms.utils.MDMSConstants.DOT_SEPARATOR;

@Component
public class MdmsDataQueryBuilderV2 {

    private static final String SEARCH_MDMS_DATA_QUERY = "SELECT data.id, data.tenantid, data.uniqueidentifier, data.schemacode, data.data, data.isactive, data.createdby, data.lastmodifiedby, data.createdtime, data.lastmodifiedtime" +
            " FROM eg_mdms_data data ";

    private static final String MDMS_DATA_QUERY_ORDER_BY_CLAUSE = " order by data.createdtime desc ";

    /**
     * Method to handle request for fetching MDMS data search query
     * @param mdmsCriteriaV2
     * @param preparedStmtList
     * @return
     */
    public String getMdmsDataSearchQuery(MdmsCriteriaV2 mdmsCriteriaV2, List<Object> preparedStmtList) {
        String query = buildQuery(mdmsCriteriaV2, preparedStmtList);
        query = QueryUtil.addOrderByClause(query, MDMS_DATA_QUERY_ORDER_BY_CLAUSE);
        return query;
    }

    /**
     * Method to build query dynamically based on the criteria passed to the method
     * @param mdmsCriteriaV2
     * @param preparedStmtList
     * @return
     */
    private String buildQuery(MdmsCriteriaV2 mdmsCriteriaV2, List<Object> preparedStmtList) {
        StringBuilder builder = new StringBuilder(SEARCH_MDMS_DATA_QUERY);
        Map<String, String> schemaCodeFilterMap = mdmsCriteriaV2.getSchemaCodeFilterMap();
        if (!Objects.isNull(mdmsCriteriaV2.getTenantId())) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" data.tenantid LIKE ? ");
            preparedStmtList.add(mdmsCriteriaV2.getTenantId() + "%");
        }
        if (!Objects.isNull(mdmsCriteriaV2.getIds())) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" data.id IN ( ").append(QueryUtil.createQuery(mdmsCriteriaV2.getIds().size())).append(" )");
            QueryUtil.addToPreparedStatement(preparedStmtList, mdmsCriteriaV2.getIds());
        }
        if (!Objects.isNull(mdmsCriteriaV2.getUniqueIdentifier())) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" data.uniqueidentifier = ? ");
            preparedStmtList.add(mdmsCriteriaV2.getUniqueIdentifier());
        }
        if (!Objects.isNull(mdmsCriteriaV2.getSchemaCodeFilterMap())) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" data.schemacode IN ( ").append(QueryUtil.createQuery(schemaCodeFilterMap.keySet().size())).append(" )");
            QueryUtil.addToPreparedStatement(preparedStmtList, schemaCodeFilterMap.keySet());
        }
        if(!Objects.isNull(mdmsCriteriaV2.getSchemaCode())){
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" data.schemacode = ? ");
            preparedStmtList.add(mdmsCriteriaV2.getSchemaCode());
        }
        if(!CollectionUtils.isEmpty(mdmsCriteriaV2.getFilterMap())){
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" data.data @> CAST( ? AS jsonb )");
            String partialQueryJsonString = QueryUtil.preparePartialJsonStringFromFilterMap(mdmsCriteriaV2.getFilterMap());
            preparedStmtList.add(partialQueryJsonString);
        }
        return builder.toString();
    }

}
