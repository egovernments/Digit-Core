package org.egov.keycloak.auth;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.services.messages.Messages;

/**
 * Flow-scoped replacement for the built-in Username Form: renders a
 * "Mobile number" field (own template + message keys, so the standard
 * "Username" label elsewhere is untouched), resolves the user by username
 * (mobile = username for self-registered users) or by the mobile attribute,
 * and hands over to the OTP – SMS step. No password, no OTP logic here.
 */
@JBossLog
public class MobileLoginAuthenticator implements Authenticator {

	private static final String TEMPLATE = "login-mobile.ftl";
	private static final String PARAM_MOBILE = "mobileNumber";

	private final String mobileAttr;

	public MobileLoginAuthenticator(String mobileAttr) {
		this.mobileAttr = mobileAttr;
	}

	@Override
	public void authenticate(AuthenticationFlowContext context) {
		context.challenge(form(context, null, null, null));
	}

	@Override
	public void action(AuthenticationFlowContext context) {
		MultivaluedMap<String, String> params =
				context.getHttpRequest().getDecodedFormParameters();

		String raw = params.getFirst(PARAM_MOBILE);
		String mobile = raw == null ? "" : raw.replaceAll("[\\s-]", "");

		if (mobile.isBlank()) {
			context.failureChallenge(AuthenticationFlowError.INVALID_USER,
					form(context, mobile, Messages.MISSING_USERNAME, PARAM_MOBILE));
			return;
		}

		UserModel user = resolveUser(context, mobile);

		if (user == null) {
			context.getEvent().error(Errors.USER_NOT_FOUND);
			context.failureChallenge(AuthenticationFlowError.INVALID_USER,
					form(context, mobile, "mobileLoginInvalid", PARAM_MOBILE));
			return;
		}

		if (!user.isEnabled()) {
			context.getEvent().user(user).error(Errors.USER_DISABLED);
			context.failureChallenge(AuthenticationFlowError.USER_DISABLED,
					form(context, mobile, Messages.ACCOUNT_DISABLED, null));
			return;
		}

		context.setUser(user);
		context.success();
	}

	/** Lookup by username first (mobile = username), then by the mobile attribute. */
	private UserModel resolveUser(AuthenticationFlowContext context, String mobile) {
		KeycloakSession session = context.getSession();
		RealmModel realm = context.getRealm();

		UserModel user = session.users().getUserByUsername(realm, mobile);
		if (user != null) return user;

		return session.users()
				.searchForUserByUserAttributeStream(realm, mobileAttr, mobile)
				.findFirst()
				.orElse(null);
	}

	private Response form(AuthenticationFlowContext context,
	                      String mobile, String error, String field) {
		LoginFormsProvider form = context.form();
		if (mobile != null && !mobile.isBlank()) {
			form.setAttribute(PARAM_MOBILE, mobile);
		}
		if (error != null) {
			if (field != null) form.addError(new FormMessage(field, error));
			else form.setError(error);
		}
		return form.createForm(TEMPLATE);
	}

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
