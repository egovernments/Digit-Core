package org.egov.requestvalidation.config;

import org.egov.requestvalidation.ValidationMode;
import org.egov.requestvalidation.core.InspectionLimits;
import org.egov.requestvalidation.core.SkipPathMatcher;

import java.util.List;

public record EffectiveValidationPolicy(boolean enabled, boolean structured, ValidationMode mode,
        List<String> skipPaths, SkipPathMatcher matcher, InspectionLimits limits) {
    public EffectiveValidationPolicy {
        skipPaths = List.copyOf(skipPaths);
    }
}
