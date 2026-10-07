package org.egov.requestvalidation.config;

import org.egov.requestvalidation.ValidationMode;
import org.egov.requestvalidation.core.ContentExemption;
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
    private final ContentExemption exemption;

    public EffectiveValidationPolicy(boolean enabled, boolean structured, ValidationMode mode,
            List<String> skipPaths, SkipPathMatcher matcher, InspectionLimits limits) {
        this(enabled, structured, mode, skipPaths, matcher, limits, null);
    }

    /** exemption is null when the handler declares none. */
    public EffectiveValidationPolicy(boolean enabled, boolean structured, ValidationMode mode,
            List<String> skipPaths, SkipPathMatcher matcher, InspectionLimits limits, ContentExemption exemption) {
        this.enabled = enabled;
        this.structured = structured;
        this.mode = mode;
        this.skipPaths = immutableCopy(skipPaths);
        this.matcher = matcher;
        this.limits = limits;
        this.exemption = exemption;
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
    public ContentExemption exemption() { return exemption; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof EffectiveValidationPolicy)) return false;
        EffectiveValidationPolicy that = (EffectiveValidationPolicy) other;
        return enabled == that.enabled && structured == that.structured && mode == that.mode
                && skipPaths.equals(that.skipPaths) && Objects.equals(matcher, that.matcher)
                && Objects.equals(limits, that.limits) && Objects.equals(exemption, that.exemption);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled, structured, mode, skipPaths, matcher, limits, exemption);
    }

    @Override
    public String toString() {
        return "EffectiveValidationPolicy[enabled=" + enabled + ", structured=" + structured + ", mode=" + mode
                + ", skipPaths=" + skipPaths + ", matcher=" + matcher + ", limits=" + limits
                + (exemption == null ? "" : ", exemption=" + exemption.getClass().getName()) + "]";
    }
}
