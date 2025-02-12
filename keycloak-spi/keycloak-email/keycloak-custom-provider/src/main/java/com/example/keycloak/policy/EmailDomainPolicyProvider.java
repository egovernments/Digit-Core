package com.example.keycloak.policy;
//import org.keycloak.authorization.policy.evaluation.Evaluation;
//import org.keycloak.authorization.policy.provider.PolicyProvider;
//import org.keycloak.representations.idm.authorization.PolicyRepresentation;
//
//public class EmailDomainPolicyProvider implements PolicyProvider {
//
//    private final String allowedDomain;
//
//    public EmailDomainPolicyProvider(PolicyRepresentation representation) {
//        this.allowedDomain = "@keycloak.org"; // Configure via representation.getConfig() if needed
//    }
//
//    @Override
//    public void evaluate(Evaluation evaluation) {
//        var context = evaluation.getContext();
//        var identity = context.getIdentity();
//        var attributes = identity.getAttributes();
//        var email = attributes.getValue("email").asString(0);
//
//        if (email != null && email.endsWith(allowedDomain)) {
//            evaluation.grant();
//        }
//    }
//
//    @Override
//    public void close() {
//        // Cleanup resources if needed
//    }
//}

//import org.keycloak.authorization.policy.evaluation.Evaluation;
//import org.keycloak.authorization.policy.provider.PolicyProvider;
//import org.keycloak.models.KeycloakSession;
//import org.keycloak.representations.idm.authorization.PolicyRepresentation;
//
//public class EmailDomainPolicyProvider implements PolicyProvider {
//
//    private String allowedDomain = "@keycloak.org";
//
//    public void configure(PolicyRepresentation representation) {
//        if (representation != null && representation.getConfig() != null) {
//            this.allowedDomain = representation.getConfig().getOrDefault("domain", "@keycloak.org");
//        }
//    }
//
//    @Override
//    public void evaluate(Evaluation evaluation) {
//        var context = evaluation.getContext();
//        var identity = context.getIdentity();
//        var attributes = identity.getAttributes();
//        var email = attributes.getValue("email").asString(0);
//
//        if (email != null && email.endsWith(allowedDomain)) {
//            evaluation.grant();
//        }
//    }
//
//    @Override
//    public void close() {
//        // Cleanup resources if needed
//    }
//}


import org.keycloak.authorization.policy.evaluation.Evaluation;
import org.keycloak.authorization.policy.provider.PolicyProvider;

public class EmailDomainPolicyProvider implements PolicyProvider {

    @Override
    public void evaluate(Evaluation evaluation) {
        String expectedEmail = "aaradhya@abcd.com"; // Hardcoded email for demo
        // String userEmail = evaluation.getContext().getIdentity().getAttribute("email");
        var context = evaluation.getContext();
       var identity = context.getIdentity();
       var attributes = identity.getAttributes();
       var email = attributes.getValue("email").asString(0);

        if (expectedEmail.equalsIgnoreCase(email)) {
            evaluation.grant(); // Allow access if email matches
        } else {
            evaluation.deny(); // Deny access otherwise
        }
    }

    @Override
    public void close() {
        // Cleanup if necessary
    }
}




