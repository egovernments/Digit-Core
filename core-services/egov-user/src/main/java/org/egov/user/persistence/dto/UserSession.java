package org.egov.user.persistence.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UserSession {

    private String userUuid;
    private String tenantId;
    private String deviceId;
    private String sessionId;
    private String status;
    private long createdTime;
    private long lastServerContact;

}
