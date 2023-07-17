package org.egov.infra.mdms.repository.querybuilder;

import org.egov.infra.mdms.model.MdmsCriteria;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class MdmsDataQueryBuilder {

    private static String SEARCH_MDMS_DATA_QUERY = "SELECT data.tenantid, data.uniqueidentifier, data.schemacode, data.data, data.isactive, data.createdby, data.lastmodifiedby, data.createdtime, data.lastmodifiedtime" +
            " FROM eg_mdms_data data ";


    public String getMdmsDataSearchQuery(MdmsCriteria mdmsCriteria, List<Object> preparedStmtList) {
        String query = buildQuery(mdmsCriteria, preparedStmtList);
        query = addOrderByClause(query);
        // query = addPagination(query, schemaDefCriteria, preparedStmtList);
        return query;
    }

    private String buildQuery(MdmsCriteria mdmsCriteria, List<Object> preparedStmtList) {
        StringBuilder builder = new StringBuilder(SEARCH_MDMS_DATA_QUERY);
        Map<String, String> schemaCodeFilterMap = mdmsCriteria.getSchemaCodeFilterMap();
        if (!Objects.isNull(mdmsCriteria.getTenantId())) {
            addClauseIfRequired(builder, preparedStmtList);
            builder.append(" data.tenantid = ? ");
            preparedStmtList.add(mdmsCriteria.getTenantId());
        }
        if (!Objects.isNull(mdmsCriteria.getIds())) {
            addClauseIfRequired(builder, preparedStmtList);
            builder.append(" data.id IN ( ").append(createQuery(mdmsCriteria.getIds())).append(" )");
            addToPreparedStatement(preparedStmtList, mdmsCriteria.getIds());
        }
        if (!Objects.isNull(mdmsCriteria.getUniqueIdentifier())) {
            addClauseIfRequired(builder, preparedStmtList);
            builder.append(" data.uniqueidentifier = ? ");
            preparedStmtList.add(mdmsCriteria.getUniqueIdentifier());
        }
        if (!Objects.isNull(mdmsCriteria.getSchemaCodeFilterMap())) {
            addClauseIfRequired(builder, preparedStmtList);
            builder.append(" data.schemacode IN ( ").append(createQuery(schemaCodeFilterMap.keySet())).append(" )");
            addToPreparedStatement(preparedStmtList, schemaCodeFilterMap.keySet());
        }
        return builder.toString();
    }

    private static void addClauseIfRequired(StringBuilder query, List<Object> preparedStmtList){
        if(preparedStmtList.isEmpty()){
            query.append(" WHERE ");
        }else{
            query.append(" AND ");
        }
    }

    private static String createQuery(Set<String> codes) {
        StringBuilder builder = new StringBuilder();
        int length = codes.size();
        for (int i = 0; i < length; i++) {
            builder.append(" ?");
            if (i != length - 1)
                builder.append(",");
        }
        return builder.toString();
    }
    private static void addToPreparedStatement(List<Object> preparedStmtList, Set<String> ids) {
        ids.forEach(id -> {
            preparedStmtList.add(id);
        });
    }


    private static String addOrderByClause(String query){
        return query + " order by data.createdtime desc ";
    }

    //TODO
    /*private static String addPagination(String query, SchemaDefCriteria schemaDefCriteria, List<Object>
            preparedStmtList){
        if (schemaDefCriteria.getLimit() > 0) {
            query = query + " limit ? ";
            preparedStmtList.add(schemaDefCriteria.getLimit());
        }
        if (schemaDefCriteria.getOffset() > 0) {
            query = query + " offset ? ";
            preparedStmtList.add(schemaDefCriteria.getOffset());
        }

        return query;
    }*/
}
