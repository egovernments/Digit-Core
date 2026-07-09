package org.egov.keycloak.auth.config;

import lombok.Getter;
import lombok.extern.jbosslog.JBossLog;

/**
 * Runtime configuration resolved from environment variables.
 * <p>
 * Instantiated ONCE per factory in {@code init()} — not per request.
 * <p>
 * Channel configs are accessed via {@link #getEmail()} and {@link #getSms()}.
 * Each factory passes only its own {@link ChannelConfig} into the authenticator,
 * so the authenticator never needs to know which channel it is.
 */
@Getter
@JBossLog
public class OtpConfig {

	// OTP service base URL
	private final String otpHost;

	// Full API paths (host + path = complete URL)
	private final String otpGeneratePath;
	private final String otpResendPath;
	private final String otpVerifyPath;
	private final String otpInvalidatePath;

	// Per-channel configs
	private final ChannelConfig email;
	private final ChannelConfig sms;
	private final ChannelConfig registration;

	// Keycloak user attribute names
	private final String emailDestinationAttr;
	private final String smsDestinationAttr;

	// HTTP client transport timeouts (milliseconds)
	private final int connectTimeoutMs;
	private final int requestTimeoutMs;

	public OtpConfig() {
		this.otpHost = getEnv("OTP_HOST", "http://localhost:8081");
		this.otpGeneratePath = getEnv("OTP_GENERATE_PATH", "/otp/v3/generate");
		this.otpResendPath = getEnv("OTP_RESEND_PATH", "/otp/v3/resend");
		this.otpVerifyPath = getEnv("OTP_VERIFY_PATH", "/otp/v3/verify");
		this.otpInvalidatePath = getEnv("OTP_INVALIDATE_PATH", "/otp/v3/invalidate");

		this.email = new ChannelConfig(getEnv("OTP_EMAIL_PURPOSE", "login"));
		this.sms = new ChannelConfig(getEnv("OTP_SMS_PURPOSE", "login"));
		this.registration = new ChannelConfig(getEnv("OTP_REGISTRATION_PURPOSE", "registration"));

		this.emailDestinationAttr = getEnv("KEYCLOAK_EMAIL_DESTINATION_ATTRIBUTE", "email");
		this.smsDestinationAttr = getEnv("KEYCLOAK_SMS_DESTINATION_ATTRIBUTE", "mobileNumber");

		this.connectTimeoutMs = getEnvInt("HTTP_CLIENT_CONNECT_TIMEOUT_MS", 3000);
		this.requestTimeoutMs = getEnvInt("HTTP_CLIENT_REQUEST_TIMEOUT_MS", 5000);

		log.infof("OtpConfig loaded: host=%s emailPurpose=%s smsPurpose=%s registrationPurpose=%s connectTimeoutMs=%d requestTimeoutMs=%d",
				otpHost, email.purpose(), sms.purpose(), registration.purpose(), connectTimeoutMs, requestTimeoutMs);
	}

	private static String getEnv(String name, String defaultValue) {
		String v = System.getenv(name);
		return (v != null && !v.isBlank()) ? v.trim() : defaultValue;
	}

	private static int getEnvInt(String name, int defaultValue) {
		String v = System.getenv(name);
		if (v == null || v.isBlank()) return defaultValue;
		try {
			return Integer.parseInt(v.trim());
		} catch (NumberFormatException e) {
			log.warnf("Invalid int for %s='%s', using default=%d", name, v, defaultValue);
			return defaultValue;
		}
	}

	/**
	 * Immutable per-channel settings.
	 *
	 * @param purpose value forwarded to the OTP service (e.g. "login"). OTP length
	 *                and destination type are now decided by the OTP service itself.
	 */
	public record ChannelConfig(String purpose) {
	}
}