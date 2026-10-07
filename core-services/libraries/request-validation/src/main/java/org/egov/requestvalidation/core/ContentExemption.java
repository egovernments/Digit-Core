package org.egov.requestvalidation.core;

/**
 * Lets one endpoint accept a string value that the content check flagged, for a field that legitimately holds
 * markup (an HTML email template, a type name such as {@code List<String>}). Consulted only for flagged string
 * values, never for field names, and only after the whole body passed the syntax and limit checks; it can accept
 * a finding but never add one. Returning false, or throwing, keeps the finding.
 */
@FunctionalInterface
public interface ContentExemption {
    boolean allows(FlaggedValue value);

    /** The "no exemption" default of {@code @ValidateRequest(exemption)}; never instantiated or consulted. */
    final class None implements ContentExemption {
        private None() {
        }

        @Override
        public boolean allows(FlaggedValue value) {
            return false;
        }
    }
}
