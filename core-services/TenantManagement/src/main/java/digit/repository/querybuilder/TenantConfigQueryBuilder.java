package digit.repository.querybuilder;

import digit.config.ApplicationConfig;
import digit.util.QueryUtil;
import digit.web.models.TenantConfigSearchCriteria;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Objects;

@Component
public class TenantConfigQueryBuilder {

    private static final String SEARCH_TENANT_CONFIG_QUERY = "SELECT" +
            "    tc.id AS tenantConfigId," +
            "    tc.code," +
            "    tc.defaultLoginType," +
            "    tc.otpLength," +
            "    tc.name," +
            "    tc.languages," +
            "    tc.enableUserBasedLogin," +
            "    tc.additionalAttributes," +
            "    tc.isActive AS isActive," +
            "    tc.createdBy AS CreatedBy," +
            "    tc.lastModifiedBy AS LastModifiedBy," +
            "    tc.createdTime AS CreatedTime," +
            "    tc.lastModifiedTime AS LastModifiedTime," +
            "    td.id AS documentId," +
            "    td.tenantId," +
            "    td.type," +
            "    td.fileStoreId," +
            "    td.url," +
            "    td.isActive AS documentIsActive," +
            "    td.createdBy AS documentCreatedBy," +
            "    td.lastModifiedBy AS documentLastModifiedBy," +
            "    td.createdTime AS documentCreatedTime," +
            "    td.lastModifiedTime AS documentLastModifiedTime" +
            " FROM " +
            " tenant_config tc " +
            " LEFT JOIN " +
            " tenant_documents td ON tc.id = td.tenantConfigId";

    private static final String TENANT_CONFIG_QUERY_ORDER_BY_CLAUSE = "order by tc.createdtime desc ";

    private ApplicationConfig config;

    public TenantConfigQueryBuilder(ApplicationConfig config) {
        this.config = config;
    }

    /**
     * Method to handle request for fetching MDMS data search query
     *
     * @param TenantConfigSearchCriteria
     * @param preparedStmtList
     * @return
     */
    public String getTenantConfigSearchQuery(TenantConfigSearchCriteria TenantConfigSearchCriteria, List<Object> preparedStmtList) {
        String query = buildQuery(TenantConfigSearchCriteria, preparedStmtList);
        query = QueryUtil.addOrderByClause(query, TENANT_CONFIG_QUERY_ORDER_BY_CLAUSE);
//        query = getPaginatedQuery(query, TenantConfigSearchCriteria, preparedStmtList);
        return query;
    }

    /**
     * Method to build query dynamically based on the criteria passed to the method
     *
     * @param TenantConfigSearchCriteria
     * @param preparedStmtList
     * @return
     */
    private String buildQuery(TenantConfigSearchCriteria TenantConfigSearchCriteria, List<Object> preparedStmtList) {
        StringBuilder builder = new StringBuilder(SEARCH_TENANT_CONFIG_QUERY);
//        Map<String, String> schemaCodeFilterMap = TenantConfigSearchCriteria.getSchemaCodeFilterMap();
        if (!Objects.isNull(TenantConfigSearchCriteria.getCode())) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" tc.code = ? ");
            preparedStmtList.add(TenantConfigSearchCriteria.getCode());
        }

        return builder.toString();
    }

    private String getPaginatedQuery(String query, TenantConfigSearchCriteria TenantConfigSearchCriteria, List<Object> preparedStmtList) {
        StringBuilder paginatedQuery = new StringBuilder(query);

        // Append offset
        paginatedQuery.append(" OFFSET ? ");
        preparedStmtList.add(ObjectUtils.isEmpty(TenantConfigSearchCriteria.getOffset()) ? config.getDefaultOffset() : TenantConfigSearchCriteria.getOffset());

        // Append limit
        paginatedQuery.append(" LIMIT ? ");
        preparedStmtList.add(ObjectUtils.isEmpty(TenantConfigSearchCriteria.getLimit()) ? config.getDefaultLimit() : TenantConfigSearchCriteria.getLimit());

        return paginatedQuery.toString();
    }


}
