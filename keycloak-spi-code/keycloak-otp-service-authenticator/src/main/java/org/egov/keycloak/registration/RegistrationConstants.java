package org.egov.keycloak.registration;

import lombok.experimental.UtilityClass;

/**
 * Compile-time string constants for the mobile self-registration flow.
 *
 * OTP-related session-note keys are shared with the login flow via
 * {@link org.egov.keycloak.auth.config.OtpConstants} — registration and login
 * never share an auth session, so there is no risk of collision.
 */
@UtilityClass
public class RegistrationConstants {

	// Auth-session note keys
	public static final String SESSION_REG_MOBILE = "regMobileNumber";

	// Form / request parameter names
	public static final String PARAM_MOBILE = "mobileNumber";

	// FTL templates (shipped in this jar under theme-resources/templates)
	public static final String TEMPLATE_MOBILE = "register-mobile.ftl";
	public static final String TEMPLATE_OTP = "register-mobile-otp.ftl";

	// Attributes passed to the templates
	public static final String ATTR_MOBILE = "mobileNumber";
	public static final String ATTR_MASKED_MOBILE = "maskedMobile";
}
