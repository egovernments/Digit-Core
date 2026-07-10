package org.egov.keycloak.registration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.keycloak.auth.clients.otp.OtpClient;
import org.egov.keycloak.auth.clients.otp.OtpClientImpl;
import org.egov.keycloak.auth.config.OtpConfig;
import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

/**
 * Registers "Registration – Mobile OTP Verify" in the Keycloak flow editor.
 * <p>
 * Step B of the mobile self-registration flow: verifies the OTP sent by
 * Step A and creates the passwordless user (username = mobile number).
 * Must be placed directly after "Registration – Mobile Number".
 */
public class MobileOtpVerifyAuthenticatorFactory implements AuthenticatorFactory {

	public static final String PROVIDER_ID = "registration-mobile-otp-verify";

	private MobileOtpVerifyAuthenticator authenticator;

	@Override
	public String getId() {
		return PROVIDER_ID;
	}

	@Override
	public String getDisplayType() {
		return "Registration – Mobile OTP Verify";
	}

	@Override
	public String getHelpText() {
		return "Verifies the OTP sent to the mobile number and creates a passwordless user (username = mobile number).";
	}

	@Override
	public void init(Config.Scope scope) {
		OtpConfig config = new OtpConfig();
		HttpClient hc = HttpClient.newBuilder()
				.connectTimeout(Duration.ofMillis(config.getConnectTimeoutMs()))
				.build();
		OtpClient client = new OtpClientImpl(config, hc, new ObjectMapper());

		this.authenticator = new MobileOtpVerifyAuthenticator(
				client,
				config.getRegistration().purpose(),
				config.getSmsDestinationAttr(),
				config.getRegistrationRoles(),
				config.getRegistration().defaultOtp()
		);
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
