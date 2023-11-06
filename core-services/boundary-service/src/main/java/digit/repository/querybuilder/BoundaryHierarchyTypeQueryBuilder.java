package digit.repository.querybuilder;

import digit.util.QueryUtil;
import digit.web.models.BoundaryTypeHierarchySearchCriteria;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import java.util.List;

@Component
public class BoundaryHierarchyTypeQueryBuilder {

    private static String BOUNDARY_HIERARCHY_TYPE_BASE_SEARCH_QUERY = "SELECT id, tenantid, hierarchytype, boundaryhierarchy, createdtime, lastmodifiedtime, createdby, lastmodifiedby" +
            " FROM boundary_hierarchy ";

    private static String ORDER_BY_CLAUSE = " order by createdtime desc ";

    public String getBoundaryHierarchyTypeSearchQuery(BoundaryTypeHierarchySearchCriteria boundaryTypeHierarchySearchCriteria, List<Object> preparedStmtList) {
        String query = buildQuery(boundaryTypeHierarchySearchCriteria, preparedStmtList);
        query = QueryUtil.addOrderByClause(query, ORDER_BY_CLAUSE);
        return query;
    }

    private String buildQuery(BoundaryTypeHierarchySearchCriteria boundaryTypeHierarchySearchCriteria, List<Object> preparedStmtList) {
        StringBuilder builder = new StringBuilder(BOUNDARY_HIERARCHY_TYPE_BASE_SEARCH_QUERY);

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
}
