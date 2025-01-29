package digit.repository.querybuilder;

import digit.config.ApplicationConfig;
import digit.util.QueryUtil;
import digit.web.models.SubTenantDataSearchCriteria;
import digit.web.models.TenantDataSearchCriteria;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Objects;

@Component
public class SubTenantDataQueryBuilder {

    private static final String SEARCH_TENANT_DATA_QUERY = "SELECT data.id, data.code, data.tenantId, data.name, data.email, " +
            "data.additionalAttributes, data.isActive, data.createdBy, data.lastModifiedBy, " +
            "data.createdTime, data.lastModifiedTime " +
            "FROM tenant data";

    private static final String TENANT_DATA_QUERY_ORDER_BY_CLAUSE = " order by data.createdtime desc ";

    private ApplicationConfig config;

    public SubTenantDataQueryBuilder(ApplicationConfig config) {
        this.config = config;
    }

    /**
     * Method to handle request for fetching MDMS data search query
     * @param tenantDataSearchCriteria
     * @param preparedStmtList
     * @return
     */
    public String getTenantDataSearchQuery(SubTenantDataSearchCriteria tenantDataSearchCriteria, List<Object> preparedStmtList) {
        String query = buildQuery(tenantDataSearchCriteria, preparedStmtList);
        query = QueryUtil.addOrderByClause(query, TENANT_DATA_QUERY_ORDER_BY_CLAUSE);
//        query = getPaginatedQuery(query, tenantDataSearchCriteria, preparedStmtList);
        return query;
    }

    /**
     * Method to build query dynamically based on the criteria passed to the method
     * @param tenantDataSearchCriteria
     * @param preparedStmtList
     * @return
     */
    private String buildQuery(SubTenantDataSearchCriteria tenantDataSearchCriteria, List<Object> preparedStmtList) {
        StringBuilder builder = new StringBuilder(SEARCH_TENANT_DATA_QUERY);
//        Map<String, String> schemaCodeFilterMap = tenantDataSearchCriteria.getSchemaCodeFilterMap();
        if (!Objects.isNull(tenantDataSearchCriteria.getCode())) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" data.code = ? ");
            preparedStmtList.add(tenantDataSearchCriteria.getCode());
        }

        if(!Objects.isNull(tenantDataSearchCriteria.getName())){
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" data.name = ? ");
            preparedStmtList.add(tenantDataSearchCriteria.getName());
        }
        return builder.toString();
    }

    private String getPaginatedQuery(String query, TenantDataSearchCriteria tenantDataSearchCriteria, List<Object> preparedStmtList) {
        StringBuilder paginatedQuery = new StringBuilder(query);

        // Append offset
        paginatedQuery.append(" OFFSET ? ");
        preparedStmtList.add(ObjectUtils.isEmpty(tenantDataSearchCriteria.getOffset()) ? config.getDefaultOffset() : tenantDataSearchCriteria.getOffset());

        // Append limit
        paginatedQuery.append(" LIMIT ? ");
        preparedStmtList.add(ObjectUtils.isEmpty(tenantDataSearchCriteria.getLimit()) ? config.getDefaultLimit() : tenantDataSearchCriteria.getLimit());

        return paginatedQuery.toString();
    }


}
