package org.egov.user.identity;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class IdentityContext {
    private String organizationId;
    private String organizationAlias;
    private String tenantId;
    private String name;
    private List<String> roles;
    private boolean active;
}
