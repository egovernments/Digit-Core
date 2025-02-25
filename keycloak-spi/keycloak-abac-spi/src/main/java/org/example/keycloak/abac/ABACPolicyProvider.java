package org.example.keycloak.abac;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.keycloak.authorization.attribute.Attributes;
import org.keycloak.authorization.identity.Identity;
import org.keycloak.authorization.policy.evaluation.Evaluation;
import org.keycloak.authorization.policy.evaluation.EvaluationContext;
import org.keycloak.authorization.policy.provider.PolicyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;

public class ABACPolicyProvider implements PolicyProvider {

	private static final Logger log = LoggerFactory.getLogger(ABACPolicyProvider.class);
	private static final ObjectMapper objectMapper = new ObjectMapper();

	static {
		// Configure ObjectMapper to pretty-print JSON
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
	}

	@Override
	public void evaluate(Evaluation evaluation) {
		log.info("Policy evaluation started for policy: {}", evaluation.getPolicy().getName());

		try {
			String resourcePermission = objectMapper.writeValueAsString(evaluation.getPermission());
			log.info("ResourcePermission Object: {}", resourcePermission);
		} catch (Exception e) {
			log.error("Failed to serialize ResourcePermission object", e);
		}

		try {
			String evaluationContextIdentity = objectMapper.writeValueAsString(evaluation.getContext().getIdentity());
			log.info("EvaluationContext.Identity Object: {}", evaluationContextIdentity);
		} catch (Exception e) {
			log.error("Failed to serialize EvaluationContext object", e);
		}

		try {
			String evaluationContextAttributes = objectMapper.writeValueAsString(evaluation.getContext().getAttributes());
			log.info("EvaluationContext.Attributes Object: {}", evaluationContextAttributes);
		} catch (Exception e) {
			log.error("Failed to serialize EvaluationContext object", e);
		}

		try {
			String policy = objectMapper.writeValueAsString(evaluation.getPolicy());
			log.info("Policy Object: {}", policy);
		} catch (Exception e) {
			log.error("Failed to serialize Policy object", e);
		}

//		try {
//			String realm = objectMapper.writeValueAsString(evaluation.getRealm());
//			log.info("Realm Object: {}", realm);
//		} catch (Exception e) {
//			log.error("Failed to serialize Realm object", e);
//		}

		try {
			String authorizationProvider = objectMapper.writeValueAsString(evaluation.getAuthorizationProvider());
			log.info("AuthorizationProvider Object: {}", authorizationProvider);
		} catch (Exception e) {
			log.error("Failed to serialize AuthorizationProvider object", e);
		}

		try {
			String effect = objectMapper.writeValueAsString(evaluation.getEffect());
			log.info("Effect Object: {}", effect);
		} catch (Exception e) {
			log.error("Failed to serialize Effect object", e);
		}


		EvaluationContext context = evaluation.getContext();

		// Get attributes from the request
		Map<String, Collection<String>> attributeList = context.getAttributes().toMap();

		if (!attributeList.isEmpty()) {
			log.info("Request Attributes:");
			attributeList.forEach((key, value) -> {
				log.info("Key: " + key + ", Value: " + value);
			});
		} else {
			log.info("No attributes found in request.");
		}

//		KeycloakContext keycloakContext = evaluation.getAuthorizationProvider().getKeycloakSession().getContext();
//		Map<String, List<String>> headers = keycloakContext.getRequestHeaders().getRequestHeaders();
//
//		log.info("Request Headers:");
//		headers.forEach((key, value) -> log.info("Key: {}, Value: {}", key, value));
//
//		String requestFormParameters = keycloakContext.getHttpRequest().getDecodedFormParameters().toString();
//		log.info("Request Decoded Form Parameters: {}", requestFormParameters);
//
//		String requestMultiPartFormParameters = keycloakContext.getHttpRequest().getMultiPartFormParameters().toString();
//		log.info("Request Decoded Form Parameters: {}", requestMultiPartFormParameters);
//
//		MultivaluedMap<String, String> queryParams = keycloakContext.getHttpRequest().getUri().getQueryParameters();
//
//		log.info("Query Parameters:");
//		queryParams.forEach((key, value) -> log.info("Key: {}, Value: {}", key, value));
//
//		MultivaluedMap<String, String> pathParams = keycloakContext.getHttpRequest().getUri().getPathParameters();
//
//		log.info("Path Parameters:");
//		pathParams.forEach((key, value) -> log.info("Key: {}, Value: {}", key, value));

		// Get the Identity object
		Identity identity = evaluation.getContext().getIdentity();

		// Get all attributes (claims) from the Identity
		Attributes attributes = identity.getAttributes();

		// Print all claims
		log.info("Claims from Identity:");
		for (String claimName : attributes.toMap().keySet()) {
			Attributes.Entry claimEntry = attributes.getValue(claimName);
			log.info("Claim: {} = {}", claimName, claimEntry.asString(0)); // Print the first value of the claim
		}

		// Get attributes from the EvaluationContext
		Map<String, Collection<String>> contextAttributes = evaluation.getContext().getAttributes().toMap();

		// Print all context attributes
		log.info("Claims from EvaluationContext:");
		contextAttributes.forEach((key, value) -> {
			log.info("Key: {}, Value: {}", key, value);
		});

//		Policy policy = evaluation.getPolicy();
//		Identity identity = evaluation.getContext().getIdentity();
//
//		String requiredAttribute = policy.getConfig().get("requiredAttribute");
//		String requiredValue = policy.getConfig().get("requiredValue");
//
//		if (requiredAttribute != null && requiredValue != null) {
//			Attributes attributes = identity.getAttributes();
//			Attributes.Entry attributeEntry = attributes.getValue(requiredAttribute);
//
//			if (attributeEntry != null) {
//				// Loop through all values of the attribute
//				for (int i = 0; i < attributeEntry.size(); i++) {
//					String attributeValue = attributeEntry.asString(i);
//					if (attributeValue.equals(requiredValue)) {
//						evaluation.grant();
//						return;
//					}
//				}
//			}
//		} else {
//			log.info("No required attribute or value found in policy.");
//		}
	}

	@Override
	public void close() {
		// Cleanup resources if needed
	}
}
