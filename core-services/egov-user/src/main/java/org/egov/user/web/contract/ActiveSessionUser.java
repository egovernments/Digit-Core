package org.egov.user.web.contract;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One currently-ACTIVE mobile session joined with its user's display details.
 * Populated by BeanPropertyRowMapper from the columns selected in
 * UserSessionQueryBuilder.SELECT_ACTIVE_SESSIONS_BY_TENANT_SQL.
 */
@Data
@NoArgsConstructor
public class ActiveSessionUser {

    @JsonProperty("userUuid")
    private String userUuid;

    @JsonProperty("userName")
    private String userName;

    @JsonProperty("name")
    private String name;

    @JsonProperty("deviceId")
    private String deviceId;

    @JsonProperty("sessionId")
    private String sessionId;

    @JsonProperty("lastServerContact")
    private long lastServerContact;
}
