package org.egov.keycloak.auth.config;

import lombok.Getter;
import lombok.extern.jbosslog.JBossLog;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

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

	// Realm roles granted to users created by the mobile self-registration flow
	private final List<String> registrationRoles;

	// HTTP client transport timeouts (milliseconds)
	private final int connectTimeoutMs;
	private final int requestTimeoutMs;

	public OtpConfig() {
		this.otpHost = getEnv("OTP_HOST", "http://localhost:8081");
		this.otpGeneratePath = getEnv("OTP_GENERATE_PATH", "/otp/v3/generate");
		this.otpResendPath = getEnv("OTP_RESEND_PATH", "/otp/v3/resend");
		this.otpVerifyPath = getEnv("OTP_VERIFY_PATH", "/otp/v3/verify");
		this.otpInvalidatePath = getEnv("OTP_INVALIDATE_PATH", "/otp/v3/invalidate");

		this.email = new ChannelConfig(getEnv("OTP_EMAIL_PURPOSE", "login"), getEnv("OTP_EMAIL_DEFAULT_OTP", null));
		this.sms = new ChannelConfig(getEnv("OTP_SMS_PURPOSE", "login"), getEnv("OTP_SMS_DEFAULT_OTP", null));
		this.registration = new ChannelConfig(getEnv("OTP_REGISTRATION_PURPOSE", "registration"), getEnv("OTP_REGISTRATION_DEFAULT_OTP", null));

		this.emailDestinationAttr = getEnv("KEYCLOAK_EMAIL_DESTINATION_ATTRIBUTE", "email");
		this.smsDestinationAttr = getEnv("KEYCLOAK_SMS_DESTINATION_ATTRIBUTE", "mobileNumber");

		this.registrationRoles = Arrays.stream(
						getEnvOrProp("OTP_REGISTRATION_ROLES", "otp.registration.roles", "CITIZEN").split(","))
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.toList();

		this.connectTimeoutMs = getEnvInt("HTTP_CLIENT_CONNECT_TIMEOUT_MS", 3000);
		this.requestTimeoutMs = getEnvInt("HTTP_CLIENT_REQUEST_TIMEOUT_MS", 5000);

		log.infof("OtpConfig loaded: host=%s emailPurpose=%s smsPurpose=%s registrationPurpose=%s connectTimeoutMs=%d requestTimeoutMs=%d",
				otpHost, email.purpose(), sms.purpose(), registration.purpose(), connectTimeoutMs, requestTimeoutMs);
	}

	/** application.properties bundled in this jar — fallback source for config values. */
	private static final Properties FILE_PROPS = loadFileProps();

	private static Properties loadFileProps() {
		Properties p = new Properties();
		try (InputStream in = OtpConfig.class.getClassLoader()
				.getResourceAsStream("application.properties")) {
			if (in != null) p.load(in);
		} catch (IOException e) {
			log.warn("Could not read application.properties from classpath", e);
		}
		return p;
	}

	private static String getEnv(String name, String defaultValue) {
		String v = System.getenv(name);
		return (v != null && !v.isBlank()) ? v.trim() : defaultValue;
	}

	/** Resolution order: environment variable → application.properties → default. */
	private static String getEnvOrProp(String envName, String propName, String defaultValue) {
		String v = System.getenv(envName);
		if (v != null && !v.isBlank()) return v.trim();
		v = FILE_PROPS.getProperty(propName);
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
	 * @param purpose    value forwarded to the OTP service (e.g. "login"). OTP length
	 *                   and destination type are now decided by the OTP service itself.
	 * @param defaultOtp if set (via OTP_&lt;CHANNEL&gt;_DEFAULT_OTP), this value is
	 *                   accepted as a valid OTP without calling the verify service —
	 *                   a bypass for non-production environments. null/blank = disabled.
	 */
	public record ChannelConfig(String purpose, String defaultOtp) {
	}
}