package org.egov.keycloak.auth;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import lombok.extern.jbosslog.JBossLog;
import org.egov.keycloak.auth.clients.otp.OtpClient;
import org.egov.keycloak.auth.clients.otp.OtpClientException;
import org.egov.keycloak.auth.clients.otp.models.*;
import org.egov.keycloak.auth.config.OtpConfig.ChannelConfig;
import org.egov.keycloak.auth.config.OtpConstants;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.AuthenticationFlowException;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.services.messages.Messages;
import org.keycloak.sessions.AuthenticationSessionModel;

/**
 * Generic OTP authenticator.
 * <p>
 * This class is channel-agnostic. It does not know whether it is sending
 * an email or SMS. The channel is determined entirely by what the factory
 * injects at construction time:
 * <p>
 * - {@link ChannelConfig}  → OTP length, destinationType, purpose
 * - {@code destinationAttr} → which Keycloak user attribute to read
 * <p>
 * EmailOtpAuthenticatorFactory injects email channel config.
 * SmsOtpAuthenticatorFactory  injects SMS channel config.
 * The authenticator logic is identical for both.
 * <p>
 * Supports browser flow and direct grant (two-round-trip) flow.
 */
@JBossLog
public class OtpAuthenticator extends AbstractUsernameFormAuthenticator {

	private final OtpClient otpClient;
	private final ChannelConfig channelConfig;
	private final String destinationAttr;

	/**
	 * @param otpClient       HTTP client for the OTP microservice
	 * @param channelConfig   channel-specific settings from {@link org.egov.keycloak.auth.config.OtpConfig}
	 * @param destinationAttr Keycloak user attribute that holds the destination
	 *                        (e.g. "email" → user.getEmail(), "mobileNumber" → user attribute)
	 */
	public OtpAuthenticator(OtpClient otpClient, ChannelConfig channelConfig, String destinationAttr) {
		this.otpClient = otpClient;
		this.channelConfig = channelConfig;
		this.destinationAttr = destinationAttr;
	}

	// -----------------------------------------------------------------------
	// Entry point
	// -----------------------------------------------------------------------

	private static Response jsonError(Response.Status status, String error, String description) {
		return Response.status(status)
				.entity(String.format(
						"{\"error\":\"%s\",\"error_description\":\"%s\"}", error, description))
				.type("application/json")
				.build();
	}

	// -----------------------------------------------------------------------
	// Browser flow – form actions
	// -----------------------------------------------------------------------

	@Override
	public void authenticate(AuthenticationFlowContext context) {
		if (isDirectGrant(context)) {
			handleDirectGrant(context);
		} else {
			generateOtp(context);
			showForm(context, null, null);
		}
	}

	// -----------------------------------------------------------------------
	// Generate
	// -----------------------------------------------------------------------

	@Override
	public void action(AuthenticationFlowContext context) {
		if (!enabledUser(context, context.getUser())) return;

		MultivaluedMap<String, String> form =
				context.getHttpRequest().getDecodedFormParameters();

		if (form.containsKey(OtpConstants.PARAM_RESEND)) {
			handleResend(context);
		} else if (form.containsKey(OtpConstants.PARAM_CANCEL)) {
			handleCancel(context);
		} else {
			String otp = form.getFirst(OtpConstants.PARAM_OTP);
			if (otp == null || otp.isBlank()) {
				showForm(context, Messages.MISSING_TOTP, OtpConstants.PARAM_OTP);
			} else {
				verifyBrowserFlow(context, otp);
			}
		}
	}

	// -----------------------------------------------------------------------
	// Resend
	// -----------------------------------------------------------------------

	/**
	 * Calls /otp/v3/generate and stores referenceId in the auth session.
	 * Idempotent — skips if referenceId already present (browser reload).
	 */
	private void generateOtp(AuthenticationFlowContext context) {
		AuthenticationSessionModel session = context.getAuthenticationSession();
		if (session.getAuthNote(OtpConstants.SESSION_REFERENCE_ID) != null) {
			log.debug("OTP already generated for this session – skipping.");
			return;
		}

		try {
			GenerateOtpRequest req = GenerateOtpRequest.builder()
					.identifier(resolveDestination(context))            // raw email/phone; service infers type
					.purpose(channelConfig.purpose())                   // from injected channel
					.build();

			GenerateOtpResponse res = otpClient.generate(context.getRealm().getName(), req);

			session.setAuthNote(OtpConstants.SESSION_REFERENCE_ID, res.getReferenceId());
			session.setAuthNote(OtpConstants.SESSION_EXPIRES_AT, String.valueOf(res.getExpiresIn()));

			log.infof("OTP generated – referenceId=%s  purpose=%s  user=%s",
					res.getReferenceId(), channelConfig.purpose(),
					context.getUser().getUsername());

		} catch (OtpClientException e) {
			log.errorf(e, "OTP generate failed HTTP %d for user=%s",
					e.getStatusCode(), context.getUser().getUsername());
			throw new AuthenticationFlowException(AuthenticationFlowError.INTERNAL_ERROR);
		} catch (Exception e) {
			log.errorf(e, "OTP generate unexpected error for user=%s",
					context.getUser().getUsername());
			throw new AuthenticationFlowException(AuthenticationFlowError.INTERNAL_ERROR);
		}
	}

	// -----------------------------------------------------------------------
	// Cancel
	// -----------------------------------------------------------------------

	private void handleResend(AuthenticationFlowContext context) {
		String referenceId = getReferenceId(context);

		if (referenceId == null) {
			// Session lost — generate a fresh OTP
			clearSession(context);
			generateOtp(context);
			showForm(context, null, null);
			return;
		}

		try {
			otpClient.resend(context.getRealm().getName(),
					ResendOtpRequest.builder().referenceId(referenceId).build());

			log.infof("OTP resent – referenceId=%s  user=%s",
					referenceId, context.getUser().getUsername());

		} catch (OtpClientException e) {
			// 429 = too soon; surface a message but stay on the form
			log.warnf(e, "Resend failed HTTP %d for referenceId=%s", e.getStatusCode(), referenceId);
			showForm(context, "otpResendFailed", null);
			return;
		} catch (Exception e) {
			log.errorf(e, "Resend unexpected error for referenceId=%s", referenceId);
			showForm(context, "otpResendFailed", null);
			return;
		}

		showForm(context, null, null);
	}

	// -----------------------------------------------------------------------
	// Verify – browser flow
	// -----------------------------------------------------------------------

	private void handleCancel(AuthenticationFlowContext context) {
		String referenceId = getReferenceId(context);

		if (referenceId != null) {
			try {
				otpClient.invalidate(context.getRealm().getName(),
						InvalidateOtpRequest.builder()
								.referenceId(referenceId)
								.build());
				log.infof("OTP invalidated – referenceId=%s  user=%s",
						referenceId, context.getUser().getUsername());
			} catch (Exception e) {
				log.warnf(e, "Failed to invalidate OTP referenceId=%s – resetting flow anyway.", referenceId);
			}
		}

		clearSession(context);
		context.resetFlow();
	}

	// -----------------------------------------------------------------------
	// Direct grant – two-round-trip model
	//
	// Round 1: POST /token (no otp) → generate OTP → 400 otp_required
	// Round 2: POST /token (otp=X)  → verify       → 200 token or 400 error
	// -----------------------------------------------------------------------

	private void verifyBrowserFlow(AuthenticationFlowContext context, String otp) {
		if (isDefaultOtp(otp)) {
			clearSession(context);
			log.warnf("Default OTP bypass accepted (browser) – user=%s purpose=%s",
					context.getUser().getUsername(), channelConfig.purpose());
			context.success();
			return;
		}

		String referenceId = getReferenceId(context);

		if (referenceId == null) {
			// Auth session lost (e.g. idle timeout)
			clearSession(context);
			context.getEvent().user(context.getUser()).error(Errors.EXPIRED_CODE);
			Response cr = errorForm(context, Messages.EXPIRED_ACTION_TOKEN_SESSION_EXISTS,
					OtpConstants.PARAM_OTP);
			context.failureChallenge(AuthenticationFlowError.EXPIRED_CODE, cr);
			return;
		}

		try {
			VerifyOtpResponse res = otpClient.verify(context.getRealm().getName(),
					VerifyOtpRequest.builder()
							.referenceId(referenceId)
							.otp(otp)
							.purpose(channelConfig.purpose())
							.build());

			if (res.isVerified()) {
				clearSession(context);
				log.infof("OTP verified – referenceId=%s  user=%s",
						referenceId, context.getUser().getUsername());
				context.success();
			} else {
				// HTTP 200 without verified=true is unexpected — treat as a wrong code
				context.getEvent().user(context.getUser()).error(Errors.INVALID_USER_CREDENTIALS);
				Response cr = errorForm(context, Messages.INVALID_ACCESS_CODE, OtpConstants.PARAM_OTP);
				context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, cr);
			}

		} catch (OtpClientException e) {
			int sc = e.getStatusCode();
			if (sc == 410 || sc == 423) {
				// 410 expired, 423 locked — terminal, drop the session
				clearSession(context);
				context.getEvent().user(context.getUser()).error(Errors.EXPIRED_CODE);
				Response cr = errorForm(context,
						Messages.EXPIRED_ACTION_TOKEN_SESSION_EXISTS, OtpConstants.PARAM_OTP);
				context.failureChallenge(AuthenticationFlowError.EXPIRED_CODE, cr);
			} else if (sc == 400 || sc == 404 || sc == 422) {
				// 400 bad/malformed code, 404 not found, 422 wrong code — keep session so user can retry or resend
				context.getEvent().user(context.getUser()).error(Errors.INVALID_USER_CREDENTIALS);
				Response cr = errorForm(context, Messages.INVALID_ACCESS_CODE, OtpConstants.PARAM_OTP);
				context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, cr);
			} else {
				log.errorf(e, "Verify failed HTTP %d for referenceId=%s", sc, referenceId);
				Response cr = errorForm(context, Messages.UNEXPECTED_ERROR_HANDLING_REQUEST, null);
				context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR, cr);
			}
		} catch (Exception e) {
			log.errorf(e, "Verify unexpected error for referenceId=%s", referenceId);
			Response cr = errorForm(context, Messages.UNEXPECTED_ERROR_HANDLING_REQUEST, null);
			context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR, cr);
		}
	}

	// -----------------------------------------------------------------------
	// Destination resolution
	// -----------------------------------------------------------------------

	private void handleDirectGrant(AuthenticationFlowContext context) {
		MultivaluedMap<String, String> params =
				context.getHttpRequest().getDecodedFormParameters();

		String enteredOtp = params.getFirst(OtpConstants.PARAM_OTP);
		String referenceId = params.getFirst("referenceId");

		boolean hasOtp = enteredOtp != null && !enteredOtp.isBlank();
		boolean hasReferenceId = referenceId != null && !referenceId.isBlank();

		// -------------------------------
		// ROUND 1 → Generate OTP
		// -------------------------------
		if (!hasOtp) {

			generateOtp(context);

			String generatedReferenceId = getReferenceId(context);

			context.getEvent().user(context.getUser()).error(Errors.INVALID_USER_CREDENTIALS);

			context.failure(AuthenticationFlowError.INVALID_CREDENTIALS,
					Response.status(Response.Status.BAD_REQUEST)
							.entity(String.format(
									"{\"error\":\"otp_required\",\"referenceId\":\"%s\",\"error_description\":\"OTP sent. Retry with otp and referenceId\"}",
									generatedReferenceId))
							.type("application/json")
							.build());

			return;
		}

		// -------------------------------
		// Default OTP bypass (non-prod) — accept without verify / referenceId
		// -------------------------------
		if (isDefaultOtp(enteredOtp)) {
			log.warnf("Default OTP bypass accepted (direct grant) – user=%s purpose=%s",
					context.getUser().getUsername(), channelConfig.purpose());
			context.success();
			return;
		}

		// -------------------------------
		// ROUND 2 → Validate referenceId
		// -------------------------------
		if (!hasReferenceId) {
			context.failure(AuthenticationFlowError.INVALID_CREDENTIALS,
					jsonError(Response.Status.BAD_REQUEST,
							"invalid_request",
							"referenceId is required"));
			return;
		}

		// -------------------------------
		// VERIFY OTP
		// -------------------------------
		try {
			VerifyOtpResponse res = otpClient.verify(context.getRealm().getName(),
					VerifyOtpRequest.builder()
							.referenceId(referenceId)
							.otp(enteredOtp)
							.purpose(channelConfig.purpose())
							.build());

			if (res.isVerified()) {

				log.infof("OTP verified (direct grant) – referenceId=%s user=%s",
						referenceId, context.getUser().getUsername());

				context.success();

			} else {

				context.getEvent().user(context.getUser()).error(Errors.INVALID_USER_CREDENTIALS);

				context.failure(AuthenticationFlowError.INVALID_CREDENTIALS,
						jsonError(Response.Status.BAD_REQUEST,
								"invalid_otp", "Invalid OTP"));
			}

		} catch (OtpClientException e) {

			int sc = e.getStatusCode();
			if (sc == 410 || sc == 423) {

				context.getEvent().user(context.getUser()).error(Errors.EXPIRED_CODE);

				context.failure(AuthenticationFlowError.EXPIRED_CODE,
						jsonError(Response.Status.BAD_REQUEST,
								"otp_expired", "OTP expired"));

			} else if (sc == 400 || sc == 404 || sc == 422) {

				context.getEvent().user(context.getUser()).error(Errors.INVALID_USER_CREDENTIALS);

				context.failure(AuthenticationFlowError.INVALID_CREDENTIALS,
						jsonError(Response.Status.BAD_REQUEST,
								"invalid_otp", "Invalid OTP"));

			} else {

				log.errorf(e, "Direct grant verify failed HTTP %d for referenceId=%s",
						sc, referenceId);

				context.failure(AuthenticationFlowError.INTERNAL_ERROR,
						jsonError(Response.Status.INTERNAL_SERVER_ERROR,
								"server_error", "OTP verification failed"));
			}

		} catch (Exception e) {

			log.errorf(e, "Direct grant verify unexpected error for referenceId=%s", referenceId);

			context.failure(AuthenticationFlowError.INTERNAL_ERROR,
					jsonError(Response.Status.INTERNAL_SERVER_ERROR,
							"server_error", "OTP verification failed"));
		}
	}

	// -----------------------------------------------------------------------
	// Form helpers
	// -----------------------------------------------------------------------

	/**
	 * Reads the destination from the Keycloak user.
	 * <p>
	 * The {@code destinationAttr} injected by the factory decides the source:
	 * "email"        → user.getEmail()
	 * anything else  → user.getFirstAttribute(attrName)
	 */
	private String resolveDestination(AuthenticationFlowContext context) {
		UserModel user = context.getUser();
		String destination = OtpConstants.DEST_TYPE_EMAIL.equalsIgnoreCase(destinationAttr)
				? user.getEmail()
				: user.getFirstAttribute(destinationAttr);

		if (destination == null || destination.isBlank()) {
			log.warnf("User %s has no value for attribute '%s'",
					user.getUsername(), destinationAttr);
			throw new AuthenticationFlowException(AuthenticationFlowError.INVALID_USER);
		}
		return destination;
	}

	private void showForm(AuthenticationFlowContext context, String error, String field) {
		LoginFormsProvider form = context.form();
		if (error != null) {
			if (field != null) form.addError(new FormMessage(field, error));
			else form.setError(error);
		}
		context.challenge(form.createForm("otp-form.ftl"));
	}

	private Response errorForm(AuthenticationFlowContext context, String messageKey, String field) {
		LoginFormsProvider form = context.form();
		if (field != null) form.addError(new FormMessage(field, messageKey));
		else form.setError(messageKey);
		return form.createForm("otp-form.ftl");
	}

	@Override
	protected Response challenge(AuthenticationFlowContext context, String error, String field) {
		showForm(context, error, field);
		return null;
	}

	// -----------------------------------------------------------------------
	// SPI contract
	// -----------------------------------------------------------------------

	@Override
	protected String disabledByBruteForceError() {
		return Messages.INVALID_ACCESS_CODE;
	}

	@Override
	public boolean requiresUser() {
		return true;
	}

	@Override
	public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
		// Check that the user actually has a destination value for this channel
		String value = OtpConstants.DEST_TYPE_EMAIL.equalsIgnoreCase(destinationAttr)
				? user.getEmail()
				: user.getFirstAttribute(destinationAttr);
		return value != null && !value.isBlank();
	}

	@Override
	public void setRequiredActions(KeycloakSession s, RealmModel r, UserModel u) {
	}

	// -----------------------------------------------------------------------
	// Session helpers
	// -----------------------------------------------------------------------

	@Override
	public void close() {
	}

	/** True when a channel default OTP is configured and the entered value matches it. */
	private boolean isDefaultOtp(String otp) {
		String d = channelConfig.defaultOtp();
		return d != null && !d.isBlank() && d.equals(otp);
	}

	private String getReferenceId(AuthenticationFlowContext context) {
		return context.getAuthenticationSession()
				.getAuthNote(OtpConstants.SESSION_REFERENCE_ID);
	}

	private void clearSession(AuthenticationFlowContext context) {
		AuthenticationSessionModel s = context.getAuthenticationSession();
		s.removeAuthNote(OtpConstants.SESSION_REFERENCE_ID);
		s.removeAuthNote(OtpConstants.SESSION_EXPIRES_AT);
	}

	private boolean isDirectGrant(AuthenticationFlowContext context) {
		return "password".equals(context.getHttpRequest()
				.getDecodedFormParameters().getFirst("grant_type"));
	}
}