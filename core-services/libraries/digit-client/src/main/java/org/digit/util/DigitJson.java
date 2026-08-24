package org.digit.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * The single place the library's Jackson behaviour is defined.
 *
 * <p>Every mapper in this library must come from here. There are seven call sites (the RestTemplate
 * converters, the registry cache mapper, the non-Spring factory, the JWT decoder, and the mapper
 * {@code BillingClient} uses for {@code convertValue}), and configuring only some of them leaves
 * clients parsing responses under different rules depending on how they were built.
 *
 * <p>Returns {@link JsonMapper} rather than {@code ObjectMapper} because Spring's Jackson 3 message
 * converter accepts only the former — it has no mapper setter. {@code JsonMapper} extends
 * {@code ObjectMapper}, so callers holding the wider type are unaffected.
 *
 * <p>No mapper is exposed as a Spring bean, deliberately. Boot declares its own
 * {@code @Primary @ConditionalOnMissingBean JsonMapper}; publishing one here could suppress it and
 * silently make this library's rules govern the host application's own serialization.
 */
public final class DigitJson {

    private static volatile JsonMapper shared;

    private DigitJson() {
    }

    /**
     * A new mapper configured for talking to DIGIT services.
     *
     * <p>Runs on Jackson 3 defaults except for the four settings below. Flips accepted deliberately:
     * properties serialize alphabetically unless {@code @JsonPropertyOrder} says otherwise (which it
     * does everywhere order matters); dates serialize as ISO strings rather than epoch numbers, which
     * is what the services emit and expect; getters no longer act as setters; and constructor
     * parameter names are detected, which is harmless here because every builder class also has a
     * no-args constructor and so keeps using setters.
     */
    public static JsonMapper mapper() {
        return JsonMapper.builder()

                // Services are released independently of this library, so a response field we don't
                // know about must never fail the call. Now the Jackson 3 default; kept explicit
                // because this is the reason the library is shaped this way, and the default has
                // already moved once.
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

                // Jackson 3 turns this on, which would make an explicit null for a primitive throw
                // where it previously read as 0/false. 93 primitive fields across 43 models are
                // exposed, including creator-based records where it would abort the whole payload.
                // Same reasoning as above: a service-side change must not fail the call.
                .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)

                // Also on by default in Jackson 3. JwtTokenUtil parses a base64url-decoded segment
                // whose padding slack can leave trailing bytes, so tokens that work today would
                // start failing.
                .disable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)

                // Unknown enum *values* throw even when unknown properties are ignored, so this
                // needs its own setting. Load-bearing: without it, a new status added by any service
                // turns into a 500 for every consumer. Moved to EnumFeature in Jackson 3.
                .enable(EnumFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)

                // Several services parse request bodies with a strict mapper, which rejects keys
                // their DTO doesn't declare. Omitting nulls keeps outbound bodies to the fields
                // actually being set.
                .changeDefaultPropertyInclusion(i -> i.withValueInclusion(JsonInclude.Include.NON_NULL))

                // Deliberately NOT disabling scalar coercion: billing serialises every monetary
                // BigDecimal as a quoted string, and reading those back relies on it.
                .build();
    }

    /**
     * A lazily created shared mapper, for {@code static final} fields that cannot be injected.
     * Prefer passing one in wherever construction is under your control.
     */
    public static JsonMapper shared() {
        JsonMapper local = shared;
        if (local == null) {
            synchronized (DigitJson.class) {
                local = shared;
                if (local == null) {
                    local = mapper();
                    shared = local;
                }
            }
        }
        return local;
    }
}
