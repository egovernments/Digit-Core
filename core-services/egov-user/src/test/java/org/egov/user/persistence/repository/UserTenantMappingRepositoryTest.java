package org.egov.user.persistence.repository;

import org.egov.user.domain.model.UserTenantMapping;
import org.egov.user.domain.model.enums.UserType;
import org.egov.user.repository.builder.UserTenantMappingQueryBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserTenantMappingRepositoryTest {

    @Mock
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private UserTenantMappingRepository repository;

    @Before
    public void setUp() {
        repository = new UserTenantMappingRepository(namedParameterJdbcTemplate);
    }

    @Test
    public void upsert_bindsSixParamsWithTypeName() {
        ArgumentCaptor<SqlParameterSource> captor = ArgumentCaptor.forClass(SqlParameterSource.class);
        repository.upsert(7L, UserType.EMPLOYEE, "pb.amritsar", "enc-key", "uuid-7", true);
        verify(namedParameterJdbcTemplate).update(eq(UserTenantMappingQueryBuilder.UPSERT), captor.capture());
        MapSqlParameterSource p = (MapSqlParameterSource) captor.getValue();
        assertEquals(6, p.getValues().size());
        assertEquals("EMPLOYEE", p.getValue("type"));
        assertEquals("enc-key", p.getValue("usernamekey"));
        assertEquals(true, p.getValue("active"));
    }

    @Test
    public void setActive_bindsFourParams() {
        ArgumentCaptor<SqlParameterSource> captor = ArgumentCaptor.forClass(SqlParameterSource.class);
        repository.setActive(7L, UserType.EMPLOYEE, "pb.amritsar", false);
        verify(namedParameterJdbcTemplate).update(eq(UserTenantMappingQueryBuilder.SET_ACTIVE), captor.capture());
        MapSqlParameterSource p = (MapSqlParameterSource) captor.getValue();
        assertEquals(4, p.getValues().size());
        assertEquals(false, p.getValue("active"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void findActiveByUsernameKeyAndType_mapsRows() throws SQLException {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("tenantid")).thenReturn("pb.amritsar");
        when(rs.getLong("userid")).thenReturn(7L);
        when(rs.getString("uuid")).thenReturn("uuid-7");
        when(namedParameterJdbcTemplate.query(eq(UserTenantMappingQueryBuilder.FIND_ACTIVE_BY_USERNAMEKEY_AND_TYPE),
                any(SqlParameterSource.class), any(RowMapper.class)))
                .thenAnswer(inv -> Collections.singletonList(((RowMapper<UserTenantMapping>) inv.getArguments()[2]).mapRow(rs, 0)));
        List<UserTenantMapping> out = repository.findActiveByUsernameKeyAndType("enc-key", UserType.EMPLOYEE);
        assertEquals(1, out.size());
        assertEquals("pb.amritsar", out.get(0).getTenantId());
        assertEquals(Long.valueOf(7L), out.get(0).getUserId());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void findActiveByUsernameKeyAndType_returnsEmptyListWhenNoneFound() {
        when(namedParameterJdbcTemplate.query(eq(UserTenantMappingQueryBuilder.FIND_ACTIVE_BY_USERNAMEKEY_AND_TYPE),
                any(SqlParameterSource.class), any(RowMapper.class))).thenReturn(Collections.emptyList());
        assertTrue(repository.findActiveByUsernameKeyAndType("enc-key", UserType.EMPLOYEE).isEmpty());
    }
}
