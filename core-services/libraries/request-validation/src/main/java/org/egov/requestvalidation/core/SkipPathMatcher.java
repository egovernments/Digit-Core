package org.egov.requestvalidation.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Immutable matcher for JSON-Pointer-style subtree skip patterns. */
public final class SkipPathMatcher {
    private final List<List<String>> patterns;

    public SkipPathMatcher(List<String> pointers) {
        Objects.requireNonNull(pointers, "pointers");
        List<List<String>> compiled = new ArrayList<List<String>>(pointers.size());
        for (String pointer : pointers) {
            compiled.add(parse(pointer));
        }
        this.patterns = Collections.unmodifiableList(compiled);
    }

    public boolean matches(List<String> path) {
        Objects.requireNonNull(path, "path");
        for (List<String> pattern : patterns) {
            if (pattern.size() > path.size()) {
                continue;
            }
            boolean match = true;
            for (int index = 0; index < pattern.size(); index++) {
                String expected = pattern.get(index);
                String actual = path.get(index);
                if (!"*".equals(expected) && !expected.equals(actual)) {
                    match = false;
                    break;
                }
            }
            if (match) {
                return true;
            }
        }
        return false;
    }

    private static List<String> parse(String pointer) {
        Objects.requireNonNull(pointer, "skip path");
        if (pointer.isEmpty()) {
            // "" is the whole-document pointer. Allowing it would be a whole-body exclusion
            // without the reason that structured = DISABLED requires.
            throw new IllegalArgumentException(
                    "Empty skip path excludes the whole body; use structured = DISABLED with a reason");
        }
        if (pointer.charAt(0) != '/') {
            throw new IllegalArgumentException("Skip path must be a JSON Pointer");
        }
        String[] encoded = pointer.substring(1).split("/", -1);
        List<String> decoded = new ArrayList<String>(encoded.length);
        for (String segment : encoded) {
            String value = decodeSegment(segment);
            if (value.indexOf('*') >= 0 && !"*".equals(value)) {
                throw new IllegalArgumentException("Wildcard must occupy an entire path segment");
            }
            decoded.add(value);
        }
        return Collections.unmodifiableList(decoded);
    }

    private static String decodeSegment(String segment) {
        StringBuilder decoded = new StringBuilder(segment.length());
        for (int index = 0; index < segment.length(); index++) {
            char current = segment.charAt(index);
            if (current != '~') {
                decoded.append(current);
                continue;
            }
            if (index + 1 >= segment.length()) {
                throw new IllegalArgumentException("Invalid JSON Pointer escape");
            }
            char escaped = segment.charAt(++index);
            if (escaped == '0') {
                decoded.append('~');
            } else if (escaped == '1') {
                decoded.append('/');
            } else {
                throw new IllegalArgumentException("Invalid JSON Pointer escape");
            }
        }
        return decoded.toString();
    }
}
