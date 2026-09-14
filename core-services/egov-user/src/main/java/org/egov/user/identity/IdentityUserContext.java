package org.egov.user.identity;

import lombok.Builder;
import lombok.Getter;
import org.egov.user.web.contract.auth.User;

@Getter
@Builder
public class IdentityUserContext {
    private User user;
    private String organizationId;
    private String organizationAlias;
    private String tenantId;
    private long authorizationVersion;
}
