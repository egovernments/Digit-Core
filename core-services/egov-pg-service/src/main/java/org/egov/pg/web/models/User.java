package org.egov.pg.web.models;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private String uuid;

    @NotNull
    private String name;

    private String userName;

    @NotNull
    private String mobileNumber;

    private String emailId;

    @NotNull
    private String tenantId;

    public User(org.egov.common.contract.request.User user) {
        this.uuid = user.getUuid();
        this.name = user.getName();
        this.userName = user.getUserName();
        this.mobileNumber = user.getMobileNumber();
        this.emailId = user.getEmailId();
        this.tenantId = user.getTenantId();
    }

}
