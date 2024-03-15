package digit.repository.querybuilder;

import digit.util.QueryUtil;
import digit.web.models.BoundaryRelationshipSearchCriteria;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import java.util.HashSet;
import java.util.List;

@Component
public class BoundaryRelationshipQueryBuilder {

    private static String BOUNDARY_RELATIONSHIP_BASE_SEARCH_QUERY = "SELECT id, tenantid, code, hierarchytype, boundarytype, parent, ancestralmaterializedpath, createdtime, createdby, lastmodifiedtime, lastmodifiedby" +
            " FROM boundary_relationship ";

    private static String ORDER_BY_CLAUSE = " order by createdtime desc ";

    public String getBoundaryRelationshipSearchQuery(BoundaryRelationshipSearchCriteria boundaryRelationshipSearchCriteria, List<Object> preparedStmtList) {
        String query = buildQuery(boundaryRelationshipSearchCriteria, preparedStmtList);
        query += ORDER_BY_CLAUSE;
        return query;
    }

    private String buildQuery(BoundaryRelationshipSearchCriteria boundaryRelationshipSearchCriteria, List<Object> preparedStmtList) {
        StringBuilder builder = new StringBuilder(BOUNDARY_RELATIONSHIP_BASE_SEARCH_QUERY);

        if (!ObjectUtils.isEmpty(boundaryRelationshipSearchCriteria.getTenantId())) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" tenantid = ? ");
            preparedStmtList.add(boundaryRelationshipSearchCriteria.getTenantId());
        }

        if (!ObjectUtils.isEmpty(boundaryRelationshipSearchCriteria.getHierarchyType())) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" hierarchytype = ? ");
            preparedStmtList.add(boundaryRelationshipSearchCriteria.getHierarchyType());
        }

        if(!ObjectUtils.isEmpty(boundaryRelationshipSearchCriteria.getParent())) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" parent = ? ");
            preparedStmtList.add(boundaryRelationshipSearchCriteria.getParent());
        }

        if(!boundaryRelationshipSearchCriteria.getIsSearchForRootNode()) {
            if (!ObjectUtils.isEmpty(boundaryRelationshipSearchCriteria.getBoundaryType())) {
                QueryUtil.addClauseIfRequired(builder, preparedStmtList);
                builder.append(" boundarytype = ? ");
                preparedStmtList.add(boundaryRelationshipSearchCriteria.getBoundaryType());
            }

            if (!CollectionUtils.isEmpty(boundaryRelationshipSearchCriteria.getCodes())) {
                QueryUtil.addClauseIfRequired(builder, preparedStmtList);
                builder.append(" code IN ( ").append(QueryUtil.createQuery(boundaryRelationshipSearchCriteria.getCodes().size())).append(" )");
                QueryUtil.addToPreparedStatement(preparedStmtList, new HashSet<>(boundaryRelationshipSearchCriteria.getCodes()));
            }
        }

        if(boundaryRelationshipSearchCriteria.getIsSearchForRootNode()) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" parent IS NULL ");
        }

        if(!CollectionUtils.isEmpty(boundaryRelationshipSearchCriteria.getCurrentBoundaryCodes())) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" ARRAY [ ").append(QueryUtil.createQuery(boundaryRelationshipSearchCriteria.getCurrentBoundaryCodes().size())).append(" ]").append("::text[] ");
            builder.append(" && string_to_array(ancestralmaterializedpath, '|') ");
            QueryUtil.addToPreparedStatement(preparedStmtList, new HashSet<>(boundaryRelationshipSearchCriteria.getCurrentBoundaryCodes()));
        }

        return builder.toString();
    }

}
