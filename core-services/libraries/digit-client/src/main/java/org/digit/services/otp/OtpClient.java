package org.digit.services.otp;

import org.digit.config.ApiProperties;
import org.digit.exception.DigitClientException;
import org.digit.services.otp.model.GenerateOtpRequest;
import org.digit.services.otp.model.GenerateOtpResponse;
import org.digit.services.otp.model.InvalidateOtpRequest;
import org.digit.services.otp.model.InvalidateOtpResponse;
import org.digit.services.otp.model.OtpConfig;
import org.digit.services.otp.model.ResendOtpRequest;
import org.digit.services.otp.model.ResendOtpResponse;
import org.digit.services.otp.model.VerifyOtpRequest;
import org.digit.services.otp.model.VerifyOtpResponse;
import java.util.List;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Client for the OTP service: issuing and verifying one-time codes, and the per-purpose policy that
 * governs them.
 *
 * <p>A {@code purpose} (for example {@code LOGIN} or {@code TENANT_SIGNUP}) selects the policy — code
 * length, lifetime, cooldown and rate limits — and must have a configuration entry before codes can
 * be issued for it.
 */
@Slf4j
@Getter
public class OtpClient {
    private static final ParameterizedTypeReference<List<OtpConfig>> CONFIG_LIST =
            new ParameterizedTypeReference<List<OtpConfig>>() {};

    private final RestTemplate restTemplate;
    private final ApiProperties apiProperties;

    public OtpClient(RestTemplate restTemplate, ApiProperties apiProperties) {
        this.restTemplate = restTemplate;
        this.apiProperties = apiProperties;
    }

    // ── Codes ────────────────────────────────────────────────────────────────

    /**
     * Issues a code and returns the reference to verify it against.
     *
     * <p>The code itself is never returned — it is delivered to the identifier — so verification
     * needs this reference plus whatever the user types.
     */
    public GenerateOtpResponse generateOtp(GenerateOtpRequest request) {
        if (request == null || isBlank(request.getIdentifier()) || isBlank(request.getPurpose())) {
            throw new DigitClientException("identifier and purpose are required to generate an OTP");
        }
        ResponseEntity<GenerateOtpResponse> response = this.restTemplate.postForEntity(
                otpUrl("/generate"), request, GenerateOtpResponse.class);
        return response.getBody();
    }

    public GenerateOtpResponse generateOtp(String identifier, String purpose) {
        return generateOtp(GenerateOtpRequest.builder().identifier(identifier).purpose(purpose).build());
    }

    /**
     * Checks a code.
     *
     * <p>A wrong code is a normal answer, not a failure: {@code verified} comes back false. Being
     * locked out after too many attempts, or presenting an expired reference, is an error from the
     * service.
     */
    public VerifyOtpResponse verifyOtp(VerifyOtpRequest request) {
        if (request == null || isBlank(request.getReferenceId()) || isBlank(request.getOtp())) {
            throw new DigitClientException("referenceId and otp are required to verify");
        }
        ResponseEntity<VerifyOtpResponse> response = this.restTemplate.postForEntity(
                otpUrl("/verify"), request, VerifyOtpResponse.class);
        return response.getBody();
    }

    /**
     * Whether {@code otp} is the code currently outstanding for {@code referenceId}.
     *
     * <p>{@code purpose} is required, and is why this takes three arguments: the service rejects a
     * verify without it, so the previous two-argument form failed every call with
     * {@code purpose is required} and could never return a result. One of {@code login},
     * {@code transaction}, {@code forgot-password}, {@code phone-verify} or {@code registration}.
     */
    public boolean isOtpValid(String referenceId, String purpose, String otp) {
        try {
            VerifyOtpResponse response = verifyOtp(VerifyOtpRequest.builder()
                    .referenceId(referenceId)
                    .purpose(purpose)
                    .otp(otp)
                    .build());
            return response != null && response.isVerified();
        } catch (DigitClientException e) {
            // An unknown referenceId answers 404. For a boolean probe that is simply "not valid",
            // and returning it matches how isFileAvailable and the individual exists* probes behave;
            // throwing here would make a caller branching on the boolean handle an exception for the
            // ordinary case of an expired or already-consumed code. Callers that need to tell "no
            // such reference" from "wrong code" apart should use verifyOtp directly.
            if (e.getHttpStatus() != null
                    && e.getHttpStatus().value() == HttpStatus.NOT_FOUND.value()) {
                log.debug("OTP reference {} not found: {}", referenceId, e.getMessage());
                return false;
            }
            throw e;
        }
    }

    /** Re-sends a code. Subject to the purpose's cooldown and hourly resend cap. */
    public ResendOtpResponse resendOtp(String referenceId) {
        requireText(referenceId, "referenceId is required to resend");
        ResponseEntity<ResendOtpResponse> response = this.restTemplate.postForEntity(
                otpUrl("/resend"), ResendOtpRequest.builder().referenceId(referenceId).build(),
                ResendOtpResponse.class);
        return response.getBody();
    }

    /** Retires a code early, so it can no longer be verified. */
    public boolean invalidateOtp(String referenceId) {
        requireText(referenceId, "referenceId is required to invalidate");
        ResponseEntity<InvalidateOtpResponse> response = this.restTemplate.postForEntity(
                otpUrl("/invalidate"), InvalidateOtpRequest.builder().referenceId(referenceId).build(),
                InvalidateOtpResponse.class);
        return response.getBody() != null && response.getBody().isInvalidated();
    }

    // ── Per-purpose configuration ────────────────────────────────────────────

    public OtpConfig createOtpConfig(OtpConfig config) {
        if (config == null || isBlank(config.getPurpose())) {
            throw new DigitClientException("purpose is required to create an OTP config");
        }
        ResponseEntity<OtpConfig> response = this.restTemplate.postForEntity(
                configUrl(), config, OtpConfig.class);
        return response.getBody();
    }

    /**
     * Every configured purpose.
     *
     * <p>A separate method from {@link #getOtpConfig} because the endpoint is polymorphic: it answers
     * with an array when no purpose is given and a single object when one is.
     */
    public List<OtpConfig> listOtpConfigs() {
        ResponseEntity<List<OtpConfig>> response = this.restTemplate.exchange(
                configUrl(), HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), CONFIG_LIST);
        List<OtpConfig> configs = response.getBody();
        return configs == null ? List.of() : configs;
    }

    /** The configuration for one purpose, or null when that purpose has none. */
    public OtpConfig getOtpConfig(String purpose) {
        requireText(purpose, "purpose is required");
        String url = UriComponentsBuilder.fromUriString(configUrl())
                .queryParam("purpose", purpose).toUriString();
        try {
            ResponseEntity<OtpConfig> response = this.restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), OtpConfig.class);
            return response.getBody();
        }
        catch (DigitClientException e) {
            if (e.getHttpStatus() != null && e.getHttpStatus().value() == 404) {
                return null;
            }
            throw e;
        }
    }

    /** Updates a configuration entry. The service takes the id as a query parameter, not a path. */
    public OtpConfig updateOtpConfig(String id, OtpConfig config) {
        requireText(id, "config id is required");
        if (config == null) {
            throw new DigitClientException("update payload is required");
        }
        String url = UriComponentsBuilder.fromUriString(configUrl()).queryParam("id", id).toUriString();
        ResponseEntity<OtpConfig> response = this.restTemplate.exchange(
                url, HttpMethod.PUT, new HttpEntity<>(config), OtpConfig.class);
        return response.getBody();
    }

    /** Removes the configuration for a purpose, which is identified by purpose rather than id. */
    public boolean deleteOtpConfig(String purpose) {
        requireText(purpose, "purpose is required");
        String url = UriComponentsBuilder.fromUriString(configUrl())
                .queryParam("purpose", purpose).toUriString();
        ResponseEntity<Map<String, Object>> response = this.restTemplate.exchange(
                url, HttpMethod.DELETE, new HttpEntity<>(new HttpHeaders()),
                new ParameterizedTypeReference<Map<String, Object>>() {});
        return response.getBody() != null && Boolean.TRUE.equals(response.getBody().get("deleted"));
    }

    // ── Internals ────────────────────────────────────────────────────────────

    private String otpUrl(String path) {
        return this.apiProperties.getOtpServiceUrl() + "/otp/v3" + path;
    }

    private String configUrl() {
        return this.apiProperties.getOtpServiceUrl() + "/otp/v3/config";
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static void requireText(String value, String message) {
        if (isBlank(value)) {
            throw new DigitClientException(message);
        }
    }
}
