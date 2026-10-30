package digit.repository.querybuilder;

import digit.config.ApplicationProperties;
import digit.util.QueryUtil;
import digit.web.models.BoundaryIdentifierGenerateCriteria;
import digit.web.models.BoundaryIdentifierSearchCriteria;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Component
public class BoundaryIdentifierQueryBuilder {

    private ApplicationProperties config;

    private static final String SEARCH_BOUNDARY_IDENTIFIER_QUERY = "SELECT bi.id, bi.tenantid, bi.hierarchytype, bi.code, bi.identifierid, bi.createdtime, bi.createdby, bi.lastmodifiedtime, bi.lastmodifiedby FROM boundary_identifier bi";

    private static final String BOUNDARY_IDENTIFIER_QUERY_ORDER_BY_CLAUSE = " order by bi.createdtime desc, bi.id ";

    // Boundaries of the type in the hierarchy that are not mapped yet.
    private static final String CODES_WITHOUT_IDENTIFIER_QUERY = "SELECT br.code FROM boundary_relationship br WHERE br.tenantid = ? AND br.hierarchytype = ? AND br.boundarytype = ?"
            + " AND NOT EXISTS (SELECT 1 FROM boundary_identifier bi WHERE bi.tenantid = br.tenantid AND bi.hierarchytype = br.hierarchytype AND bi.code = br.code)"
            + " ORDER BY br.code ";

    private static final String CODES_IN_HIERARCHY_QUERY = "SELECT br.code FROM boundary_relationship br WHERE br.tenantid = ? AND br.hierarchytype = ? AND br.code IN ( ";

    private static final String EXISTING_IDENTIFIER_IDS_QUERY = "SELECT bi.identifierid FROM boundary_identifier bi WHERE bi.tenantid = ? AND bi.hierarchytype = ? AND bi.identifierid IN ( ";

    public BoundaryIdentifierQueryBuilder(ApplicationProperties config) {
        this.config = config;
    }

    /**
     * Method to build query dynamically based on the criteria passed to the method
     * @param criteria
     * @param preparedStmtList
     * @return
     */
    public String getBoundaryIdentifierSearchQuery(BoundaryIdentifierSearchCriteria criteria, List<Object> preparedStmtList) {
        String query = buildQuery(criteria, preparedStmtList);
        query = QueryUtil.addOrderByClause(query, BOUNDARY_IDENTIFIER_QUERY_ORDER_BY_CLAUSE);
        query = getPaginatedQuery(query, criteria, preparedStmtList);
        return query;
    }

    /**
     * Query for codes of the given boundaryType within the hierarchy that do not have an identifier
     * @param generateCriteria
     * @param preparedStmtList
     * @return
     */
    public String getCodesWithoutIdentifierQuery(BoundaryIdentifierGenerateCriteria generateCriteria, List<Object> preparedStmtList) {
        preparedStmtList.add(generateCriteria.getTenantId());
        preparedStmtList.add(generateCriteria.getHierarchyType());
        preparedStmtList.add(generateCriteria.getBoundaryType());
        return CODES_WITHOUT_IDENTIFIER_QUERY;
    }

    /**
     * Query for which of the given codes exist in the hierarchy
     * @param tenantId
     * @param hierarchyType
     * @param codes
     * @param preparedStmtList
     * @return
     */
    public String getCodesInHierarchyQuery(String tenantId, String hierarchyType, Collection<String> codes, List<Object> preparedStmtList) {
        return buildInQuery(CODES_IN_HIERARCHY_QUERY, tenantId, hierarchyType, codes, preparedStmtList);
    }

    /**
     * Query for which of the given identifierIds already exist in the hierarchy
     * @param tenantId
     * @param hierarchyType
     * @param identifierIds
     * @param preparedStmtList
     * @return
     */
    public String getExistingIdentifierIdsQuery(String tenantId, String hierarchyType, Collection<String> identifierIds, List<Object> preparedStmtList) {
        return buildInQuery(EXISTING_IDENTIFIER_IDS_QUERY, tenantId, hierarchyType, identifierIds, preparedStmtList);
    }

    private String buildInQuery(String baseQuery, String tenantId, String hierarchyType, Collection<String> values, List<Object> preparedStmtList) {
        Set<String> uniqueValues = new HashSet<>(values);
        preparedStmtList.add(tenantId);
        preparedStmtList.add(hierarchyType);
        QueryUtil.addToPreparedStatement(preparedStmtList, uniqueValues);
        return baseQuery + QueryUtil.createQuery(uniqueValues.size()) + " )";
    }

    private String buildQuery(BoundaryIdentifierSearchCriteria criteria, List<Object> preparedStmtList) {
        StringBuilder builder = new StringBuilder(SEARCH_BOUNDARY_IDENTIFIER_QUERY);

        if (!Objects.isNull(criteria.getTenantId())) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" bi.tenantid = ? ");
            preparedStmtList.add(criteria.getTenantId());
        }
        if (!Objects.isNull(criteria.getHierarchyType())) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" bi.hierarchytype = ? ");
            preparedStmtList.add(criteria.getHierarchyType());
        }
        if (!CollectionUtils.isEmpty(criteria.getCodes())) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            // Deduplicate first so the placeholder count matches the bind values.
            Set<String> codes = new HashSet<>(criteria.getCodes());
            builder.append(" bi.code IN ( ").append(QueryUtil.createQuery(codes.size())).append(" )");
            QueryUtil.addToPreparedStatement(preparedStmtList, codes);
        }
        if (!CollectionUtils.isEmpty(criteria.getIdentifierIds())) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            Set<String> identifierIds = new HashSet<>(criteria.getIdentifierIds());
            builder.append(" bi.identifierid IN ( ").append(QueryUtil.createQuery(identifierIds.size())).append(" )");
            QueryUtil.addToPreparedStatement(preparedStmtList, identifierIds);
        }
        return builder.toString();
    }

    /**
     * Pagination is applied only when asked for: without limit every matching record is returned,
     * and a passed limit is capped at boundary.max.default.limit.
     */
    private String getPaginatedQuery(String query, BoundaryIdentifierSearchCriteria criteria, List<Object> preparedStmtList) {
        StringBuilder paginatedQuery = new StringBuilder(query);

        if (!Objects.isNull(criteria.getOffset())) {
            paginatedQuery.append(" OFFSET ? ");
            preparedStmtList.add(criteria.getOffset());
        }

        if (!Objects.isNull(criteria.getLimit())) {
            paginatedQuery.append(" LIMIT ? ");
            preparedStmtList.add(Math.min(criteria.getLimit(), config.getMaxDefaultLimit()));
        }

        return paginatedQuery.toString();
    }

}
