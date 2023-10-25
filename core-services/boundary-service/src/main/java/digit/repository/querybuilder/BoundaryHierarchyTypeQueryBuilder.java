package digit.repository.querybuilder;

import digit.web.models.BoundaryTypeHierarchySearchCriteria;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

@Component
public class BoundaryHierarchyTypeQueryBuilder {

    private static String BOUNDARY_HIERARCHY_TYPE_BASE_SEARCH_QUERY = "SELECT id, tenantid, hierarchytype, boundaryhierarchy, createdtime, lastmodifiedtime, createdby, lastmodifiedby" +
            " FROM boundary_hierarchy ";

    private static String ORDER_BY_CLAUSE = " order by createdtime desc ";

    public String getBoundaryHierarchyTypeSearchQuery(BoundaryTypeHierarchySearchCriteria boundaryTypeHierarchySearchCriteria, List<Object> preparedStmtList) {
        String query = buildQuery(boundaryTypeHierarchySearchCriteria, preparedStmtList);
//        query = QueryUtil.addOrderByClause(query, ORDER_BY_CLAUSE);
        query = query + ORDER_BY_CLAUSE;
        return query;
    }

    private String buildQuery(BoundaryTypeHierarchySearchCriteria boundaryTypeHierarchySearchCriteria, List<Object> preparedStmtList) {
        StringBuilder builder = new StringBuilder(BOUNDARY_HIERARCHY_TYPE_BASE_SEARCH_QUERY);

        if (!ObjectUtils.isEmpty(boundaryTypeHierarchySearchCriteria.getTenantId())) {
            addClauseIfRequired(builder, preparedStmtList);
            builder.append(" tenantid = ? ");
            preparedStmtList.add(boundaryTypeHierarchySearchCriteria.getTenantId());
        }

        if (!ObjectUtils.isEmpty(boundaryTypeHierarchySearchCriteria.getHierarchyType())) {
            addClauseIfRequired(builder, preparedStmtList);
            builder.append(" hierarchytype = ? ");
            preparedStmtList.add(boundaryTypeHierarchySearchCriteria.getHierarchyType());
        }

        return builder.toString();
    }

    /**
     * This method aids in adding "WHERE" clause and "AND" condition depending on preparedStatementList i.e.,
     * if preparedStatementList is empty, it will understand that it is the first clause being added so it
     * will add "WHERE" to the query and otherwise it will
     * @param query
     * @param preparedStmtList
     */
    public static void addClauseIfRequired(StringBuilder query, List<Object> preparedStmtList){
        if(preparedStmtList.isEmpty()){
            query.append(" WHERE ");
        }else{
            query.append(" AND ");
        }
    }

}
