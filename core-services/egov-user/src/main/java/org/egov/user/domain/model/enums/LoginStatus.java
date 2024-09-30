package org.egov.user.domain.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum LoginStatus {
    SUCCESS, FAILURE;

    @JsonCreator
    public static LoginStatus fromValue(String text) {
        for (LoginStatus loginStatus : LoginStatus.values()) {
            if (String.valueOf(loginStatus).equalsIgnoreCase(text)) {
                return loginStatus;
            }
        }
        return null;
    }


}
