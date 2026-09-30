package org.egov.user.persistence.repository;

import org.egov.user.domain.model.UserIdpLink;
import org.egov.user.utils.DatabaseSchemaUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class UserIdpLinkRepositoryTest {

    private NamedParameterJdbcTemplate jdbcTemplate;
    private DatabaseSchemaUtils databaseSchemaUtils;
    private UserIdpLinkRepository repository;

    @Before
    public void setup() {
        jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
        databaseSchemaUtils = mock(DatabaseSchemaUtils.class);
        repository = new UserIdpLinkRepository(jdbcTemplate, databaseSchemaUtils);

        when(databaseSchemaUtils.replaceSchemaPlaceholder(anyString(), eq("pb")))
                .thenAnswer(inv -> ((String) inv.getArguments()[0])
                        .replace(DatabaseSchemaUtils.SCHEMA_REPLACE_STRING + ".", ""));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void find_ReturnsLink_WhenPresent() {
        UserIdpLink link = UserIdpLink.builder().tenantId("pb").issuer("issuer").subject("subject")
                .userId(1L).uuid("uuid-1").providerId("oidc-azure").build();
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(Collections.singletonList(link));

        Optional<UserIdpLink> result = repository.find("pb", "oidc-azure", "issuer", "subject");

        assertTrue(result.isPresent());
        assertEquals("subject", result.get().getSubject());

        ArgumentCaptor<MapSqlParameterSource> paramsCaptor = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(jdbcTemplate).query(anyString(), paramsCaptor.capture(), any(RowMapper.class));
        assertEquals("pb", paramsCaptor.getValue().getValue("tenantid"));
        assertEquals("oidc-azure", paramsCaptor.getValue().getValue("providerid"));
        assertEquals("issuer", paramsCaptor.getValue().getValue("issuer"));
        assertEquals("subject", paramsCaptor.getValue().getValue("subject"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void find_ReturnsEmpty_WhenAbsent() {
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(Collections.emptyList());

        Optional<UserIdpLink> result = repository.find("pb", "oidc-azure", "issuer", "subject");

        assertFalse(result.isPresent());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void findByUser_ReturnsAllLinksForUser() {
        UserIdpLink link1 = UserIdpLink.builder().tenantId("pb").issuer("issuer-1").subject("sub-1")
                .userId(1L).uuid("uuid-1").providerId("oidc-azure").build();
        UserIdpLink link2 = UserIdpLink.builder().tenantId("pb").issuer("issuer-2").subject("sub-2")
                .userId(1L).uuid("uuid-1").providerId("oidc-google").build();
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(java.util.Arrays.asList(link1, link2));

        List<UserIdpLink> results = repository.findByUser(1L, "pb");

        assertEquals(2, results.size());

        ArgumentCaptor<MapSqlParameterSource> paramsCaptor = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(jdbcTemplate).query(anyString(), paramsCaptor.capture(), any(RowMapper.class));
        assertEquals(1L, paramsCaptor.getValue().getValue("userid"));
        assertEquals("pb", paramsCaptor.getValue().getValue("tenantid"));
    }

    @Test
    public void insert_WritesAllColumns() {
        UserIdpLink link = UserIdpLink.builder().tenantId("pb").issuer("issuer").subject("subject")
                .userId(1L).uuid("uuid-1").providerId("oidc-azure").build();

        repository.insert(link);

        ArgumentCaptor<MapSqlParameterSource> paramsCaptor = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(jdbcTemplate).update(anyString(), paramsCaptor.capture());
        MapSqlParameterSource params = paramsCaptor.getValue();
        assertEquals("pb", params.getValue("tenantid"));
        assertEquals("issuer", params.getValue("issuer"));
        assertEquals("subject", params.getValue("subject"));
        assertEquals(1L, params.getValue("userid"));
        assertEquals("uuid-1", params.getValue("uuid"));
        assertEquals("oidc-azure", params.getValue("providerid"));
    }
}
