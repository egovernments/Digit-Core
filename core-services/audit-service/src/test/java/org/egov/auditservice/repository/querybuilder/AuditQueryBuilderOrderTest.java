package org.egov.auditservice.repository.querybuilder;

import org.egov.auditservice.config.AuditServiceConfiguration;
import org.egov.auditservice.web.models.AuditLogSearchCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuditQueryBuilderOrderTest {

    private AuditQueryBuilder auditQueryBuilder;

    @BeforeEach
    void setUp() {
        AuditServiceConfiguration config = mock(AuditServiceConfiguration.class);
        when(config.getDefaultLimit()).thenReturn(10);
        when(config.getDefaultOffset()).thenReturn(0);
        when(config.getMaxSearchLimit()).thenReturn(30);
        auditQueryBuilder = new AuditQueryBuilder();
        ReflectionTestUtils.setField(auditQueryBuilder, "config", config);
    }

    @Test
    void searchIsOrderedLatestFirstBeforePagination() {
        AuditLogSearchCriteria criteria = new AuditLogSearchCriteria();
        criteria.setTenantId("pb");
        criteria.setObjectId("obj-1");
        criteria.setModule("MDMS");
        List<Object> params = new ArrayList<>();

        String query = auditQueryBuilder.getAuditLogQuery(criteria, params);

        assertTrue(query.contains(" WHERE  tenantid = ?  AND  module = ?  AND  objectid = ? "), query);
        assertTrue(query.contains("ORDER BY changedate DESC "), query);
        assertTrue(query.indexOf("ORDER BY changedate DESC ") < query.indexOf("OFFSET ?"), query);
        assertTrue(query.trim().endsWith("LIMIT ?"), query);
        assertEquals(List.of("pb", "MDMS", "obj-1", 0, 10), params);
    }

    @Test
    void requestedLimitIsCappedAtMaxSearchLimit() {
        AuditLogSearchCriteria criteria = new AuditLogSearchCriteria();
        criteria.setTenantId("pb");
        criteria.setObjectId("obj-1");
        criteria.setOffset(30);
        criteria.setLimit(500);
        List<Object> params = new ArrayList<>();

        auditQueryBuilder.getAuditLogQuery(criteria, params);

        assertEquals(List.of("pb", "obj-1", 30, 30), params);
    }
}
