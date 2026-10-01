package org.egov.requestvalidation.config;

import org.egov.requestvalidation.ValidationMode;
import org.egov.requestvalidation.core.InspectionLimits;
import org.egov.requestvalidation.core.SkipPathMatcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Immutable value with record-style accessors; a class rather than a record so the library also builds for Java 8. */
public final class EffectiveValidationPolicy {
    private final boolean enabled;
    private final boolean structured;
    private final ValidationMode mode;
    private final List<String> skipPaths;
    private final SkipPathMatcher matcher;
    private final InspectionLimits limits;

    public EffectiveValidationPolicy(boolean enabled, boolean structured, ValidationMode mode,
            List<String> skipPaths, SkipPathMatcher matcher, InspectionLimits limits) {
        this.enabled = enabled;
        this.structured = structured;
        this.mode = mode;
        this.skipPaths = immutableCopy(skipPaths);
        this.matcher = matcher;
        this.limits = limits;
    }

    /** Java 8 equivalent of List.copyOf: rejects null elements and returns an unmodifiable copy. */
    static List<String> immutableCopy(Collection<String> values) {
        List<String> copy = new ArrayList<String>(values);
        for (String value : copy) {
            Objects.requireNonNull(value);
        }
        return Collections.unmodifiableList(copy);
    }

    public boolean enabled() { return enabled; }
    public boolean structured() { return structured; }
    public ValidationMode mode() { return mode; }
    public List<String> skipPaths() { return skipPaths; }
    public SkipPathMatcher matcher() { return matcher; }
    public InspectionLimits limits() { return limits; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof EffectiveValidationPolicy)) return false;
        EffectiveValidationPolicy that = (EffectiveValidationPolicy) other;
        return enabled == that.enabled && structured == that.structured && mode == that.mode
                && skipPaths.equals(that.skipPaths) && Objects.equals(matcher, that.matcher)
                && Objects.equals(limits, that.limits);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled, structured, mode, skipPaths, matcher, limits);
    }

    @Override
    public String toString() {
        return "EffectiveValidationPolicy[enabled=" + enabled + ", structured=" + structured + ", mode=" + mode
                + ", skipPaths=" + skipPaths + ", matcher=" + matcher + ", limits=" + limits + "]";
    }
}
