package org.egov.user.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A resolved MDMS {@code MobileNumberValidation} entry — the regex to validate against, and the
 * countryCode that entry is configured for (may be null if the entry didn't declare one). Carrying
 * both together (rather than just the regex) lets a caller resolving an unspecified countryCode use
 * the one MDMS itself configured as the {@code default} entry, instead of falling straight through
 * to the application.properties-level fallback.
 *
 * Getters/setters and a no-args constructor are kept for Jackson (de)serialization when this is
 * cached — see {@link org.egov.user.repository.MobileNumerValidationCacheRepository}.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class MobileValidationRule {
    private String regex;
    private String countryCode;
}
