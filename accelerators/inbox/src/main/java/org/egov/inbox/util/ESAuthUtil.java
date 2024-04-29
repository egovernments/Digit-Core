package org.egov.inbox.util;

import org.egov.inbox.config.InboxConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Base64;

public class ESAuthUtil {
    private final InboxConfiguration config;

    @Autowired
    public ESAuthUtil(InboxConfiguration config) {
        this.config = config;
    }


    public String getESEncodedCredentials() {
        String credentials = config.getEsUserName() + ":" + config.getEsPassword();
        byte[] credentialsBytes = credentials.getBytes();
        byte[] base64CredentialsBytes = Base64.getEncoder().encode(credentialsBytes);
        return "Basic " + new String(base64CredentialsBytes);
    }
}
