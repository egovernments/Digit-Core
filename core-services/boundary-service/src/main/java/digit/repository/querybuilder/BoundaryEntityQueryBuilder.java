package digit.repository.querybuilder;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BoundaryEntityQueryBuilder {

    private static final String SEARCH_BOUNDARY_ENTITY_QUERY = "SELECT * FROM boundary WHERE code IN ";
    private static final String SEARCH_GEOMETRY_QUERY = "SELECT * FROM geometry WHERE id IN ";

    public String buildSearchBoundaryQuery(List<String> codes) {
        StringBuilder queryBuilder = new StringBuilder(SEARCH_BOUNDARY_ENTITY_QUERY);
        return buildQuery(queryBuilder, codes);
    }
    public String buildSearchGeometryQuery(List<String> ids) {
        StringBuilder queryBuilder = new StringBuilder(SEARCH_GEOMETRY_QUERY);
        return buildQuery(queryBuilder, ids);
    }
    public String buildQuery(StringBuilder queryBuilder, List<String> codes) {
        queryBuilder.append("(");
        for (int i = 0; i < codes.size(); i++) {
            queryBuilder.append("?");
            if (i != codes.size() - 1) {
                queryBuilder.append(",");
            }
        }
        queryBuilder.append(")");
        return queryBuilder.toString();
    }
}
