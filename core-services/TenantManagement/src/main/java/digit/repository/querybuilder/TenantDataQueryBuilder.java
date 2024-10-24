package digit.repository.querybuilder;

import digit.config.ApplicationConfig;
import digit.util.QueryUtil;
import digit.web.models.TenantDataSearchCriteria;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Objects;

@Component
public class TenantDataQueryBuilder {

    private static final String SEARCH_TENANT_DATA_QUERY = "SELECT data.id, data.code, data.name, data.email, " +
            "data.additionalAttributes, data.isActive, data.createdBy, data.lastModifiedBy, " +
            "data.createdTime, data.lastModifiedTime " +
            "FROM tenant data";

    private static final String TENANT_DATA_QUERY_ORDER_BY_CLAUSE = " order by data.createdtime desc ";

    private ApplicationConfig config;

    public TenantDataQueryBuilder(ApplicationConfig config) {
        this.config = config;
    }

    /**
     * Method to handle request for fetching MDMS data search query
     * @param tenantDataSearchCriteria
     * @param preparedStmtList
     * @return
     */
    public String getTenantDataSearchQuery(TenantDataSearchCriteria tenantDataSearchCriteria, List<Object> preparedStmtList) {
        String query = buildQuery(tenantDataSearchCriteria, preparedStmtList);
        query = QueryUtil.addOrderByClause(query, TENANT_DATA_QUERY_ORDER_BY_CLAUSE);
        // TODO: Making sure root tenant returns in paginated query
        // query = getPaginatedQuery(query, tenantDataSearchCriteria, preparedStmtList);
        return query;
    }

    /**
     * Method to build query dynamically based on the criteria passed to the method
     * @param tenantDataSearchCriteria
     * @param preparedStmtList
     * @return
     */
    private String buildQuery(TenantDataSearchCriteria tenantDataSearchCriteria, List<Object> preparedStmtList) {

        // TODO: n - level hierarchy

        StringBuilder builder = new StringBuilder(SEARCH_TENANT_DATA_QUERY);

        boolean includeSubTenants = tenantDataSearchCriteria.getIncludeSubTenants();

        if (!Objects.isNull(tenantDataSearchCriteria.getCode())) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            if (includeSubTenants) {
                // Search by parentId instead of code
                builder.append(" data.parentId = ? ");
            } else {
                // Default search by code
                builder.append(" data.code = ? ");
            }
            preparedStmtList.add(tenantDataSearchCriteria.getCode());
        }

        // TODO: Search based on name and flag combination
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