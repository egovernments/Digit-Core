package org.egov.keycloak.auth.email;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.AuthenticationFlowException;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.common.util.SecretGenerator;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.*;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.services.messages.Messages;
import org.keycloak.sessions.AuthenticationSessionModel;

import java.util.List;
import java.util.Map;

@JBossLog
public class EmailAuthenticatorSyncForm extends AbstractUsernameFormAuthenticator {

	@Override
	public void authenticate(AuthenticationFlowContext context) {
		challenge(context, null);
	}

	@Override
	protected Response challenge(AuthenticationFlowContext context, String error, String field) {
		generateAndSendEmailCode(context);

		LoginFormsProvider form = context.form().setExecution(context.getExecution().getId());
		if (error != null) {
			if (field != null) {
				form.addError(new FormMessage(field, error));
			} else {
				form.setError(error);
			}
		}
		Response response = form.createForm("email-code-form.ftl");
		context.challenge(response);
		return response;
	}

	private void generateAndSendEmailCode(AuthenticationFlowContext context) {
		AuthenticatorConfigModel config = context.getAuthenticatorConfig();
		AuthenticationSessionModel session = context.getAuthenticationSession();

		if (session.getAuthNote(EmailSyncConstants.CODE) != null) {
			// skip sending email code
			return;
		}

		int length = EmailSyncConstants.DEFAULT_LENGTH;
		int ttl = EmailSyncConstants.DEFAULT_TTL;
		if (config != null) {
			// get config values
			length = Integer.parseInt(config.getConfig().get(EmailSyncConstants.CODE_LENGTH));
			ttl = Integer.parseInt(config.getConfig().get(EmailSyncConstants.CODE_TTL));
		}

		String code = SecretGenerator.getInstance().randomString(length, SecretGenerator.DIGITS);
		sendEmailWithCode(context.getSession(), context.getRealm(), context.getUser(), code, ttl);
		session.setAuthNote(EmailSyncConstants.CODE, code);
		session.setAuthNote(EmailSyncConstants.CODE_TTL, Long.toString(System.currentTimeMillis() + (ttl * 1000L)));
	}

	@Override
	public void action(AuthenticationFlowContext context) {
		UserModel userModel = context.getUser();
		if (!enabledUser(context, userModel)) {
			// error in context is set in enabledUser/isDisabledByBruteForce
			return;
		}

		MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
		if (formData.containsKey("resend")) {
			resetEmailCode(context);
			challenge(context, null);
			return;
		}

		if (formData.containsKey("cancel")) {
			resetEmailCode(context);
			context.resetFlow();
			return;
		}

		AuthenticationSessionModel session = context.getAuthenticationSession();
		String code = session.getAuthNote(EmailSyncConstants.CODE);
		String ttl = session.getAuthNote(EmailSyncConstants.CODE_TTL);
		String enteredCode = formData.getFirst(EmailSyncConstants.CODE);

		if (enteredCode.equals(code)) {
			if (Long.parseLong(ttl) < System.currentTimeMillis()) {
				// expired
				context.getEvent().user(userModel).error(Errors.EXPIRED_CODE);
				Response challengeResponse = challenge(context, Messages.EXPIRED_ACTION_TOKEN_SESSION_EXISTS, EmailSyncConstants.CODE);
				context.failureChallenge(AuthenticationFlowError.EXPIRED_CODE, challengeResponse);
			} else {
				// valid
				resetEmailCode(context);
				context.success();
			}
		} else {
			// invalid
			AuthenticationExecutionModel execution = context.getExecution();
			if (execution.isRequired()) {
				context.getEvent().user(userModel).error(Errors.INVALID_USER_CREDENTIALS);
				Response challengeResponse = challenge(context, Messages.INVALID_ACCESS_CODE, EmailSyncConstants.CODE);
				context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challengeResponse);
			} else if (execution.isConditional() || execution.isAlternative()) {
				context.attempted();
			}
		}
	}

	@Override
	protected String disabledByBruteForceError() {
		return Messages.INVALID_ACCESS_CODE;
	}

	private void resetEmailCode(AuthenticationFlowContext context) {
		context.getAuthenticationSession().removeAuthNote(EmailSyncConstants.CODE);
	}

	@Override
	public boolean requiresUser() {
		return true;
	}

	@Override
	public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
		return user.getEmail() != null;
	}

	@Override
	public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
		// NOOP
	}

	@Override
	public void close() {
		// NOOP
	}

	private void sendEmailWithCode(KeycloakSession session, RealmModel realm, UserModel user, String code, int ttl) {
		if (user.getEmail() == null) {
			log.warnf("Could not send access code email due to missing email. realm=%s user=%s", realm.getId(), user.getUsername());
			throw new AuthenticationFlowException(AuthenticationFlowError.INVALID_USER);
		}
		log.info("Running in simulation mode. OTP is : " + code);

		EmailRequest emailRequest = EmailRequest.builder()
				.templateId(EmailSyncConstants.getEnv("NOTIFICATION_EMAIL_TEMPLATEID", "sandbox-email-login-otp"))
				.version(EmailSyncConstants.getEnv("NOTIFICATION_EMAIL_VERSION", "1.0.0"))
				.tenantId(realm.getName())
				.emailIds(List.of(user.getEmail()))
				.enrich(EmailSyncConstants.getEnvBool("NOTIFICATION_EMAIL_ENRICH", false))
				.payload(Map.of(
						"otp", code,
						"baseUrl", session.getContext().getUri().getBaseUri().toString(),
						"tenantId", realm.getName()
				))
				.attachments(null)
				.build();

		try {
			EmailSyncUtil.sendEmail(emailRequest);
		} catch (Exception e) {
			log.errorf(e, "Failed to send access code email. realm=%s user=%s", realm.getId(), user.getUsername());
			// signal Keycloak that authentication failed due to internal error
			throw new AuthenticationFlowException(AuthenticationFlowError.INTERNAL_ERROR);
		}
	}
}
