package org.egov.user.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.egov.user.domain.model.enums.LoginStatus;
import org.egov.user.domain.model.enums.LoginType;

import java.util.List;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
public class LoginAudit {

    private String tenantId;
    private String userid;
    private Long createdTime;
    private String ipAddress;
    private LoginStatus loginStatus;
    private Set<Role> roles;
    private LoginType loginType ;

}
