package org.egov.infra.mdms.repository.querybuilder;

import org.egov.infra.mdms.model.SchemaDefCriteria;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SchemaDefinitionQueryBuilder {
    private static String SEARCH_SCHEMA_DEF_QUERY = "SELECT schema.id,schema.tenantid, schema.code, schema.description, schema.definition, schema.isactive, " +
            "schema.createdby, schema.lastmodifiedby, schema.createdtime, schema.lastmodifiedtime FROM " +
            "eg_mdms_schema_definition schema ";


    public static String getSchemaSearchQuery(SchemaDefCriteria schemaDefCriteria, List<Object> preparedStmtList) {
        String query = buildQuery(schemaDefCriteria, preparedStmtList);
        query = addOrderByClause(query);
       // query = addPagination(query, schemaDefCriteria, preparedStmtList);
        return query;
    }

    private static String buildQuery(SchemaDefCriteria schemaDefCriteria, List<Object> preparedStmtList) {
        StringBuilder builder = new StringBuilder(SchemaDefinitionQueryBuilder.SEARCH_SCHEMA_DEF_QUERY);
        Map<String, Object> queryParams = new HashMap<>();

        if (!Objects.isNull(schemaDefCriteria.getTenantId())) {
            addClauseIfRequired(builder, preparedStmtList);
            builder.append(" schema.tenantid = ? ");
            preparedStmtList.add(schemaDefCriteria.getTenantId());
        }
        if (!Objects.isNull(schemaDefCriteria.getCodes())) {
            addClauseIfRequired(builder, preparedStmtList);
            builder.append(" schema.code IN ( ").append(createQuery(schemaDefCriteria.getCodes())).append(" )");
            addToPreparedStatement(preparedStmtList, schemaDefCriteria.getCodes());
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

    private static String createQuery(List<String> codes) {
        StringBuilder builder = new StringBuilder();
        int length = codes.size();
        for (int i = 0; i < length; i++) {
            builder.append(" ?");
            if (i != length - 1)
                builder.append(",");
        }
        return builder.toString();
    }
    private static void addToPreparedStatement(List<Object> preparedStmtList, List<String> ids) {
        ids.forEach(id -> {
            preparedStmtList.add(id);
        });
    }

    private static String addOrderByClause(String query){
        return query + " order by schema.createdtime desc ";
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
