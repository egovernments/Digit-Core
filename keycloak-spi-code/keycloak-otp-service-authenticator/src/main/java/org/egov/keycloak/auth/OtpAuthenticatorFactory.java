package org.egov.keycloak.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.keycloak.auth.clients.otp.OtpClient;
import org.egov.keycloak.auth.clients.otp.OtpClientImpl;
import org.egov.keycloak.auth.config.OtpConfig;
import org.egov.keycloak.auth.config.OtpConfig.ChannelConfig;
import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.credential.OTPCredentialModel;
import org.keycloak.provider.ProviderConfigProperty;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

/**
 * Abstract base factory.
 * <p>
 * Responsibilities:
 * ✔ Build OtpConfig and OtpClient once in init() — not per request
 * ✔ Expose config properties for Keycloak Admin UI
 * ✔ Delegate channel selection to concrete subclass
 * <p>
 * Concrete subclasses tell the base which channel to use by implementing:
 * - channelConfig(OtpConfig)  → returns config.getEmail() or config.getSms()
 * - destinationAttr(OtpConfig) → returns the right Keycloak user attribute
 */
public abstract class OtpAuthenticatorFactory implements AuthenticatorFactory {

	private OtpAuthenticator authenticator;

	// -----------------------------------------------------------------------
	// Abstract contract — implemented by Email/SMS subclasses
	// -----------------------------------------------------------------------

	@Override
	public abstract String getId();

	@Override
	public abstract String getDisplayType();

	@Override
	public abstract String getHelpText();

	/**
	 * Returns the channel-specific OTP settings from the fully loaded config.
	 */
	protected abstract ChannelConfig channelConfig(OtpConfig config);

	/**
	 * Returns the Keycloak user attribute name for this channel's destination.
	 */
	protected abstract String destinationAttr(OtpConfig config);

	// -----------------------------------------------------------------------
	// Lifecycle — wiring happens ONCE at server startup
	// -----------------------------------------------------------------------

	@Override
	public void init(Config.Scope scope) {
		OtpConfig config = new OtpConfig();
		HttpClient hc = HttpClient.newBuilder()
				.connectTimeout(Duration.ofMillis(config.getConnectTimeoutMs()))
				.build();
		OtpClient client = new OtpClientImpl(config, hc, new ObjectMapper());

		// Each factory instantiates the same authenticator class but injects
		// its own channel config — email factory gets config.getEmail(),
		// SMS factory gets config.getSms().
		this.authenticator = new OtpAuthenticator(
				client,
				channelConfig(config),
				destinationAttr(config)
		);
	}

	@Override
	public Authenticator create(KeycloakSession session) {
		// Authenticator is stateless — safe to return the same instance
		return authenticator;
	}

	// -----------------------------------------------------------------------
	// Shared SPI metadata
	// -----------------------------------------------------------------------

	@Override
	public String getReferenceCategory() {
		return OTPCredentialModel.TYPE;
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
		return REQUIREMENT_CHOICES;
	}

	/**
	 * No Admin UI config properties — channel settings are fully env-driven
	 * via {@link OtpConfig}. The factory already knows its channel from the
	 * concrete subclass; there is nothing left for an admin to configure.
	 */
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