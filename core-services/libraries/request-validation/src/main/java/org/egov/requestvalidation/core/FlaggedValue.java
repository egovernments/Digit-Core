package org.egov.requestvalidation.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** A string value the content check flagged, with read access to the other string values of the same body. */
public final class FlaggedValue {
    private final List<String> path;
    private final String value;
    private final String rule;
    private final Map<String, String> strings;

    FlaggedValue(List<String> path, String value, String rule, Map<String, String> strings) {
        this.path = Collections.unmodifiableList(new ArrayList<String>(path));
        this.value = value;
        this.rule = rule;
        this.strings = strings;
    }

    /** Raw (unescaped) segments of the value's location; array indexes are decimal strings. */
    public List<String> path() {
        return path;
    }

    /** The JSON Pointer of the value, e.g. {@code /messages/3/message}; "" for a bare string body. */
    public String pointer() {
        return format(path);
    }

    public String value() {
        return value;
    }

    /** The content rule that flagged the value: R1..R4. */
    public String rule() {
        return rule;
    }

    /** True when the path has exactly the pattern's segments; a {@code *} segment matches any one segment. */
    public boolean matches(String pattern) {
        List<String> segments = parse(pattern);
        if (segments.size() != path.size()) {
            return false;
        }
        for (int index = 0; index < segments.size(); index++) {
            String expected = segments.get(index);
            if (!"*".equals(expected) && !expected.equals(path.get(index))) {
                return false;
            }
        }
        return true;
    }

    /**
     * The string value at a JSON Pointer of the same body; empty when absent or not a string. With duplicate keys
     * allowed (reject-duplicate-keys=false) the last occurrence wins, as in Jackson data binding.
     */
    public Optional<String> string(String pointer) {
        return Optional.ofNullable(strings.get(format(parse(pointer))));
    }

    /** The string value of another field of the object that holds this value; empty for array elements. */
    public Optional<String> sibling(String name) {
        Objects.requireNonNull(name, "name");
        if (path.isEmpty()) {
            return Optional.empty();
        }
        List<String> target = new ArrayList<String>(path.subList(0, path.size() - 1));
        target.add(name);
        return Optional.ofNullable(strings.get(format(target)));
    }

    @Override
    public String toString() {
        // Never the value: it is untrusted input.
        return "FlaggedValue[rule=" + rule + ", location=" + SafeLocationFormatter.format(path)
                + ", length=" + value.length() + "]";
    }

    static String format(List<String> segments) {
        StringBuilder pointer = new StringBuilder();
        for (String segment : segments) {
            pointer.append('/').append(segment.replace("~", "~0").replace("/", "~1"));
        }
        return pointer.toString();
    }

    private static List<String> parse(String pointer) {
        Objects.requireNonNull(pointer, "pointer");
        if (pointer.isEmpty()) {
            return Collections.emptyList();
        }
        if (pointer.charAt(0) != '/') {
            throw new IllegalArgumentException("Not a JSON Pointer");
        }
        String[] encoded = pointer.substring(1).split("/", -1);
        List<String> decoded = new ArrayList<String>(encoded.length);
        for (String segment : encoded) {
            StringBuilder value = new StringBuilder(segment.length());
            for (int index = 0; index < segment.length(); index++) {
                char current = segment.charAt(index);
                if (current != '~') {
                    value.append(current);
                } else if (index + 1 < segment.length() && segment.charAt(index + 1) == '0') {
                    value.append('~');
                    index++;
                } else if (index + 1 < segment.length() && segment.charAt(index + 1) == '1') {
                    value.append('/');
                    index++;
                } else {
                    throw new IllegalArgumentException("Invalid JSON Pointer escape");
                }
            }
            decoded.add(value.toString());
        }
        return decoded;
    }
}
