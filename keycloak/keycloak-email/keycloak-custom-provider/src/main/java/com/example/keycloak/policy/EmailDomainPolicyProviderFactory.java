package com.example.keycloak.policy;
//import org.keycloak.authorization.AuthorizationProvider;
//import org.keycloak.authorization.model.Policy;
//import org.keycloak.authorization.policy.provider.PolicyProvider;
//import org.keycloak.authorization.policy.provider.PolicyProviderFactory;
//import org.keycloak.models.KeycloakSession;
//import org.keycloak.representations.idm.authorization.PolicyRepresentation;
//
//public class EmailDomainPolicyProviderFactory implements PolicyProviderFactory<PolicyRepresentation> {
//
//    @Override
//    public String getName() {
//        return "Email Domain Policy";
//    }
//
//    @Override
//    public String getGroup() {
//        return "Email Based";
//    }
//
//    @Override
//    public PolicyProvider create(Policy policy, AuthorizationProvider authorization) {
//        return new EmailDomainPolicyProvider(policy.toRepresentation());
//    }
//
//    @Override
//    public PolicyProvider create(KeycloakSession session) {
//        return new EmailDomainPolicyProvider(null);
//    }
//
//    @Override
//    public Class<PolicyRepresentation> getRepresentationType() {
//        return PolicyRepresentation.class;
//    }
//
//    @Override
//    public void onCreate(Policy policy, PolicyRepresentation representation,
//                         AuthorizationProvider authorization) {
//        // Handle policy creation logic
//    }
//
//    @Override
//    public void onUpdate(Policy policy, PolicyRepresentation representation,
//                         AuthorizationProvider authorization) {
//        // Handle policy update logic
//    }
//
//    @Override
//    public void onDelete(Policy policy, AuthorizationProvider authorization) {
//        // Handle policy deletion
//    }
//}


import java.util.List;

//import org.keycloak.authorization.AuthorizationProvider;
//import org.keycloak.authorization.model.Policy;
//import org.keycloak.authorization.policy.provider.PolicyProvider;
//import org.keycloak.authorization.policy.provider.PolicyProviderFactory;
//import org.keycloak.models.KeycloakSession;
//import org.keycloak.models.KeycloakSessionFactory;
//import org.keycloak.models.utils.ModelToRepresentation;
//import org.keycloak.representations.idm.authorization.PolicyRepresentation;
//
//public class EmailDomainPolicyProviderFactory implements PolicyProviderFactory<PolicyRepresentation> {
//
//    private final EmailDomainPolicyProvider provider = new EmailDomainPolicyProvider();
//
//    @Override
//    public String getName() {
//        return "Email Domain Policy";
//    }
//
//    @Override
//    public String getGroup() {
//        return "Email Based";
//    }
//
//    @Override
//    public PolicyProvider create(Policy policy, AuthorizationProvider authorization) {
//        PolicyRepresentation rep = ModelToRepresentation.toRepresentation(policy, authorization);
//        provider.configure(rep);
//        return provider;
//
//    }
//
//
//
//    @Override
//    public PolicyProvider create(AuthorizationProvider authorization) {
//        return provider;
//    }
//
//    @Override
//    public PolicyProvider create(KeycloakSession session) {
//        return provider;
//    }
//
//    @Override
//    public PolicyRepresentation toRepresentation(Policy policy, AuthorizationProvider authorization) {
//        PolicyRepresentation representation = new PolicyRepresentation();
//        representation.setName(policy.getName());
//        representation.setDescription(policy.getDescription());
//        representation.setType(getId());
//        representation.setConfig(policy.getConfig());
//        return representation;
//    }
//
//    @Override
//    public Class<PolicyRepresentation> getRepresentationType() {
//        return PolicyRepresentation.class;
//    }
//
//    @Override
//    public void onCreate(Policy policy, PolicyRepresentation representation, AuthorizationProvider authorization) {
//        // Handle policy creation logic if needed
//    }
//
//    @Override
//    public void onUpdate(Policy policy, PolicyRepresentation representation, AuthorizationProvider authorization) {
//        // Handle policy update logic if needed
//    }
//
//    @Override
//    public void onRemove(Policy policy, AuthorizationProvider authorization) {
//        // Handle policy deletion if needed
//    }
//
//    @Override
//    public void init(org.keycloak.Config.Scope config) {
//        // Initialize if needed
//    }
//
//    @Override
//    public void postInit(KeycloakSessionFactory factory) {
//        // Post-initialization if needed
//    }
//
//    @Override
//    public void close() {
//        // Cleanup resources if needed
//    }
//
//    @Override
//    public String getId() {
//        return "email-domain";
//    }
//}


//import org.keycloak.authorization.AuthorizationProvider;
//import org.keycloak.authorization.model.Policy;
//import org.keycloak.authorization.policy.provider.PolicyProvider;
//import org.keycloak.authorization.policy.provider.PolicyProviderFactory;
//import org.keycloak.models.KeycloakSession;
//import org.keycloak.models.KeycloakSessionFactory;
//import org.keycloak.representations.idm.authorization.PolicyRepresentation;
//
//public class EmailDomainPolicyProviderFactory implements PolicyProviderFactory<PolicyRepresentation> {
//
//    private final EmailDomainPolicyProvider provider = new EmailDomainPolicyProvider();
//
//    // Corrected create() method using factory's own toRepresentation
//    @Override
//    public PolicyProvider create(Policy policy, AuthorizationProvider authorization) {
//        PolicyRepresentation rep = toRepresentation(policy, authorization);
//        provider.configure(rep);
//        return provider;
//    }
//
//    // Proper representation conversion
//    @Override
//    public PolicyRepresentation toRepresentation(Policy policy, AuthorizationProvider authorization) {
//        PolicyRepresentation representation = new PolicyRepresentation();
//        representation.setId(policy.getId());
//        representation.setName(policy.getName());
//        representation.setDescription(policy.getDescription());
//        representation.setType(getId());
//        representation.setDecisionStrategy(policy.getDecisionStrategy());
//        representation.setLogic(policy.getLogic());
//        representation.setConfig(policy.getConfig());
//        return representation;
//    }
//
//    // Rest of the factory implementation
//    @Override
//    public String getId() {
//        return "email-domain";
//    }
//
//    @Override
//    public PolicyProvider create(AuthorizationProvider authorization) {
//        return provider;
//    }
//
//    @Override
//    public PolicyProvider create(KeycloakSession session) {
//        return provider;
//    }
//
//    @Override
//    public String getName() {
//        return "Email Domain Policy";
//    }
//
//    @Override
//    public String getGroup() {
//        return "Email Based";
//    }
//
//    @Override
//    public Class<PolicyRepresentation> getRepresentationType() {
//        return PolicyRepresentation.class;
//    }
//
//    // Remaining interface methods
//    @Override
//    public void init(org.keycloak.Config.Scope config) {}
//
//    @Override
//    public void postInit(KeycloakSessionFactory factory) {}
//
//    @Override
//    public void close() {}
//}
//



import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.policy.provider.PolicyProvider;
import org.keycloak.authorization.policy.provider.PolicyProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.representations.idm.authorization.PolicyRepresentation;

public class EmailDomainPolicyProviderFactory implements PolicyProviderFactory {

    private EmailDomainPolicyProvider provider = new EmailDomainPolicyProvider();

    // CORRECT METHOD SIGNATURES (Keycloak 25.x)
    @Override
    public PolicyProvider create(KeycloakSession session) {
        return provider;
    }

    @Override
    public PolicyProvider create(AuthorizationProvider authorization) {
        return provider;
    }

    @Override
    public PolicyRepresentation toRepresentation(Policy policy, AuthorizationProvider authorization) {
        PolicyRepresentation rep = new PolicyRepresentation();
        rep.setId(policy.getId());
        rep.setName(policy.getName());
        rep.setType(getId());
        rep.setConfig(policy.getConfig());
        return rep;
    }

    @Override
    public String getId() {
        return "email-domain";
    }

    @Override
    public String getName() {
        return "Email Domain Policy";
    }

    @Override
    public String getGroup() {
        return "Email Based";
    }

    // Remaining required methods
    @Override
    public void init(org.keycloak.Config.Scope config) {}

    @Override
    public void postInit(KeycloakSessionFactory factory) {}

    @Override
    public void close() {}

    @Override
    public Class<PolicyRepresentation> getRepresentationType() {
        return PolicyRepresentation.class;
    }
}



