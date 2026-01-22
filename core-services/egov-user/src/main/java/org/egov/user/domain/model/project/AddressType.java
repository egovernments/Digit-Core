package org.egov.user.domain.model.project;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonValue;
@JsonIgnoreProperties(ignoreUnknown = true)
public enum AddressType {
    PERMANENT("PERMANENT"),

    CORRESPONDENCE("CORRESPONDENCE"),

    OTHER("OTHER");

    private String value;

    AddressType(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static AddressType fromValue(String text) {
        for (AddressType b : AddressType.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}