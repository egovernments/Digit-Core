package org.egov.infra.mdms.repository.querybuilder;

import org.egov.infra.mdms.model.SchemaDefCriteria;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.ModuleDetail;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class MdmsDataQueryBuilder {

    private static String SEARCH_MDMS_DATA_QUERY = "SELECT data.tenantid, data.uniqueidentifier, data.schemacode, data.data, data.isactive, data.createdby, data.lastmodifiedby, data.createdtime, data.lastmodifiedtime" +
            " FROM eg_mdms_data data ";


    public String getMdmsDataSearchQuery(Set<String> schemaCodes, List<Object> preparedStmtList) {
        String query = buildQuery(schemaCodes, preparedStmtList);
        query = addOrderByClause(query);
        // query = addPagination(query, schemaDefCriteria, preparedStmtList);
        return query;
    }

    private static String buildQuery(Set<String> schemaCodes, List<Object> preparedStmtList) {
        StringBuilder builder = new StringBuilder(SEARCH_MDMS_DATA_QUERY);

        if (!Objects.isNull(schemaCodes)) {
            addClauseIfRequired(builder, preparedStmtList);
            builder.append(" data.schemacode IN ( ").append(createQuery(schemaCodes)).append(" )");
            addToPreparedStatement(preparedStmtList, schemaCodes);
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
