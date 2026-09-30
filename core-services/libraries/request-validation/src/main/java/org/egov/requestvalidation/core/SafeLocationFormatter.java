package org.egov.requestvalidation.core;

import java.util.List;
import java.util.Objects;

public final class SafeLocationFormatter {
    private static final int MAX_SAFE_SEGMENT = 64;

    private SafeLocationFormatter() {
    }

    public static String format(List<String> segments) {
        Objects.requireNonNull(segments, "segments");
        if (segments.isEmpty()) {
            return "/";
        }
        StringBuilder result = new StringBuilder();
        for (String segment : segments) {
            result.append('/').append(scalar(segment));
        }
        return result.toString();
    }

    public static String scalar(String value) {
        return isSafeSegment(value) ? value : "*";
    }

    // Same set as [A-Za-z0-9_.-]{1,64}; a character scan because this runs on hot paths.
    private static boolean isSafeSegment(String value) {
        if (value == null || value.isEmpty() || value.length() > MAX_SAFE_SEGMENT) {
            return false;
        }
        for (int index = 0; index < value.length(); index++) {
            char current = value.charAt(index);
            boolean safe = (current >= 'a' && current <= 'z') || (current >= 'A' && current <= 'Z')
                    || (current >= '0' && current <= '9') || current == '_' || current == '.' || current == '-';
            if (!safe) {
                return false;
            }
        }
        return true;
    }

    static String sanitizeLocation(String value) {
        if (value == null || value.isEmpty()) {
            return "*";
        }
        if (value.charAt(0) != '/') {
            return scalar(value);
        }
        if ("/".equals(value)) {
            return value;
        }
        String[] parts = value.substring(1).split("/", -1);
        StringBuilder safe = new StringBuilder();
        for (String part : parts) {
            safe.append('/').append(scalar(part));
        }
        return safe.toString();
    }
}
