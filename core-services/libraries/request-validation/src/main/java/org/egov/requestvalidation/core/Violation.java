package org.egov.requestvalidation.core;

import java.util.Objects;

public final class Violation {
    private final ViolationCode code;
    private final String ruleId;
    private final String location;
    private final int length;

    public Violation(ViolationCode code, String ruleId, String location, int length) {
        this.code = Objects.requireNonNull(code, "code");
        this.ruleId = requireSafeRuleId(ruleId);
        this.location = SafeLocationFormatter.sanitizeLocation(location);
        if (length < 0) {
            throw new IllegalArgumentException("length must not be negative");
        }
        this.length = length;
    }

    public ViolationCode getCode() {
        return code;
    }

    public String getRuleId() {
        return ruleId;
    }

    public String getLocation() {
        return location;
    }

    public int getLength() {
        return length;
    }

    private static String requireSafeRuleId(String value) {
        Objects.requireNonNull(value, "ruleId");
        if (!value.matches("[A-Za-z0-9_.-]{1,64}")) {
            throw new IllegalArgumentException("ruleId is not safe");
        }
        return value;
    }
}
