package org.egov.infra.mdms.repository.querybuilder;

import org.egov.infra.mdms.config.ApplicationConfig;
import org.egov.infra.mdms.model.SchemaDefHistoryCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SchemaDefinitionHistoryQueryBuilderTest {

    private SchemaDefinitionHistoryQueryBuilder queryBuilder;

    @BeforeEach
    void setUp() {
        ApplicationConfig config = new ApplicationConfig();
        config.setDefaultOffset(0);
        config.setDefaultLimit(10);
        queryBuilder = new SchemaDefinitionHistoryQueryBuilder();
        ReflectionTestUtils.setField(queryBuilder, "config", config);
    }

    @Test
    void duplicateCodesAreCollapsed() {
        SchemaDefHistoryCriteria criteria = SchemaDefHistoryCriteria.builder().tenantId("pg")
                .codes(List.of("A", "A")).offset(2).limit(3).build();

        List<Object> params = new ArrayList<>();
        String query = queryBuilder.getSchemaHistorySearchQuery(criteria, params);

        assertTrue(query.contains("FROM eg_mdms_schema_definition_history history"));
        assertTrue(query.contains("history.code IN (  ? )"));
        assertEquals(List.of("pg", "A", 2, 3), params);
    }

    @Test
    void timeRangeIsApplied() {
        SchemaDefHistoryCriteria criteria = SchemaDefHistoryCriteria.builder().tenantId("pg").fromTime(1L).toTime(2L).build();

        List<Object> params = new ArrayList<>();
        String query = queryBuilder.getSchemaHistorySearchQuery(criteria, params);

        assertTrue(query.contains("history.changedtime >= ?"));
        assertTrue(query.contains("history.changedtime <= ?"));
        assertEquals(List.of("pg", 1L, 2L, 0, 10), params);
        assertEquals(params.size(), query.chars().filter(c -> c == '?').count());
    }

    @Test
    void missingOrBlankTenantIsRejected() {
        for (String tenantId : new String[]{null, "", "  "}) {
            SchemaDefHistoryCriteria criteria = SchemaDefHistoryCriteria.builder().tenantId(tenantId).build();
            org.egov.tracer.model.CustomException e = assertThrows(org.egov.tracer.model.CustomException.class,
                    () -> queryBuilder.getSchemaHistorySearchQuery(criteria, new ArrayList<>()));
            assertEquals("INVALID_HISTORY_TENANT", e.getCode());
        }
    }
}
