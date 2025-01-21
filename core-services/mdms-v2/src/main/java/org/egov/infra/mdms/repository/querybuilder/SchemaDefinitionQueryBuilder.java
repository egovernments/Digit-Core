package org.egov.infra.mdms.repository.querybuilder;

import org.egov.infra.mdms.config.ApplicationConfig;
import org.egov.infra.mdms.model.SchemaDefCriteria;
import org.egov.infra.mdms.utils.QueryUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.*;

@Component
public class SchemaDefinitionQueryBuilder {

    @Autowired
    private ApplicationConfig config;

    private static final String SEARCH_SCHEMA_DEF_QUERY = "SELECT schema.id,schema.tenantid, schema.code, schema.description, schema.definition, schema.isactive, " +
            "schema.createdby, schema.lastmodifiedby, schema.createdtime, schema.lastmodifiedtime FROM " +
            "eg_mdms_schema_definition schema ";

    private static final String SEARCH_SCHEMA_DEF_ORDER_BY_CLAUSE = " order by schema.createdtime desc ";

    /**
     * Method to handle request for fetching schema search query
     * @param schemaDefCriteria
     * @param preparedStmtList
     * @return
     */
    public String getSchemaSearchQuery(SchemaDefCriteria schemaDefCriteria, List<Object> preparedStmtList) {
        String query = buildQuery(schemaDefCriteria, preparedStmtList);
        query = QueryUtil.addOrderByClause(query, SEARCH_SCHEMA_DEF_ORDER_BY_CLAUSE);
        query = getPaginatedQuery(query, schemaDefCriteria, preparedStmtList);
        return query;
    }

    /**
     * Method to build query dynamically based on the criteria passed to the method
     * @param schemaDefCriteria
     * @param preparedStmtList
     * @return
     */
    private String buildQuery(SchemaDefCriteria schemaDefCriteria, List<Object> preparedStmtList) {
        StringBuilder builder = new StringBuilder(SchemaDefinitionQueryBuilder.SEARCH_SCHEMA_DEF_QUERY);

        if (!Objects.isNull(schemaDefCriteria.getTenantId())) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" schema.tenantid = ? ");
            preparedStmtList.add(schemaDefCriteria.getTenantId());
        }
        if (!Objects.isNull(schemaDefCriteria.getCodes())) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" schema.code IN ( ").append(QueryUtil.createQuery(schemaDefCriteria.getCodes().size())).append(" )");
            QueryUtil.addToPreparedStatement(preparedStmtList, new HashSet<>(schemaDefCriteria.getCodes()));
        }

        return builder.toString();
    }

    private String getPaginatedQuery(String query, SchemaDefCriteria schemaDefCriteria, List<Object> preparedStmtList) {
        StringBuilder paginatedQuery = new StringBuilder(query);

        // Append offset
        paginatedQuery.append(" OFFSET ? ");
        preparedStmtList.add(ObjectUtils.isEmpty(schemaDefCriteria.getOffset()) ? config.getDefaultOffset() : schemaDefCriteria.getOffset());

        // Append limit
        paginatedQuery.append(" LIMIT ? ");
        preparedStmtList.add(ObjectUtils.isEmpty(schemaDefCriteria.getLimit()) ? config.getDefaultLimit() : schemaDefCriteria.getLimit());

        return paginatedQuery.toString();
    }

}
