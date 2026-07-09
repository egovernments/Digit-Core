package org.egov.keycloak.registration;

import jakarta.ws.rs.core.MultivaluedMap;
import lombok.extern.jbosslog.JBossLog;
import org.egov.keycloak.auth.clients.otp.OtpClient;
import org.egov.keycloak.auth.clients.otp.OtpClientException;
import org.egov.keycloak.auth.clients.otp.models.GenerateOtpRequest;
import org.egov.keycloak.auth.clients.otp.models.GenerateOtpResponse;
import org.egov.keycloak.auth.config.OtpConstants;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.sessions.AuthenticationSessionModel;

import java.util.regex.Pattern;

/**
 * Step A of the mobile self-registration flow.
 * <p>
 * Renders a form asking for a mobile number only. On submit it validates the
 * number, checks it is not already registered (as username or as the
 * {@code mobileNumber} attribute), sends an OTP via the shared
 * {@link OtpClient}, stores the pending number + referenceId in the auth
 * session, and hands over to {@link MobileOtpVerifyAuthenticator}.
 * <p>
 * Runs before any user exists — {@link #requiresUser()} is false.
 */
@JBossLog
public class MobileRegistrationAuthenticator implements Authenticator {

	/**
	 * Permissive sanity check only: optional leading '+', 7–15 digits.
	 * Country-code normalization is intentionally NOT implemented yet —
	 * the expected format is still to be decided.
	 */
	private static final Pattern MOBILE_PATTERN = Pattern.compile("^\\+?\\d{7,15}$");

	private final OtpClient otpClient;
	private final String purpose;
	private final String mobileAttr;

	/**
	 * @param otpClient  shared HTTP client for the OTP microservice
	 * @param purpose    OTP purpose forwarded to the service (OTP_REGISTRATION_PURPOSE)
	 * @param mobileAttr Keycloak user attribute holding the mobile number
	 *                   (same attribute the SMS login authenticator reads)
	 */
	public MobileRegistrationAuthenticator(OtpClient otpClient, String purpose, String mobileAttr) {
		this.otpClient = otpClient;
		this.purpose = purpose;
		this.mobileAttr = mobileAttr;
	}

	@Override
	public void authenticate(AuthenticationFlowContext context) {
		String pending = context.getAuthenticationSession()
				.getAuthNote(RegistrationConstants.SESSION_REG_MOBILE);
		context.challenge(form(context, pending, null, null)
				.createForm(RegistrationConstants.TEMPLATE_MOBILE));
	}

	@Override
	public void action(AuthenticationFlowContext context) {
		MultivaluedMap<String, String> params =
				context.getHttpRequest().getDecodedFormParameters();

		String raw = params.getFirst(RegistrationConstants.PARAM_MOBILE);
		String mobile = raw == null ? "" : raw.replaceAll("[\\s-]", "");

		if (!MOBILE_PATTERN.matcher(mobile).matches()) {
			challengeWithError(context, mobile, "registerMobileInvalid");
			return;
		}

		if (isAlreadyRegistered(context, mobile)) {
			challengeWithError(context, mobile, "registerMobileExists");
			return;
		}

		AuthenticationSessionModel authSession = context.getAuthenticationSession();
		try {
			GenerateOtpRequest req = GenerateOtpRequest.builder()
					.identifier(mobile)
					.purpose(purpose)
					.build();

			GenerateOtpResponse res = otpClient.generate(context.getRealm().getName(), req);

			authSession.setAuthNote(RegistrationConstants.SESSION_REG_MOBILE, mobile);
			authSession.setAuthNote(OtpConstants.SESSION_REFERENCE_ID, res.getReferenceId());
			authSession.setAuthNote(OtpConstants.SESSION_EXPIRES_AT, String.valueOf(res.getExpiresIn()));

			log.infof("Registration OTP generated – referenceId=%s purpose=%s mobile=%s",
					res.getReferenceId(), purpose, mobile);

			context.success();

		} catch (OtpClientException e) {
			log.errorf(e, "Registration OTP generate failed HTTP %d for mobile=%s",
					e.getStatusCode(), mobile);
			challengeWithError(context, mobile, "otpSendFailed");
		} catch (Exception e) {
			log.errorf(e, "Registration OTP generate unexpected error for mobile=%s", mobile);
			challengeWithError(context, mobile, "otpSendFailed");
		}
	}

	/**
	 * A number is taken if it is already a username (mobile = username in this
	 * flow) or already stored as the mobile attribute on any user (covers
	 * accounts created through other channels).
	 */
	private boolean isAlreadyRegistered(AuthenticationFlowContext context, String mobile) {
		KeycloakSession session = context.getSession();
		RealmModel realm = context.getRealm();

		if (session.users().getUserByUsername(realm, mobile) != null) {
			return true;
		}
		return session.users()
				.searchForUserByUserAttributeStream(realm, mobileAttr, mobile)
				.findAny()
				.isPresent();
	}

	// -----------------------------------------------------------------------
	// Form helpers
	// -----------------------------------------------------------------------

	private void challengeWithError(AuthenticationFlowContext context, String mobile, String messageKey) {
		context.challenge(form(context, mobile, messageKey, RegistrationConstants.PARAM_MOBILE)
				.createForm(RegistrationConstants.TEMPLATE_MOBILE));
	}

	private LoginFormsProvider form(AuthenticationFlowContext context,
	                                String mobile, String error, String field) {
		LoginFormsProvider form = context.form();
		if (mobile != null && !mobile.isBlank()) {
			form.setAttribute(RegistrationConstants.ATTR_MOBILE, mobile);
		}
		if (error != null) {
			if (field != null) form.addError(new FormMessage(field, error));
			else form.setError(error);
		}
		return form;
	}

	// -----------------------------------------------------------------------
	// SPI contract
	// -----------------------------------------------------------------------

	@Override
	public boolean requiresUser() {
		return false;
	}

	@Override
	public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
		return true;
	}

	@Override
	public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
	}

	@Override
	public void close() {
	}
}
