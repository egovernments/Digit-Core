package org.egov.keycloak.registration;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import lombok.extern.jbosslog.JBossLog;
import org.egov.keycloak.auth.clients.otp.OtpClient;
import org.egov.keycloak.auth.clients.otp.OtpClientException;
import org.egov.keycloak.auth.clients.otp.models.*;
import org.egov.keycloak.auth.config.OtpConstants;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;

import java.util.List;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.services.messages.Messages;
import org.keycloak.sessions.AuthenticationSessionModel;

/**
 * Step B of the mobile self-registration flow.
 * <p>
 * Prompts for the OTP sent by {@link MobileRegistrationAuthenticator},
 * verifies it via the shared {@link OtpClient}, and on success creates the
 * Keycloak user:
 * <ul>
 *   <li>username = mobile number</li>
 *   <li>{@code mobileNumber} attribute = same value</li>
 *   <li>enabled = true, emailVerified = false</li>
 *   <li>NO password credential — the account is OTP-only</li>
 *   <li>realm default roles/groups apply automatically via addUser()</li>
 * </ul>
 * Supports resend (cooldown enforced by the OTP service — a 429 keeps the
 * user on the form with a message, same as the login flow) and cancel
 * (invalidates the OTP and restarts the flow at the mobile-number step).
 */
@JBossLog
public class MobileOtpVerifyAuthenticator implements Authenticator {

	private final OtpClient otpClient;
	private final String purpose;
	private final String mobileAttr;
	private final List<String> rolesToGrant;

	public MobileOtpVerifyAuthenticator(OtpClient otpClient, String purpose, String mobileAttr,
	                                    List<String> rolesToGrant) {
		this.otpClient = otpClient;
		this.purpose = purpose;
		this.mobileAttr = mobileAttr;
		this.rolesToGrant = rolesToGrant;
	}

	@Override
	public void authenticate(AuthenticationFlowContext context) {
		String mobile = pendingMobile(context);
		if (mobile == null) {
			// Flow is mis-wired (this step must run after the mobile-number step)
			log.error("No pending mobile number in auth session – is '"
					+ MobileRegistrationAuthenticatorFactory.PROVIDER_ID
					+ "' configured before this execution?");
			context.failure(AuthenticationFlowError.INTERNAL_ERROR);
			return;
		}

		// Normally Step A has already generated the OTP; regenerate only if the
		// referenceId was dropped (e.g. after a terminal expiry).
		if (referenceId(context) == null && !generateOtp(context, mobile)) {
			showForm(context, "otpSendFailed", null);
			return;
		}
		showForm(context, null, null);
	}

	@Override
	public void action(AuthenticationFlowContext context) {
		String mobile = pendingMobile(context);
		if (mobile == null) {
			// Auth session lost — start over at the mobile-number step
			context.resetFlow();
			return;
		}

		MultivaluedMap<String, String> form =
				context.getHttpRequest().getDecodedFormParameters();

		if (form.containsKey(OtpConstants.PARAM_RESEND)) {
			handleResend(context, mobile);
		} else if (form.containsKey(OtpConstants.PARAM_CANCEL)) {
			handleCancel(context);
		} else {
			String otp = form.getFirst(OtpConstants.PARAM_OTP);
			if (otp == null || otp.isBlank()) {
				showForm(context, Messages.MISSING_TOTP, OtpConstants.PARAM_OTP);
			} else {
				verify(context, mobile, otp);
			}
		}
	}

	// -----------------------------------------------------------------------
	// Generate / resend / cancel — same semantics as the login OtpAuthenticator
	// -----------------------------------------------------------------------

	private boolean generateOtp(AuthenticationFlowContext context, String mobile) {
		try {
			GenerateOtpResponse res = otpClient.generate(context.getRealm().getName(),
					GenerateOtpRequest.builder()
							.identifier(mobile)
							.purpose(purpose)
							.build());

			AuthenticationSessionModel session = context.getAuthenticationSession();
			session.setAuthNote(OtpConstants.SESSION_REFERENCE_ID, res.getReferenceId());
			session.setAuthNote(OtpConstants.SESSION_EXPIRES_AT, String.valueOf(res.getExpiresIn()));

			log.infof("Registration OTP generated – referenceId=%s purpose=%s mobile=%s",
					res.getReferenceId(), purpose, mobile);
			return true;
		} catch (Exception e) {
			log.errorf(e, "Registration OTP generate failed for mobile=%s", mobile);
			return false;
		}
	}

	private void handleResend(AuthenticationFlowContext context, String mobile) {
		String referenceId = referenceId(context);

		if (referenceId == null) {
			// Previous OTP was invalidated/expired — generate a fresh one
			if (generateOtp(context, mobile)) {
				showForm(context, null, null);
			} else {
				showForm(context, "otpResendFailed", null);
			}
			return;
		}

		try {
			otpClient.resend(context.getRealm().getName(),
					ResendOtpRequest.builder().referenceId(referenceId).build());
			log.infof("Registration OTP resent – referenceId=%s mobile=%s", referenceId, mobile);
			showForm(context, null, null);
		} catch (OtpClientException e) {
			// 429 = cooldown not elapsed; surface a message but stay on the form
			log.warnf(e, "Registration resend failed HTTP %d for referenceId=%s",
					e.getStatusCode(), referenceId);
			showForm(context, "otpResendFailed", null);
		} catch (Exception e) {
			log.errorf(e, "Registration resend unexpected error for referenceId=%s", referenceId);
			showForm(context, "otpResendFailed", null);
		}
	}

	private void handleCancel(AuthenticationFlowContext context) {
		String referenceId = referenceId(context);
		if (referenceId != null) {
			try {
				otpClient.invalidate(context.getRealm().getName(),
						InvalidateOtpRequest.builder().referenceId(referenceId).build());
				log.infof("Registration OTP invalidated – referenceId=%s", referenceId);
			} catch (Exception e) {
				log.warnf(e, "Failed to invalidate OTP referenceId=%s – resetting flow anyway.", referenceId);
			}
		}
		clearOtpNotes(context);
		context.getAuthenticationSession().removeAuthNote(RegistrationConstants.SESSION_REG_MOBILE);
		context.resetFlow();
	}

	// -----------------------------------------------------------------------
	// Verify + user creation
	// -----------------------------------------------------------------------

	private void verify(AuthenticationFlowContext context, String mobile, String otp) {
		String referenceId = referenceId(context);

		if (referenceId == null) {
			context.getEvent().error(Errors.EXPIRED_CODE);
			Response cr = errorForm(context, "registerOtpExpired", OtpConstants.PARAM_OTP);
			context.failureChallenge(AuthenticationFlowError.EXPIRED_CODE, cr);
			return;
		}

		try {
			VerifyOtpResponse res = otpClient.verify(context.getRealm().getName(),
					VerifyOtpRequest.builder()
							.referenceId(referenceId)
							.otp(otp)
							.purpose(purpose)
							.build());

			if (res.isVerified()) {
				log.infof("Registration OTP verified – referenceId=%s mobile=%s", referenceId, mobile);
				createUser(context, mobile);
			} else {
				// HTTP 200 without verified=true is unexpected — treat as a wrong code
				context.getEvent().error(Errors.INVALID_USER_CREDENTIALS);
				Response cr = errorForm(context, Messages.INVALID_ACCESS_CODE, OtpConstants.PARAM_OTP);
				context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, cr);
			}

		} catch (OtpClientException e) {
			int sc = e.getStatusCode();
			if (sc == 410 || sc == 423) {
				// 410 expired, 423 locked — drop the referenceId; "Resend Code" issues a fresh OTP
				clearOtpNotes(context);
				context.getEvent().error(Errors.EXPIRED_CODE);
				Response cr = errorForm(context, "registerOtpExpired", OtpConstants.PARAM_OTP);
				context.failureChallenge(AuthenticationFlowError.EXPIRED_CODE, cr);
			} else if (sc == 400 || sc == 404 || sc == 422) {
				// wrong/unknown code — keep session so the user can retry or resend
				context.getEvent().error(Errors.INVALID_USER_CREDENTIALS);
				Response cr = errorForm(context, Messages.INVALID_ACCESS_CODE, OtpConstants.PARAM_OTP);
				context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, cr);
			} else {
				log.errorf(e, "Registration verify failed HTTP %d for referenceId=%s", sc, referenceId);
				Response cr = errorForm(context, Messages.UNEXPECTED_ERROR_HANDLING_REQUEST, null);
				context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR, cr);
			}
		} catch (Exception e) {
			log.errorf(e, "Registration verify unexpected error for referenceId=%s", referenceId);
			Response cr = errorForm(context, Messages.UNEXPECTED_ERROR_HANDLING_REQUEST, null);
			context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR, cr);
		}
	}

	private void createUser(AuthenticationFlowContext context, String mobile) {
		try {
			// addUser(realm, username) grants realm default roles/groups and
			// default required actions automatically
			UserModel user = context.getSession().users().addUser(context.getRealm(), mobile);
			user.setEnabled(true);
			user.setEmailVerified(false);
			user.setSingleAttribute(mobileAttr, mobile);
			// Intentionally NO password credential — this account is OTP-only

			// Grant configured realm roles (OTP_REGISTRATION_ROLES, default: CITIZEN)
			for (String roleName : rolesToGrant) {
				RoleModel role = context.getRealm().getRole(roleName);
				if (role != null) {
					user.grantRole(role);
				} else {
					log.warnf("Realm role '%s' not found in realm '%s' – not granted to user %s",
							roleName, context.getRealm().getName(), mobile);
				}
			}

			context.setUser(user);
			context.getEvent()
					.user(user)
					.detail(Details.USERNAME, mobile)
					.detail(Details.REGISTER_METHOD, "form");

			clearOtpNotes(context);
			context.getAuthenticationSession().removeAuthNote(RegistrationConstants.SESSION_REG_MOBILE);

			log.infof("User registered via mobile OTP – username=%s", mobile);
			context.success();

		} catch (ModelDuplicateException e) {
			// Registered concurrently between the mobile step and now
			context.getEvent().error(Errors.USERNAME_IN_USE);
			Response cr = errorForm(context, "registerMobileExists", null);
			context.failureChallenge(AuthenticationFlowError.USER_CONFLICT, cr);
		}
	}

	// -----------------------------------------------------------------------
	// Form helpers
	// -----------------------------------------------------------------------

	private void showForm(AuthenticationFlowContext context, String error, String field) {
		context.challenge(buildForm(context, error, field));
	}

	private Response errorForm(AuthenticationFlowContext context, String messageKey, String field) {
		return buildForm(context, messageKey, field);
	}

	private Response buildForm(AuthenticationFlowContext context, String error, String field) {
		LoginFormsProvider form = context.form()
				.setAttribute(RegistrationConstants.ATTR_MASKED_MOBILE,
						mask(pendingMobile(context)));
		if (error != null) {
			if (field != null) form.addError(new FormMessage(field, error));
			else form.setError(error);
		}
		return form.createForm(RegistrationConstants.TEMPLATE_OTP);
	}

	private static String mask(String mobile) {
		if (mobile == null || mobile.length() <= 3) return mobile == null ? "" : mobile;
		int visible = 3;
		return "*".repeat(mobile.length() - visible)
				+ mobile.substring(mobile.length() - visible);
	}

	// -----------------------------------------------------------------------
	// Session helpers
	// -----------------------------------------------------------------------

	private String pendingMobile(AuthenticationFlowContext context) {
		return context.getAuthenticationSession()
				.getAuthNote(RegistrationConstants.SESSION_REG_MOBILE);
	}

	private String referenceId(AuthenticationFlowContext context) {
		return context.getAuthenticationSession()
				.getAuthNote(OtpConstants.SESSION_REFERENCE_ID);
	}

	private void clearOtpNotes(AuthenticationFlowContext context) {
		AuthenticationSessionModel s = context.getAuthenticationSession();
		s.removeAuthNote(OtpConstants.SESSION_REFERENCE_ID);
		s.removeAuthNote(OtpConstants.SESSION_EXPIRES_AT);
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
