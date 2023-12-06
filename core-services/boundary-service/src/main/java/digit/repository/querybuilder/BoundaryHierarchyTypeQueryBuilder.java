package digit.repository.querybuilder;

import digit.config.ApplicationProperties;
import digit.util.QueryUtil;
import digit.web.models.BoundarySearchCriteria;
import digit.web.models.BoundaryTypeHierarchyDefinition;
import digit.web.models.BoundaryTypeHierarchySearchCriteria;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import java.util.List;

@Component
public class BoundaryHierarchyTypeQueryBuilder {

    private ApplicationProperties config;

    public BoundaryHierarchyTypeQueryBuilder(ApplicationProperties config) {
        this.config = config;
    }

    private static String BOUNDARY_HIERARCHY_TYPE_BASE_SEARCH_QUERY = "SELECT id, tenantid, hierarchytype, boundaryhierarchy, createdtime, lastmodifiedtime, createdby, lastmodifiedby" +
            " FROM boundary_hierarchy ";

    private static String ORDER_BY_CLAUSE = " order by createdtime desc ";

    private static String BOUNDARY_HIERARCHY_TYPE_COUNT_QUERY = "SELECT count(*) FROM boundary_hierarchy ";

    public String getBoundaryHierarchyTypeSearchQuery(BoundaryTypeHierarchySearchCriteria boundaryTypeHierarchySearchCriteria, List<Object> preparedStmtList) {
        String query = buildQuery(boundaryTypeHierarchySearchCriteria, preparedStmtList, BOUNDARY_HIERARCHY_TYPE_BASE_SEARCH_QUERY);
        query = QueryUtil.addOrderByClause(query, ORDER_BY_CLAUSE);
        query = getPaginatedQuery(query , boundaryTypeHierarchySearchCriteria , preparedStmtList);
        return query;
    }

    public String getBoundaryHierarchyTypeCountQuery(BoundaryTypeHierarchySearchCriteria boundaryTypeHierarchySearchCriteria, List<Object> preparedStmtList) {
        String query = buildQuery(boundaryTypeHierarchySearchCriteria, preparedStmtList, BOUNDARY_HIERARCHY_TYPE_COUNT_QUERY);
        return query;
    }
    private String buildQuery(BoundaryTypeHierarchySearchCriteria boundaryTypeHierarchySearchCriteria, List<Object> preparedStmtList, String Query) {
        StringBuilder builder = new StringBuilder(Query);

        if (!ObjectUtils.isEmpty(boundaryTypeHierarchySearchCriteria.getTenantId())) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" tenantid = ? ");
            preparedStmtList.add(boundaryTypeHierarchySearchCriteria.getTenantId());
        }

        if (!ObjectUtils.isEmpty(boundaryTypeHierarchySearchCriteria.getHierarchyType())) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" hierarchytype = ? ");
            preparedStmtList.add(boundaryTypeHierarchySearchCriteria.getHierarchyType());
        }

        return builder.toString();
    }

    /**
     * Method to add pagination to the query
     * @param query
     * @param boundaryTypeHierarchySearchCriteria
     * @param preparedStmtList
     * @return
     */
    private String getPaginatedQuery(String query, BoundaryTypeHierarchySearchCriteria boundaryTypeHierarchySearchCriteria , List<Object> preparedStmtList) {
        StringBuilder paginatedQuery = new StringBuilder(query);

        // Append offset
        paginatedQuery.append(" OFFSET ? ");
        preparedStmtList.add(ObjectUtils.isEmpty(boundaryTypeHierarchySearchCriteria.getOffset()) ? config.getDefaultOffset() : boundaryTypeHierarchySearchCriteria.getOffset());

        // Append limit
        paginatedQuery.append(" LIMIT ? ");
        preparedStmtList.add(ObjectUtils.isEmpty(boundaryTypeHierarchySearchCriteria.getLimit()) ? config.getDefaultLimit() : (boundaryTypeHierarchySearchCriteria.getLimit() > config.getMaxDefaultLimit() ? config.getMaxDefaultLimit() : boundaryTypeHierarchySearchCriteria.getLimit()) );

        return paginatedQuery.toString();
    }
}
