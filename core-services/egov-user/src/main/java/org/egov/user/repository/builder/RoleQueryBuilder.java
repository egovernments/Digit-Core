package org.egov.user.repository.builder;

import org.springframework.stereotype.Component;

import static org.egov.common.utils.MultiStateInstanceUtil.SCHEMA_REPLACE_STRING;

@Component
public class RoleQueryBuilder {

    public static final String GET_ROLES_BY_ID_TENANTID = "select roleid,roleidtenantid from " + SCHEMA_REPLACE_STRING + ".eg_userrole where userid=:userId and tenantid=:tenantId";
    public static final String GET_ROLES_BY_ROLEIDS = "select * from " + SCHEMA_REPLACE_STRING + ".eg_role where id in (:id) and tenantid=:tenantId";
    public static final String GET_ROLE_BYTENANT_ANDCODE = "select * from " + SCHEMA_REPLACE_STRING + ".eg_role where code =:code and tenantid=:tenantId";
    public static final String INSERT_USER_ROLES = "insert into " + SCHEMA_REPLACE_STRING + ".eg_userrole_v1(role_code, role_tenantid, user_id, " +
            "user_tenantid, lastmodifieddate) values(:role_code,:role_tenantid,:user_id,:user_tenantid,:lastmodifieddate)";

    public static final String DELETE_USER_ROLES = "delete from " + SCHEMA_REPLACE_STRING + ".eg_userrole_v1 where user_id=:user_id and " +
            "user_tenantid=:user_tenantid";
}

