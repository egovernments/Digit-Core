package org.egov.user.persistence.repository;

import org.egov.user.domain.model.UserTenantMapping;
import org.egov.user.domain.model.enums.UserType;
import org.egov.user.repository.builder.UserTenantMappingQueryBuilder;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserTenantMappingRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public UserTenantMappingRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public void upsert(Long userId, UserType type, String tenantId, String usernameKey, String uuid, boolean active) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userid", userId)
                .addValue("type", type.name())
                .addValue("tenantid", tenantId)
                .addValue("usernamekey", usernameKey)
                .addValue("uuid", uuid)
                .addValue("active", active);
        namedParameterJdbcTemplate.update(UserTenantMappingQueryBuilder.UPSERT, params);
    }

    public boolean exists(Long userId, UserType type, String tenantId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userid", userId)
                .addValue("type", type.name())
                .addValue("tenantid", tenantId);
        return !namedParameterJdbcTemplate.queryForList(UserTenantMappingQueryBuilder.EXISTS, params, Integer.class)
                .isEmpty();
    }

    public void setActive(Long userId, UserType type, String tenantId, boolean active) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userid", userId)
                .addValue("type", type.name())
                .addValue("tenantid", tenantId)
                .addValue("active", active);
        namedParameterJdbcTemplate.update(UserTenantMappingQueryBuilder.SET_ACTIVE, params);
    }

    public List<UserTenantMapping> findActiveByUsernameKeyAndType(String usernameKey, UserType type) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("usernamekey", usernameKey)
                .addValue("type", type.name());
        return namedParameterJdbcTemplate.query(UserTenantMappingQueryBuilder.FIND_ACTIVE_BY_USERNAMEKEY_AND_TYPE, params,
                (rs, rowNum) -> UserTenantMapping.builder()
                        .tenantId(rs.getString("tenantid"))
                        .userId(rs.getLong("userid"))
                        .uuid(rs.getString("uuid"))
                        .build());
    }
}
