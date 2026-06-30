package org.egov.keycloak.auth.config;

import lombok.experimental.UtilityClass;

/**
 * Compile-time string constants.
 *
 * No env reads. No defaults. Just identifiers.
 */
@UtilityClass
public class OtpConstants {

	// Auth-session note keys
	public static final String SESSION_REFERENCE_ID         = "otpReferenceId";
	public static final String SESSION_RESEND_ALLOWED_AFTER = "otpResendAllowedAfter";
	public static final String SESSION_EXPIRES_AT           = "otpExpiresAt";

	// Authenticator config-property keys (Keycloak Admin UI)
	public static final String CFG_OTP_LENGTH       = "otpLength";
	public static final String CFG_OTP_PURPOSE      = "otpPurpose";
	public static final String CFG_DESTINATION_TYPE = "destinationType";
	public static final String CFG_DESTINATION_ATTR = "destinationAttribute";

	// Destination-type tokens
	public static final String DEST_TYPE_EMAIL = "email";
	public static final String DEST_TYPE_PHONE = "phone";

	// Form / request parameter names
	public static final String PARAM_OTP    = "otp";
	public static final String PARAM_RESEND = "resend";
	public static final String PARAM_CANCEL = "cancel";
}