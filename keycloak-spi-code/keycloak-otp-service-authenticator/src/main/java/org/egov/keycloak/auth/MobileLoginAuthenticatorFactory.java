package org.egov.keycloak.auth;

import org.egov.keycloak.auth.config.OtpConfig;
import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.List;

/**
 * Registers "Login – Mobile Number" in the Keycloak flow editor.
 * <p>
 * A username-form replacement that asks for a mobile number instead.
 * Use it as the identification step in a passwordless browser flow,
 * directly before "OTP – SMS".
 */
public class MobileLoginAuthenticatorFactory implements AuthenticatorFactory {

	public static final String PROVIDER_ID = "login-mobile-number";

	private MobileLoginAuthenticator authenticator;

	@Override
	public String getId() {
		return PROVIDER_ID;
	}

	@Override
	public String getDisplayType() {
		return "Login – Mobile Number";
	}

	@Override
	public String getHelpText() {
		return "Identifies the user by mobile number (username or mobile attribute). Renders a mobile-number field instead of the standard username form.";
	}

	@Override
	public void init(Config.Scope scope) {
		OtpConfig config = new OtpConfig();
		this.authenticator = new MobileLoginAuthenticator(config.getSmsDestinationAttr());
	}

	@Override
	public Authenticator create(KeycloakSession session) {
		// Authenticator is stateless — safe to return the same instance
		return authenticator;
	}

	@Override
	public String getReferenceCategory() {
		return null;
	}

	@Override
	public boolean isConfigurable() {
		return false;
	}

	@Override
	public boolean isUserSetupAllowed() {
		return false;
	}

	@Override
	public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
		return new AuthenticationExecutionModel.Requirement[]{
				AuthenticationExecutionModel.Requirement.REQUIRED
		};
	}

	@Override
	public List<ProviderConfigProperty> getConfigProperties() {
		return List.of();
	}

	@Override
	public void postInit(KeycloakSessionFactory factory) {
	}

	@Override
	public void close() {
	}
}
