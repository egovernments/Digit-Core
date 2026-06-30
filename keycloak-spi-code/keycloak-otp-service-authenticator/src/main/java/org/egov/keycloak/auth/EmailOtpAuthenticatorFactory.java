package org.egov.keycloak.auth;

import org.egov.keycloak.auth.config.OtpConfig;
import org.egov.keycloak.auth.config.OtpConfig.ChannelConfig;

/**
 * Registers "OTP – Email" in the Keycloak flow editor.
 *
 * Injects:
 *   channelConfig  → otpConfig.getEmail()   (OTP_EMAIL_* env vars)
 *   destinationAttr → otpConfig.getEmailDestinationAttr()
 *                     (KEYCLOAK_EMAIL_DESTINATION_ATTRIBUTE, default: "email")
 */
public class EmailOtpAuthenticatorFactory extends OtpAuthenticatorFactory {

	public static final String PROVIDER_ID = "otp-email-authenticator";

	@Override public String getId()          { return PROVIDER_ID; }
	@Override public String getDisplayType() { return "OTP – Email"; }
	@Override public String getHelpText() {
		return "Sends a one-time password to the user's e-mail address via the OTP microservice.";
	}

	@Override
	protected ChannelConfig channelConfig(OtpConfig config) {
		return config.getEmail();   // purpose
	}

	@Override
	protected String destinationAttr(OtpConfig config) {
		return config.getEmailDestinationAttr();  // default: "email" → user.getEmail()
	}
}