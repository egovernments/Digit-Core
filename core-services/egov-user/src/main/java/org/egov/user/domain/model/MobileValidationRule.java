package org.egov.user.domain.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * A resolved MDMS {@code MobileNumberValidation} entry — the regex to validate against, and the
 * countryCode that entry is configured for (may be null if the entry didn't declare one). Carrying
 * both together (rather than just the regex) lets a caller resolving an unspecified countryCode use
 * the one MDMS itself configured as the {@code default} entry, instead of falling straight through
 * to the application.properties-level fallback.
 */
@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class MobileValidationRule {
    private String regex;
    private String countryCode;
}
