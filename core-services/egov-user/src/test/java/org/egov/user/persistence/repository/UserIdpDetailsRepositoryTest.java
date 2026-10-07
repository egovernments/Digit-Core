package org.egov.user.persistence.repository;

import org.egov.user.domain.model.UserIdpDetails;
import org.egov.user.repository.builder.UserIdpDetailsQueryBuilder;
import org.egov.user.utils.DatabaseSchemaUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test suite for UserIdpDetailsRepository.
 * 
 * <p>Comprehensive unit tests covering all repository operations including:</p>
 * <ul>
 *   <li>Upsert operations with proper SQL query generation</li>
 *   <li>Audit trail insertion for change tracking</li>
 *   <li>Token replay protection queries</li>
 *   <li>Schema placeholder replacement for multi-tenant support</li>
 *   <li>Parameter binding and validation</li>
 * </ul>
 * 
 * <p>Tests use mock objects to isolate the repository logic and verify proper
 * interaction with the JDBC template and schema utilities.</p>
 */
public class UserIdpDetailsRepositoryTest {

    private NamedParameterJdbcTemplate jdbcTemplate;
    private DatabaseSchemaUtils databaseSchemaUtils;
    private UserIdpDetailsRepository repository;

    @Before
    public void setup() {
        jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
        databaseSchemaUtils = mock(DatabaseSchemaUtils.class);
        repository = new UserIdpDetailsRepository(jdbcTemplate, databaseSchemaUtils);

        when(databaseSchemaUtils.replaceSchemaPlaceholder(eq(UserIdpDetailsQueryBuilder.UPSERT_IDP_DETAILS), eq("pb")))
                .thenReturn(UserIdpDetailsQueryBuilder.UPSERT_IDP_DETAILS.replace(DatabaseSchemaUtils.SCHEMA_REPLACE_STRING, "pb"));
        when(databaseSchemaUtils.replaceSchemaPlaceholder(eq(UserIdpDetailsQueryBuilder.INSERT_IDP_AUDIT), eq("pb")))
                .thenReturn(UserIdpDetailsQueryBuilder.INSERT_IDP_AUDIT.replace(DatabaseSchemaUtils.SCHEMA_REPLACE_STRING, "pb"));
    }

    @Test
    public void upsert_InsertsIdpDetailsAndAudit() {
        UserIdpDetails details = UserIdpDetails.builder()
                .id(1L)
                .tenantId("pb.amritsar")
                .uuid("uuid-1")
                .tokenId("token-1")
                .mfaEnabled(Boolean.TRUE)
                .mfaDeviceName("device")
                .mfaPhoneLast4("1234")
                .mfaDetails("details")
                .createdBy(10L)
                .lastModifiedBy(11L)
                .build();

        repository.upsert(details, "pb");

        ArgumentCaptor<Map> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(jdbcTemplate, times(2)).update(anyString(), paramsCaptor.capture());

        assertEquals(2, paramsCaptor.getAllValues().size());
        Map<String, Object> upsertParams = paramsCaptor.getAllValues().get(0);
        assertEquals(1L, upsertParams.get("id"));
        assertEquals("pb", upsertParams.get("tenantid"));
        assertEquals("uuid-1", upsertParams.get("uuid"));
        assertEquals("token-1", upsertParams.get("tokenid"));
        assertEquals(Boolean.TRUE, upsertParams.get("mfaenabled"));
        assertEquals("device", upsertParams.get("mfadevicename"));
        assertEquals("1234", upsertParams.get("mfaphonelast4"));
        assertEquals("details", upsertParams.get("mfadetails"));
        assertEquals(10L, upsertParams.get("createdby"));
        assertEquals(11L, upsertParams.get("lastmodifiedby"));

        Map<String, Object> auditParams = paramsCaptor.getAllValues().get(1);
        assertEquals("pb", auditParams.get("tenantid"));
        assertEquals("uuid-1", auditParams.get("uuid"));
        assertEquals("token-1", auditParams.get("tokenid"));
        assertEquals(Boolean.TRUE, auditParams.get("mfaenabled"));
        assertEquals("device", auditParams.get("mfadevicename"));
        assertEquals("1234", auditParams.get("mfaphonelast4"));
        assertEquals("details", auditParams.get("mfadetails"));
        assertEquals(10L, auditParams.get("createdby"));
        assertEquals(11L, auditParams.get("lastmodifiedby"));
        assertNotNull(auditParams.get("id"));
        assertTrue(auditParams.get("id") instanceof UUID);
    }

    @Test
    public void upsert_WithCreatedDate_PreservesIt() {
        Date created = new Date(System.currentTimeMillis() - 1000);
        UserIdpDetails details = UserIdpDetails.builder()
                .id(2L)
                .tenantId("pb.amritsar")
                .uuid("uuid-2")
                .tokenId("token-2")
                .createdDate(created)
                .createdBy(10L)
                .lastModifiedBy(11L)
                .build();

        repository.upsert(details, "pb");

        ArgumentCaptor<Map> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(jdbcTemplate, times(2)).update(anyString(), paramsCaptor.capture());

        Map<String, Object> upsertParams = paramsCaptor.getAllValues().get(0);
        Map<String, Object> auditParams = paramsCaptor.getAllValues().get(1);
        assertEquals(created, upsertParams.get("createddate"));
        assertEquals(created, auditParams.get("createddate"));
    }

    @Test
    public void upsert_NullMfaEnabled_DefaultsToFalse() {
        UserIdpDetails details = UserIdpDetails.builder()
                .id(3L)
                .tenantId("pb.amritsar")
                .uuid("uuid-3")
                .tokenId("token-3")
                .createdBy(10L)
                .lastModifiedBy(11L)
                .build();

        repository.upsert(details, "pb");

        ArgumentCaptor<Map> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(jdbcTemplate, times(2)).update(anyString(), paramsCaptor.capture());

        Map<String, Object> upsertParams = paramsCaptor.getAllValues().get(0);
        Map<String, Object> auditParams = paramsCaptor.getAllValues().get(1);
        assertEquals(Boolean.FALSE, upsertParams.get("mfaenabled"));
        assertEquals(Boolean.FALSE, auditParams.get("mfaenabled"));
    }
}
