package org.egov.user.persistence.repository;

import org.egov.user.domain.model.UserIdpLink;
import org.egov.user.utils.DatabaseSchemaUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.egov.user.utils.DatabaseSchemaUtils.SCHEMA_REPLACE_STRING;

@Repository
public class UserIdpLinkRepository {

    private static final String FIND =
            "SELECT tenantid, issuer, subject, userid, uuid, providerid, createddate FROM " + SCHEMA_REPLACE_STRING
                    + ".eg_user_idp_link WHERE tenantid = :tenantid AND providerid = :providerid "
                    + "AND issuer = :issuer AND subject = :subject";

    private static final String FIND_BY_USER =
            "SELECT tenantid, issuer, subject, userid, uuid, providerid, createddate FROM " + SCHEMA_REPLACE_STRING
                    + ".eg_user_idp_link WHERE userid = :userid AND tenantid = :tenantid";

    private static final String INSERT =
            "INSERT INTO " + SCHEMA_REPLACE_STRING + ".eg_user_idp_link "
                    + "(tenantid, issuer, subject, userid, uuid, providerid) "
                    + "VALUES (:tenantid, :issuer, :subject, :userid, :uuid, :providerid) "
                    + "ON CONFLICT (tenantid, providerid, issuer, subject) DO NOTHING";

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final DatabaseSchemaUtils databaseSchemaUtils;

    public UserIdpLinkRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                                 DatabaseSchemaUtils databaseSchemaUtils) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.databaseSchemaUtils = databaseSchemaUtils;
    }

    public Optional<UserIdpLink> find(String tenantId, String providerId, String issuer, String subject) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("tenantid", tenantId)
                .addValue("providerid", providerId)
                .addValue("issuer", issuer)
                .addValue("subject", subject);
        String query = databaseSchemaUtils.replaceSchemaPlaceholder(FIND, tenantId);
        List<UserIdpLink> results = namedParameterJdbcTemplate.query(query, params, this::mapRow);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<UserIdpLink> findByUser(Long userId, String tenantId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userid", userId)
                .addValue("tenantid", tenantId);
        String query = databaseSchemaUtils.replaceSchemaPlaceholder(FIND_BY_USER, tenantId);
        return namedParameterJdbcTemplate.query(query, params, this::mapRow);
    }

    public void insert(UserIdpLink link) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("tenantid", link.getTenantId())
                .addValue("issuer", link.getIssuer())
                .addValue("subject", link.getSubject())
                .addValue("userid", link.getUserId())
                .addValue("uuid", link.getUuid())
                .addValue("providerid", link.getProviderId());
        String query = databaseSchemaUtils.replaceSchemaPlaceholder(INSERT, link.getTenantId());
        namedParameterJdbcTemplate.update(query, params);
    }

    private UserIdpLink mapRow(ResultSet rs, int rowNum) throws SQLException {
        return UserIdpLink.builder()
                .tenantId(rs.getString("tenantid"))
                .issuer(rs.getString("issuer"))
                .subject(rs.getString("subject"))
                .userId(rs.getLong("userid"))
                .uuid(rs.getString("uuid"))
                .providerId(rs.getString("providerid"))
                .createdDate(rs.getTimestamp("createddate"))
                .build();
    }
}
