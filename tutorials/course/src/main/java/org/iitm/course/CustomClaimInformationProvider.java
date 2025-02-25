//package org.iitm.course;
//
//import org.keycloak.adapters.authorization.cip.spi.ClaimInformationPointProvider;
//import org.keycloak.adapters.authorization.spi.HttpRequest;
//import org.keycloak.representations.adapters.config.PolicyEnforcerConfig;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class CustomClaimInformationProvider implements ClaimInformationPointProvider {
//	private final PolicyEnforcerConfig policyEnforcerConfig;
//
//	public CustomClaimInformationProvider(PolicyEnforcerConfig policyEnforcerConfig) {
//		this.policyEnforcerConfig = policyEnforcerConfig;
//	}
//
//	@Override
//	public Map<String, List<String>> resolve(HttpRequest request) {
//		Map<String, List<String>> claims = new HashMap<>();
//
//		// Extracting claims from the HTTP request
//		claims.put("claim-from-header", request.getHeader("b"));
//
//		return claims;
//	}
//}
