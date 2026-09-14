package org.egov.user.identity;

import org.egov.user.web.contract.auth.Role;
import org.egov.user.web.contract.auth.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Repository
public class IdentityRepository {

    private final JdbcTemplate jdbcTemplate;

    public IdentityRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String ensureSubject(String issuer, String subject, String digitUserUuid, long now) {
        List<String> existing = jdbcTemplate.query(
                "SELECT digit_user_uuid FROM eg_identity_subject WHERE issuer = ? AND external_subject = ?",
                (rs, rowNum) -> rs.getString(1), issuer, subject);
        if (!existing.isEmpty()) {
            if (!existing.get(0).equalsIgnoreCase(digitUserUuid)) {
                throw new IdentityException(409, "Identity subject is already linked to another DIGIT user");
            }
            jdbcTemplate.update("UPDATE eg_identity_subject SET active = true, updated_at = ? " +
                    "WHERE issuer = ? AND external_subject = ?", now, issuer, subject);
            return digitUserUuid;
        }
        Integer users = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM eg_user WHERE lower(uuid) = lower(?) AND active = true " +
                        "AND type = 'EMPLOYEE'",
                Integer.class, digitUserUuid);
        if (users == null || users != 1) {
            throw new IdentityException(400, "digitUserUuid must identify one active DIGIT employee");
        }
        jdbcTemplate.update("INSERT INTO eg_identity_subject " +
                        "(id, issuer, external_subject, digit_user_uuid, active, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, true, ?, ?)",
                UUID.randomUUID(), issuer, subject, digitUserUuid, now, now);
        return digitUserUuid;
    }

    public String findLinkedUserUuid(String issuer, String subject) {
        List<String> existing = jdbcTemplate.query(
                "SELECT digit_user_uuid FROM eg_identity_subject WHERE issuer = ? AND external_subject = ?",
                (rs, rowNum) -> rs.getString(1), issuer, subject);
        return existing.isEmpty() ? null : existing.get(0);
    }

    public String requireActiveOrganizationTenant(String organizationId) {
        List<String> tenants = jdbcTemplate.query(
                "SELECT tenant_id FROM eg_identity_organization WHERE organization_id = ? AND active = true",
                (rs, rowNum) -> rs.getString(1), organizationId);
        if (tenants.size() != 1) throw new IdentityException(404, "Identity organization is not mapped");
        return tenants.get(0);
    }

    public String ensureOrganization(String organizationId, String alias, String tenantId,
                                     String name, long now) {
        List<String> existing = jdbcTemplate.query(
                "SELECT tenant_id FROM eg_identity_organization WHERE organization_id = ?",
                (rs, rowNum) -> rs.getString(1), organizationId);
        if (!existing.isEmpty() && !existing.get(0).equals(tenantId)) {
            throw new IdentityException(409, "Organization is already mapped to another tenant");
        }
        if (existing.isEmpty()) {
            jdbcTemplate.update("INSERT INTO eg_identity_organization " +
                            "(organization_id, organization_alias, tenant_id, name, active, created_at, updated_at) " +
                            "VALUES (?, ?, ?, ?, true, ?, ?)",
                    organizationId, alias, tenantId, name, now, now);
        } else {
            jdbcTemplate.update("UPDATE eg_identity_organization SET organization_alias = ?, name = ?, " +
                            "active = true, updated_at = ? WHERE organization_id = ?",
                    alias, name, now, organizationId);
        }
        return tenantId;
    }

    public UUID requireSubjectId(String issuer, String subject) {
        List<UUID> ids = jdbcTemplate.query("SELECT id FROM eg_identity_subject " +
                        "WHERE issuer = ? AND external_subject = ? AND active = true",
                (rs, rowNum) -> uuid(rs, 1), issuer, subject);
        if (ids.size() != 1) throw new IdentityException(404, "Identity subject is not linked");
        return ids.get(0);
    }

    public UUID reconcileMembership(UUID subjectId, String organizationId, Set<String> roles,
                                    boolean active, long now) {
        List<MembershipRow> rows = jdbcTemplate.query(
                "SELECT m.id, s.digit_user_uuid, o.tenant_id FROM eg_identity_subject s " +
                        "JOIN eg_identity_organization o ON o.organization_id = ? AND o.active = true " +
                        "LEFT JOIN eg_identity_membership m ON m.subject_id = s.id " +
                        "AND m.organization_id = o.organization_id WHERE s.id = ? AND s.active = true",
                (rs, rowNum) -> new MembershipRow(
                        rs.getObject("id") == null ? null : uuid(rs, "id"),
                        rs.getString("digit_user_uuid"), rs.getString("tenant_id")),
                organizationId, subjectId);
        if (rows.size() != 1) throw new IdentityException(404, "Identity organization is not mapped");
        MembershipRow row = rows.get(0);
        if (!active && row.id == null) return null;
        UUID membershipId = row.id == null ? UUID.randomUUID() : row.id;
        if (row.id == null) {
            jdbcTemplate.update("INSERT INTO eg_identity_membership " +
                            "(id, subject_id, organization_id, active, authorization_version, created_at, updated_at) " +
                            "VALUES (?, ?, ?, true, 1, ?, ?)",
                    membershipId, subjectId, organizationId, now, now);
        } else if (active) {
            jdbcTemplate.update("UPDATE eg_identity_membership SET active = true, " +
                            "authorization_version = authorization_version + 1, updated_at = ? WHERE id = ?",
                    now, membershipId);
        }

        UserRow user = userByUuid(row.digitUserUuid);
        removeProjectedRoles(membershipId, user, row.tenantId);

        if (!active) {
            jdbcTemplate.update("UPDATE eg_identity_membership SET active = false, " +
                            "authorization_version = authorization_version + 1, updated_at = ? WHERE id = ?",
                    now, membershipId);
            return membershipId;
        }

        for (String role : roles) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM eg_userrole_v1 WHERE user_id = ? AND user_tenantid = ? " +
                            "AND role_tenantid = ? AND role_code = ?",
                    Integer.class, user.id, user.userTenantId, row.tenantId, role);
            if (count == null || count == 0) {
                jdbcTemplate.update("INSERT INTO eg_userrole_v1 " +
                                "(role_code, role_tenantid, user_id, user_tenantid, lastmodifieddate) " +
                                "VALUES (?, ?, ?, ?, now())",
                        role, row.tenantId, user.id, user.userTenantId);
                jdbcTemplate.update("INSERT INTO eg_identity_projected_role (membership_id, role_code) " +
                        "VALUES (?, ?)", membershipId, role);
            }
        }
        return membershipId;
    }

    private void removeProjectedRoles(UUID membershipId, UserRow user, String tenantId) {
        jdbcTemplate.update("DELETE FROM eg_userrole_v1 role USING eg_identity_projected_role projected " +
                        "WHERE projected.membership_id = ? AND role.user_id = ? " +
                        "AND role.user_tenantid = ? AND role.role_tenantid = ? " +
                        "AND role.role_code = projected.role_code",
                membershipId, user.id, user.userTenantId, tenantId);
        jdbcTemplate.update("DELETE FROM eg_identity_projected_role WHERE membership_id = ?", membershipId);
    }

    public List<Map<String, Object>> reconciliationSnapshot() {
        List<Map<String, Object>> rows = jdbcTemplate.query(
                "SELECT o.organization_id, o.organization_alias, o.tenant_id, o.name, o.active, " +
                        "s.issuer, s.external_subject, m.active AS membership_active " +
                        "FROM eg_identity_organization o " +
                        "LEFT JOIN eg_identity_membership m ON m.organization_id = o.organization_id " +
                        "LEFT JOIN eg_identity_subject s ON s.id = m.subject_id " +
                        "ORDER BY o.organization_id, s.external_subject",
                (rs, rowNum) -> {
                    Map<String, Object> row = new LinkedHashMap<String, Object>();
                    row.put("organizationId", rs.getString("organization_id"));
                    row.put("organizationAlias", rs.getString("organization_alias"));
                    row.put("tenantId", rs.getString("tenant_id"));
                    row.put("name", rs.getString("name"));
                    row.put("active", rs.getBoolean("active"));
                    row.put("issuer", rs.getString("issuer"));
                    row.put("subject", rs.getString("external_subject"));
                    row.put("membershipActive", rs.getObject("membership_active") == null
                            ? null : rs.getBoolean("membership_active"));
                    return row;
                });
        Map<String, Map<String, Object>> organizations = new LinkedHashMap<String, Map<String, Object>>();
        for (Map<String, Object> row : rows) {
            String id = (String) row.get("organizationId");
            Map<String, Object> organization = organizations.get(id);
            if (organization == null) {
                organization = new LinkedHashMap<String, Object>();
                organization.put("organizationId", id);
                organization.put("organizationAlias", row.get("organizationAlias"));
                organization.put("tenantId", row.get("tenantId"));
                organization.put("name", row.get("name"));
                organization.put("active", row.get("active"));
                organization.put("members", new ArrayList<Map<String, Object>>());
                organizations.put(id, organization);
            }
            if (row.get("subject") != null) {
                Map<String, Object> member = new LinkedHashMap<String, Object>();
                member.put("issuer", row.get("issuer"));
                member.put("subject", row.get("subject"));
                member.put("active", row.get("membershipActive"));
                ((List<Map<String, Object>>) organization.get("members")).add(member);
            }
        }
        return new ArrayList<Map<String, Object>>(organizations.values());
    }

    public IdentityContext resolveContext(String issuer, String subject, String organizationId) {
        List<IdentityUserContext> rows = findUserContext(issuer, subject, organizationId);
        if (rows.size() != 1) return null;
        IdentityUserContext row = rows.get(0);
        List<String> roles = roleCodes(row.getUser().getId(), row.getTenantId());
        if (roles.isEmpty()) return null;
        return IdentityContext.builder()
                .organizationId(row.getOrganizationId())
                .organizationAlias(row.getOrganizationAlias())
                .tenantId(row.getTenantId())
                .name(organizationName(organizationId))
                .roles(roles).active(true).build();
    }

    public IdentityUserContext requireUserContext(IdentityAssertion assertion) {
        List<IdentityUserContext> rows = findUserContext(
                assertion.getIssuer(), assertion.getSubject(), assertion.getOrganizationId());
        if (rows.size() != 1) throw new IdentityException(403, "No active DIGIT membership was found");
        IdentityUserContext row = rows.get(0);
        if (!row.getOrganizationAlias().equals(assertion.getOrganizationAlias())) {
            throw new IdentityException(403, "Organization alias does not match the mapped organization");
        }
        Set<Role> roles = new HashSet<Role>();
        for (String code : roleCodes(row.getUser().getId(), row.getTenantId())) {
            roles.add(new Role(code, code, row.getTenantId()));
        }
        if (roles.isEmpty()) throw new IdentityException(403, "DIGIT membership has no active roles");
        row.getUser().setRoles(roles);
        return row;
    }

    private List<IdentityUserContext> findUserContext(String issuer, String subject, String organizationId) {
        return jdbcTemplate.query("SELECT u.id, u.uuid, u.username, u.name, u.mobilenumber, u.emailid, " +
                        "u.locale, u.type, u.active, o.organization_id, o.organization_alias, o.tenant_id, " +
                        "m.authorization_version FROM eg_identity_subject s " +
                        "JOIN eg_identity_membership m ON m.subject_id = s.id AND m.active = true " +
                        "JOIN eg_identity_organization o ON o.organization_id = m.organization_id AND o.active = true " +
                        "JOIN eg_user u ON lower(u.uuid) = lower(s.digit_user_uuid) " +
                        "AND u.active = true AND u.type = 'EMPLOYEE' " +
                        "WHERE s.issuer = ? AND s.external_subject = ? AND s.active = true " +
                        "AND o.organization_id = ?",
                (rs, rowNum) -> IdentityUserContext.builder()
                        .user(authUser(rs, rs.getString("tenant_id")))
                        .organizationId(rs.getString("organization_id"))
                        .organizationAlias(rs.getString("organization_alias"))
                        .tenantId(rs.getString("tenant_id"))
                        .authorizationVersion(rs.getLong("authorization_version"))
                        .build(), issuer, subject, organizationId);
    }

    private User authUser(ResultSet rs, String selectedTenant) throws SQLException {
        return User.builder().id(rs.getLong("id")).uuid(text(rs, "uuid"))
                .userName(text(rs, "username")).name(text(rs, "name"))
                .mobileNumber(text(rs, "mobilenumber")).emailId(text(rs, "emailid"))
                .locale(text(rs, "locale")).type(text(rs, "type"))
                .active(rs.getBoolean("active")).tenantId(selectedTenant)
                .roles(new HashSet<Role>()).build();
    }

    private List<String> roleCodes(Long userId, String tenantId) {
        return jdbcTemplate.query("SELECT DISTINCT role_code FROM eg_userrole_v1 " +
                        "WHERE user_id = ? AND role_tenantid = ? ORDER BY role_code",
                (rs, rowNum) -> rs.getString(1), userId, tenantId);
    }

    private String organizationName(String organizationId) {
        return jdbcTemplate.queryForObject(
                "SELECT name FROM eg_identity_organization WHERE organization_id = ?",
                String.class, organizationId);
    }

    private UserRow userByUuid(String uuid) {
        List<UserRow> users = jdbcTemplate.query(
                "SELECT id, tenantid FROM eg_user WHERE lower(uuid) = lower(?) AND active = true",
                (rs, rowNum) -> new UserRow(rs.getLong("id"), rs.getString("tenantid")), uuid);
        if (users.size() != 1) throw new IdentityException(409, "Linked DIGIT user is ambiguous or inactive");
        return users.get(0);
    }

    private UUID uuid(ResultSet rs, int column) throws SQLException {
        Object value = rs.getObject(column);
        return value instanceof UUID ? (UUID) value : UUID.fromString(value.toString());
    }

    private UUID uuid(ResultSet rs, String column) throws SQLException {
        Object value = rs.getObject(column);
        return value instanceof UUID ? (UUID) value : UUID.fromString(value.toString());
    }

    private String text(ResultSet rs, String column) throws SQLException {
        String value = rs.getString(column);
        return value == null ? "" : value;
    }

    private static class MembershipRow {
        private final UUID id;
        private final String digitUserUuid;
        private final String tenantId;

        private MembershipRow(UUID id, String digitUserUuid, String tenantId) {
            this.id = id;
            this.digitUserUuid = digitUserUuid;
            this.tenantId = tenantId;
        }
    }

    private static class UserRow {
        private final Long id;
        private final String userTenantId;

        private UserRow(Long id, String userTenantId) {
            this.id = id;
            this.userTenantId = userTenantId;
        }
    }
}
