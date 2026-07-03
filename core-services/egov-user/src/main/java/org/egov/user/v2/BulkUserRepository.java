package org.egov.user.v2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.egov.user.domain.model.Role;
import org.egov.user.domain.model.User;
import org.egov.user.domain.model.enums.BloodGroup;
import org.egov.user.domain.model.enums.Gender;
import org.egov.user.domain.model.enums.GuardianRelation;
import org.egov.user.domain.model.enums.UserType;
import org.egov.user.repository.builder.RoleQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;

/**
 * Batch DB operations for v2 bulk-create.
 * <p>
 * Reuses v1's {@code UserTypeQueryBuilder} and {@code RoleQueryBuilder} for SQL strings
 * so the column list stays in one place. The bulk operations themselves are new here
 * and do not touch v1's {@link org.egov.user.persistence.repository.UserRepository}.
 */
@Repository
@Slf4j
public class BulkUserRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final JdbcTemplate jdbcTemplate;

    private static final String SELECT_EXISTING_USERNAMES =
            "SELECT username FROM eg_user " +
            "WHERE tenantid = :tenantId AND type = :type AND username IN (:usernames)";

    private static final String SELECT_N_SEQUENCES =
            "SELECT nextval('seq_eg_user') FROM generate_series(1, ?)";

    // v2's own INSERT SQL — matches the actual eg_user schema in tenant DBs
    // (independent of v1's UserTypeQueryBuilder, which may drift out of sync).
    private static final String INSERT_USER_SQL =
            "INSERT INTO eg_user (" +
            "id, uuid, tenantid, salutation, dob, locale, username, password, pwdexpirydate, " +
            "mobilenumber, altcontactnumber, emailid, active, name, gender, pan, aadhaarnumber, " +
            "type, guardian, guardianrelation, signature, accountlocked, bloodgroup, photo, " +
            "identificationmark, createddate, lastmodifieddate, createdby, lastmodifiedby, " +
            "alternatemobilenumber" +
            ") VALUES (" +
            ":id, :uuid, :tenantid, :salutation, :dob, :locale, :username, :password, :pwdexpirydate, " +
            ":mobilenumber, :altcontactnumber, :emailid, :active, :name, :gender, :pan, :aadhaarnumber, " +
            ":type, :guardian, :guardianrelation, :signature, :accountlocked, :bloodgroup, :photo, " +
            ":identificationmark, :createddate, :lastmodifieddate, :createdby, :lastmodifiedby, " +
            ":alternatemobilenumber" +
            ")";

    @Autowired
    public BulkUserRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                              JdbcTemplate jdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * One SQL SELECT that returns which of the given users' usernames already exist
     * in eg_user, matched by (tenantId, type). Assumes the whole batch shares one
     * (tenantId, type) — validated by the caller.
     */
    public Set<String> findExistingUsernames(List<User> users, String stateTenant) {
        if (users == null || users.isEmpty()) return Collections.emptySet();

        Set<String> usernames = users.stream()
                .map(User::getUsername)
                .collect(Collectors.toSet());
        String tenantId = stateTenant != null ? stateTenant : users.get(0).getTenantId();
        String type = users.get(0).getType() == null ? "" : users.get(0).getType().toString();

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("tenantId", tenantId)
                .addValue("type", type)
                .addValue("usernames", usernames);

        List<String> matches = namedParameterJdbcTemplate.queryForList(
                SELECT_EXISTING_USERNAMES, params, String.class);

        return new HashSet<>(matches);
    }

    /**
     * Bulk-create the given users. On return each user has its id and uuid populated.
     * Two batch SQL statements: INSERT INTO eg_user, then INSERT INTO eg_userrole_v1.
     */
    public void createBulk(List<User> users) {
        if (users == null || users.isEmpty()) return;

        // 1. Reserve N sequence values in one round-trip.
        List<Long> ids = jdbcTemplate.queryForList(SELECT_N_SEQUENCES, Long.class, users.size());
        for (int i = 0; i < users.size(); i++) {
            users.get(i).setId(ids.get(i));
        }

        // 2. Batch INSERT eg_user using v2's own SQL.
        SqlParameterSource[] userRows = users.stream()
                .map(this::toUserParams)
                .toArray(SqlParameterSource[]::new);
        namedParameterJdbcTemplate.batchUpdate(INSERT_USER_SQL, userRows);

        // 3. Batch INSERT eg_userrole_v1 — flatten users × roles.
        List<SqlParameterSource> roleRows = new ArrayList<>();
        Date now = new Date();
        for (User user : users) {
            if (user.getRoles() == null) continue;
            for (Role role : user.getRoles()) {
                roleRows.add(new MapSqlParameterSource()
                        .addValue("role_code", role.getCode())
                        .addValue("role_tenantid", role.getTenantId() != null
                                ? role.getTenantId() : user.getTenantId())
                        .addValue("user_id", user.getId())
                        .addValue("user_tenantid", user.getTenantId())
                        .addValue("lastmodifieddate", now));
            }
        }
        if (!roleRows.isEmpty()) {
            namedParameterJdbcTemplate.batchUpdate(
                    RoleQueryBuilder.INSERT_USER_ROLES,
                    roleRows.toArray(new SqlParameterSource[0]));
        }

        log.info("Bulk INSERT complete: {} users, {} user-roles", users.size(), roleRows.size());
    }

    /**
     * Build the named-parameter map for one row of eg_user. Column list mirrors
     * v1's {@code UserRepository.save()} so we can reuse the exact INSERT SQL from
     * {@link UserTypeQueryBuilder#getInsertUserQuery()}.
     */
    private SqlParameterSource toUserParams(User u) {
        Map<String, Object> p = new HashMap<>();
        p.put("id", u.getId());
        p.put("uuid", u.getUuid());
        p.put("tenantid", u.getTenantId());
        p.put("salutation", u.getSalutation());
        p.put("dob", u.getDob());
        p.put("locale", u.getLocale());
        p.put("username", u.getUsername());
        p.put("password", u.getPassword());
        p.put("pwdexpirydate", u.getPasswordExpiryDate());
        p.put("mobilenumber", u.getMobileNumber());
        p.put("altcontactnumber", u.getAltContactNumber());
        p.put("emailid", u.getEmailId());
        p.put("active", u.getActive());
        p.put("name", u.getName());
        p.put("gender", genderCode(u.getGender()));
        p.put("pan", u.getPan());
        p.put("aadhaarnumber", u.getAadhaarNumber());
        p.put("type", enumStringOrEmpty(u.getType(), UserType.values()));
        p.put("guardian", u.getGuardian());
        p.put("guardianrelation", enumStringOrEmpty(u.getGuardianRelation(), GuardianRelation.values()));
        p.put("signature", u.getSignature());
        p.put("accountlocked", u.getAccountLocked());
        p.put("bloodgroup", enumStringOrEmpty(u.getBloodGroup(), BloodGroup.values()));
        p.put("photo", u.getPhoto());
        p.put("identificationmark", u.getIdentificationMark());
        p.put("createddate", u.getCreatedDate());
        p.put("lastmodifieddate", u.getLastModifiedDate());
        p.put("createdby", u.getLoggedInUserId());
        p.put("lastmodifiedby", u.getLoggedInUserId());
        p.put("alternatemobilenumber", u.getAlternateMobileNumber());
        return new MapSqlParameterSource(p);
    }

    private int genderCode(Gender g) {
        if (g == null) return 0;
        switch (g) {
            case FEMALE:      return 1;
            case MALE:        return 2;
            case OTHERS:      return 3;
            case TRANSGENDER: return 4;
            default:          return 0;
        }
    }

    private String enumStringOrEmpty(Enum<?> value, Enum<?>[] allowed) {
        if (value == null) return "";
        return Arrays.asList(allowed).contains(value) ? value.toString() : "";
    }
}
