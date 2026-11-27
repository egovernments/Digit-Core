package org.egov.infra.mdms.repository.querybuilder;

import org.egov.infra.mdms.config.ApplicationConfig;
import org.egov.infra.mdms.model.MdmsHistoryCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MdmsDataHistoryQueryBuilderTest {

    private MdmsDataHistoryQueryBuilder queryBuilder;

    @BeforeEach
    void setUp() {
        ApplicationConfig config = new ApplicationConfig();
        config.setDefaultOffset(0);
        config.setDefaultLimit(10);
        queryBuilder = new MdmsDataHistoryQueryBuilder();
        ReflectionTestUtils.setField(queryBuilder, "config", config);
    }

    @Test
    void tenantOnlyCriteriaUsesDefaultPagination() {
        List<Object> params = new ArrayList<>();
        String query = queryBuilder.getMdmsDataHistorySearchQuery(MdmsHistoryCriteria.builder().tenantId("pg").build(), params);

        assertTrue(query.contains("FROM eg_mdms_data_history history  WHERE  history.tenantid = ?"));
        assertTrue(query.contains("order by history.historyid desc"));
        assertTrue(query.trim().endsWith("OFFSET ?  LIMIT ?"));
        assertEquals(List.of("pg", 0, 10), params);
    }

    @Test
    void allFiltersAreAppliedInOrderWithMatchingParameters() {
        Set<String> uniqueIdentifiers = new LinkedHashSet<>(List.of("U1", "U2"));
        Set<String> ids = new LinkedHashSet<>(List.of("id1"));
        MdmsHistoryCriteria criteria = MdmsHistoryCriteria.builder().tenantId("pg.citya").schemaCode("common.Dept")
                .uniqueIdentifiers(uniqueIdentifiers).ids(ids).fromTime(100L).toTime(200L).offset(5).limit(50).build();

        List<Object> params = new ArrayList<>();
        String query = queryBuilder.getMdmsDataHistorySearchQuery(criteria, params);

        assertTrue(query.contains("history.schemacode = ?"));
        assertTrue(query.contains("history.uniqueidentifier IN (  ?, ? )"));
        assertTrue(query.contains("history.id IN (  ? )"));
        assertTrue(query.contains("history.changedtime >= ?"));
        assertTrue(query.contains("history.changedtime <= ?"));
        assertEquals(List.of("pg.citya", "common.Dept", "U1", "U2", "id1", 100L, 200L, 5, 50), params);
        assertEquals(params.size(), query.chars().filter(c -> c == '?').count());
    }

    @Test
    void emptyCollectionsAreIgnored() {
        MdmsHistoryCriteria criteria = MdmsHistoryCriteria.builder().tenantId("pg")
                .uniqueIdentifiers(Set.of()).ids(Set.of()).build();

        List<Object> params = new ArrayList<>();
        String query = queryBuilder.getMdmsDataHistorySearchQuery(criteria, params);

        assertFalse(query.contains(" IN ("));
        assertEquals(List.of("pg", 0, 10), params);
    }

    @Test
    void missingOrBlankTenantIsRejected() {
        for (String tenantId : new String[]{null, "", "  "}) {
            MdmsHistoryCriteria criteria = MdmsHistoryCriteria.builder().tenantId(tenantId).build();
            org.egov.tracer.model.CustomException e = assertThrows(org.egov.tracer.model.CustomException.class,
                    () -> queryBuilder.getMdmsDataHistorySearchQuery(criteria, new ArrayList<>()));
            assertEquals("INVALID_HISTORY_TENANT", e.getCode());
        }
    }
}
