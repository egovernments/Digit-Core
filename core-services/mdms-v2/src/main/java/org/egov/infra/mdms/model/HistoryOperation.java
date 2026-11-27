package org.egov.infra.mdms.model;

import java.util.Arrays;

public enum HistoryOperation {

    MIGRATION,
    INSERT,
    UPDATE,
    DELETE,
    TRUNCATE;

    public static HistoryOperation fromValue(String value) {
        if (value == null)
            return null;
        return Arrays.stream(values())
                .filter(operation -> operation.name().equals(value.trim()))
                .findFirst()
                .orElse(null);
    }
}
