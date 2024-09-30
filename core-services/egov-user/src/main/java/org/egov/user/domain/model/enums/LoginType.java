package org.egov.user.domain.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum LoginType {
    OTP, PASSWORD;

    @JsonCreator
    public static LoginType fromValue(String text) {



        for (LoginType loginType : LoginType.values()) {
            if (String.valueOf(loginType).equalsIgnoreCase(text)) {
                return loginType;
            }
        }
        return null;
    }

}
