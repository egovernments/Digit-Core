package org.egov.user.domain.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum LoginStatus {
    SUCCESS("SUCCESS"), FAILURE("FAILURE");

    private String value;

    LoginStatus(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return name();
    }

    @JsonCreator
    public static LoginStatus fromValue(String passedValue) {
        for (LoginStatus obj : LoginStatus.values()) {
            if (String.valueOf(obj.value).equals(passedValue.toUpperCase())) {
                return obj;
            }
        }
        return null;
    }


}
