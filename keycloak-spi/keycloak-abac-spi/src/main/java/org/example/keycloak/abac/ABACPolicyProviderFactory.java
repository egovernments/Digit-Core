package org.example.keycloak.abac;

import org.keycloak.authorization.policy.provider.PolicyProvider;
import org.keycloak.authorization.policy.provider.PolicyProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.model.Policy;
import org.keycloak.representations.idm.authorization.PolicyRepresentation;

public class ABACPolicyProviderFactory implements PolicyProviderFactory {

	private static final String PROVIDER_ID = "abac-policy-provider";

	@Override
	public String getId() {
		return PROVIDER_ID;
	}

	@Override
	public String getName() {
		return "ABAC Policy";
	}

	@Override
	public PolicyProvider create(AuthorizationProvider authorizationProvider) {
		return new ABACPolicyProvider();
	}

	@Override
	public void postInit(KeycloakSessionFactory factory) {
		// called during startup, after init
	}

	@Override
	public PolicyProvider create(KeycloakSession keycloakSession) {
		return null;
	}

	@Override
	public ABACPolicyRepresentation toRepresentation(Policy policy, AuthorizationProvider authorization) {
		ABACPolicyRepresentation representation = new ABACPolicyRepresentation();

		// Map policy properties to the ABACPolicyRepresentation fields
		representation.setCode(policy.getConfig().get("code"));
		representation.setRequiredAttribute(policy.getConfig().get("requiredAttribute"));
		representation.setRequiredValue(policy.getConfig().get("requiredValue"));
		return representation;
	}

	@Override
	public String getGroup() {
		return null;
	}

	@Override
	public Class<? extends ABACPolicyRepresentation> getRepresentationType() {
		return ABACPolicyRepresentation.class;
	}

	@Override
	public void init(Config.Scope config) {
		// Initialization logic if needed
	}

	@Override
	public void close() {
		// Cleanup resources if needed
	}
}
