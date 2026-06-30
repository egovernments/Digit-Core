package org.egov.keycloak.auth;

import org.egov.keycloak.auth.config.OtpConfig;
import org.egov.keycloak.auth.config.OtpConfig.ChannelConfig;

/**
 * Registers "OTP – SMS" in the Keycloak flow editor.
 *
 * Injects:
 *   channelConfig  → otpConfig.getSms()   (OTP_SMS_* env vars)
 *   destinationAttr → otpConfig.getSmsDestinationAttr()
 *                     (KEYCLOAK_SMS_DESTINATION_ATTRIBUTE, default: "mobileNumber")
 */
public class SmsOtpAuthenticatorFactory extends OtpAuthenticatorFactory {

	public static final String PROVIDER_ID = "otp-sms-authenticator";

	@Override public String getId()          { return PROVIDER_ID; }
	@Override public String getDisplayType() { return "OTP – SMS"; }
	@Override public String getHelpText() {
		return "Sends a one-time password to the user's mobile number via the OTP microservice.";
	}

	@Override
	protected ChannelConfig channelConfig(OtpConfig config) {
		return config.getSms();   // purpose
	}

	@Override
	protected String destinationAttr(OtpConfig config) {
		return config.getSmsDestinationAttr();  // default: "mobileNumber"
	}
}