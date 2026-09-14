package org.egov.user.identity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class IdentityAssertion {
    private final String issuer;
    private final String subject;
    private final String organizationId;
    private final String organizationAlias;
}
