package org.egov.requestvalidation.core;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/** Immutable configuration for the four content rules. */
public final class ContentPolicy {
    private final boolean markupStart;
    private final boolean urlScheme;
    private final boolean eventHandler;
    private final Set<Integer> disallowedControls;
    private final Set<String> deniedSchemes;
    private final Set<String> deniedDataMediaTypes;
    private final int decodeRounds;
    private final boolean normalizeNfkc;

    private ContentPolicy(Builder builder) {
        this.markupStart = builder.markupStart;
        this.urlScheme = builder.urlScheme;
        this.eventHandler = builder.eventHandler;
        this.disallowedControls = immutableControls(builder.disallowedControls);
        this.deniedSchemes = immutableLowercase(builder.deniedSchemes, "deniedSchemes");
        this.deniedDataMediaTypes = immutableLowercase(
                builder.deniedDataMediaTypes, "deniedDataMediaTypes");
        if (builder.decodeRounds < 0 || builder.decodeRounds > 3) {
            throw new IllegalArgumentException("decodeRounds must be between 0 and 3");
        }
        this.decodeRounds = builder.decodeRounds;
        this.normalizeNfkc = builder.normalizeNfkc;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isMarkupStart() {
        return markupStart;
    }

    public boolean isUrlScheme() {
        return urlScheme;
    }

    public boolean isEventHandler() {
        return eventHandler;
    }

    public Set<Integer> getDisallowedControls() {
        return disallowedControls;
    }

    public Set<String> getDeniedSchemes() {
        return deniedSchemes;
    }

    public Set<String> getDeniedDataMediaTypes() {
        return deniedDataMediaTypes;
    }

    public int getDecodeRounds() {
        return decodeRounds;
    }

    public boolean isNormalizeNfkc() {
        return normalizeNfkc;
    }

    private static Set<Integer> immutableControls(Set<Integer> values) {
        Objects.requireNonNull(values, "disallowedControls");
        LinkedHashSet<Integer> copy = new LinkedHashSet<Integer>();
        for (Integer value : values) {
            if (value == null || value.intValue() < 0 || value.intValue() > 31
                    || value.intValue() == 9 || value.intValue() == 10 || value.intValue() == 13) {
                throw new IllegalArgumentException(
                        "disallowedControls must contain only C0 controls other than TAB, LF and CR");
            }
            copy.add(value);
        }
        return Collections.unmodifiableSet(copy);
    }

    private static Set<String> immutableLowercase(Set<String> values, String label) {
        Objects.requireNonNull(values, label);
        LinkedHashSet<String> copy = new LinkedHashSet<String>();
        for (String value : values) {
            if (value == null || value.trim().isEmpty()) {
                throw new IllegalArgumentException(label + " contains a blank value");
            }
            copy.add(value.trim().toLowerCase(Locale.ROOT));
        }
        return Collections.unmodifiableSet(copy);
    }

    public static final class Builder {
        private boolean markupStart = true;
        private boolean urlScheme = true;
        private boolean eventHandler = true;
        private Set<Integer> disallowedControls = Collections.singleton(Integer.valueOf(0));
        private Set<String> deniedSchemes = defaultSchemes();
        private Set<String> deniedDataMediaTypes = defaultDataMediaTypes();
        private int decodeRounds = 2;
        private boolean normalizeNfkc;

        private Builder() {
        }

        public Builder markupStart(boolean value) {
            this.markupStart = value;
            return this;
        }

        public Builder urlScheme(boolean value) {
            this.urlScheme = value;
            return this;
        }

        public Builder eventHandler(boolean value) {
            this.eventHandler = value;
            return this;
        }

        public Builder disallowedControls(Set<Integer> values) {
            this.disallowedControls = values;
            return this;
        }

        public Builder deniedSchemes(Set<String> values) {
            this.deniedSchemes = values;
            return this;
        }

        public Builder deniedDataMediaTypes(Set<String> values) {
            this.deniedDataMediaTypes = values;
            return this;
        }

        public Builder decodeRounds(int value) {
            this.decodeRounds = value;
            return this;
        }

        public Builder normalizeNfkc(boolean value) {
            this.normalizeNfkc = value;
            return this;
        }

        public ContentPolicy build() {
            return new ContentPolicy(this);
        }

        private static Set<String> defaultSchemes() {
            LinkedHashSet<String> values = new LinkedHashSet<String>();
            values.add("javascript");
            values.add("vbscript");
            return values;
        }

        private static Set<String> defaultDataMediaTypes() {
            LinkedHashSet<String> values = new LinkedHashSet<String>();
            values.add("text/html");
            values.add("application/xhtml+xml");
            values.add("image/svg+xml");
            return values;
        }
    }
}
