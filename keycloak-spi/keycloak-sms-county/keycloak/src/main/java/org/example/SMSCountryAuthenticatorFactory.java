package org.example;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.List;

public class SMSCountryAuthenticatorFactory implements AuthenticatorFactory {

    // The ID for this authenticator
    private static final String PROVIDER_ID = "sms-country-authenticator";

    // Display name for the authenticator in the admin console
    private static final String DISPLAY_NAME = "SMS Country OTP Authenticator";

    @Override
    public Authenticator create(KeycloakSession session) {
        // Return an instance of the SMSCountryAuthenticator
        return new SMSCountryAuthenticator();
    }

    @Override
    public void init(Config.Scope scope) {
        System.out.println("Initializing SMSCountryAuthenticatorFactory...");
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // Perform post-initialization logic here if needed
        System.out.println("Post-initializing SMSCountryAuthenticatorFactory...");
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        // Return the supported requirement choices
        return REQUIREMENT_CHOICES;
    }

    @Override
    public boolean isUserSetupAllowed() {
        // Return false if no user-specific setup is required for this authenticator
        return false;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayType() {
        return DISPLAY_NAME;
    }

    @Override
    public boolean isConfigurable() {
        // This can be set to true if the authenticator requires configuration
        return false;
    }

    @Override
    public String getHelpText() {
        // Help text displayed in the admin console
        return "Validates OTP sent via SMS Country.";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        // Return an empty list for now. Add configuration properties if needed.
        return List.of();
    }

    @Override
    public String getReferenceCategory() {
        // Return a reference category for the authenticator
        return "otp";
    }

    @Override
    public void close() {
        // Cleanup resources if required
    }
}
