package com.example.keycloak.policy;


import org.keycloak.representations.idm.authorization.AbstractPolicyRepresentation;

public class EmailDomainPolicyRepresentation extends AbstractPolicyRepresentation {
    private String allowedEmail;
    private String code;

    public String getAllowedEmail() {
        return allowedEmail;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setAllowedEmail(String allowedEmail) {
        this.allowedEmail = allowedEmail;
    }
}