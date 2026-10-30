package digit.repository.querybuilder;

import digit.config.ApplicationProperties;
import digit.web.models.BoundaryIdentifierSearchCriteria;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BoundaryIdentifierQueryBuilderTest {

    private final BoundaryIdentifierQueryBuilder queryBuilder = queryBuilder();

    private static BoundaryIdentifierQueryBuilder queryBuilder() {
        ApplicationProperties config = new ApplicationProperties();
        config.setMaxDefaultLimit(300);
        return new BoundaryIdentifierQueryBuilder(config);
    }

    @Test
    void searchWithOnlyTenantAndHierarchyReturnsEverythingUnpaginated() {
        List<Object> params = new ArrayList<>();
        String query = queryBuilder.getBoundaryIdentifierSearchQuery(
                BoundaryIdentifierSearchCriteria.builder().tenantId("ba").hierarchyType("ADMIN").build(), params);

        assertTrue(query.contains("bi.tenantid = ?"));
        assertTrue(query.contains("bi.hierarchytype = ?"));
        assertFalse(query.contains("LIMIT"));
        assertFalse(query.contains("OFFSET"));
        assertEquals(List.of("ba", "ADMIN"), params);
    }

    @Test
    void searchPaginatesOnlyWhenAskedAndCapsLimit() {
        List<Object> params = new ArrayList<>();
        String query = queryBuilder.getBoundaryIdentifierSearchQuery(BoundaryIdentifierSearchCriteria.builder()
                .tenantId("ba").hierarchyType("ADMIN").codes(List.of("B1")).offset(10).limit(1000).build(), params);

        assertTrue(query.contains("bi.code IN"));
        assertTrue(query.contains("OFFSET ?"));
        assertTrue(query.contains("LIMIT ?"));
        assertEquals(List.of("ba", "ADMIN", "B1", 10, 300), params);
    }
}
