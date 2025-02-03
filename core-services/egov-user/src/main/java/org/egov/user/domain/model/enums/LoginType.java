package org.egov.user.domain.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum LoginType {
    OTP("OTP"), PASSWORD("PASSWORD");

    private String value;

    LoginType(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return name();
    }

    @JsonCreator
    public static LoginType fromValue(String passedValue) {
        for (LoginType obj : LoginType.values()) {
            if (String.valueOf(obj.value).equals(passedValue.toUpperCase())) {
                return obj;
            }
        }
        return null;
    }


}
