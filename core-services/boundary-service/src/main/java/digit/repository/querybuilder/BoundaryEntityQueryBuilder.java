package digit.repository.querybuilder;

import digit.config.ApplicationProperties;
import digit.util.QueryUtil;
import digit.web.models.BoundarySearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.*;

@Component
public class BoundaryEntityQueryBuilder {

    private ApplicationProperties config;

    private static final String SEARCH_BOUNDARY_ENTITY_QUERY = "SELECT boundary.id , boundary.tenantid , boundary.createdtime , boundary.createdby , boundary.lastmodifiedby , boundary.lastmodifiedtime, boundary.code, boundary.geometry, boundary.additionaldetails FROM boundary";

    private static final String BOUNDARY_DATA_QUERY_ORDER_BY_CLAUSE = " order by createdtime desc ";

    public BoundaryEntityQueryBuilder(ApplicationProperties config) {
        this.config = config;
    }

    /**
     * Method to build query dynamically based on the criteria passed to the method
     * @param boundarySearchCriteria
     * @param preparedStmtList
     * @return
     */
    public String getBoundaryDataSearchQuery(BoundarySearchCriteria boundarySearchCriteria, List<Object> preparedStmtList) {
        String query = buildQuery(boundarySearchCriteria , preparedStmtList);
        query = QueryUtil.addOrderByClause(query , BOUNDARY_DATA_QUERY_ORDER_BY_CLAUSE);
        query = getPaginatedQuery(query , boundarySearchCriteria , preparedStmtList);
        return query;
    }

    /**
     * Method to build query dynamically based on the criteria passed to the method
     * @param boundarySearchCriteria
     * @param preparedStmtList
     * @return
     */
    private String buildQuery(BoundarySearchCriteria boundarySearchCriteria , List<Object> preparedStmtList) {
        StringBuilder builder = new StringBuilder(SEARCH_BOUNDARY_ENTITY_QUERY);

        if (!Objects.isNull(boundarySearchCriteria.getTenantId())) {
            QueryUtil.addClauseIfRequired(builder , preparedStmtList);
            builder.append(" boundary.tenantid = ? ");
            preparedStmtList.add(boundarySearchCriteria.getTenantId());
        }
        if (!Objects.isNull(boundarySearchCriteria.getCodes())) {
            QueryUtil.addClauseIfRequired(builder , preparedStmtList);
            builder.append(" boundary.code IN ( ").append(QueryUtil.createQuery(boundarySearchCriteria.getCodes().size())).append(" )");
            Set<String> codes = new HashSet<>(boundarySearchCriteria.getCodes());
            QueryUtil.addToPreparedStatement(preparedStmtList , codes);
        }
        return builder.toString();
    }

    /**
     * Method to add pagination to the query
     * @param query
     * @param boundarySearchCriteria
     * @param preparedStmtList
     * @return
     */
    private String getPaginatedQuery(String query, BoundarySearchCriteria boundarySearchCriteria , List<Object> preparedStmtList) {
        StringBuilder paginatedQuery = new StringBuilder(query);

        // Append offset
        paginatedQuery.append(" OFFSET ? ");
        preparedStmtList.add(ObjectUtils.isEmpty(boundarySearchCriteria.getOffset()) ? config.getDefaultOffset() : boundarySearchCriteria.getOffset());

        // Append limit
        paginatedQuery.append(" LIMIT ? ");
        preparedStmtList.add(ObjectUtils.isEmpty(boundarySearchCriteria.getLimit()) ? config.getDefaultLimit() : (boundarySearchCriteria.getLimit() > config.getMaxDefaultLimit() ? config.getMaxDefaultLimit() : boundarySearchCriteria.getLimit()) );

        return paginatedQuery.toString();
    }

}
