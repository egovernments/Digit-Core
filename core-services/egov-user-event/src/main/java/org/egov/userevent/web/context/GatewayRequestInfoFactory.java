package org.egov.userevent.web.context;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.common.contract.request.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Builds the internal RequestInfo the service/validator layer expects from the
 * gateway-populated 3.0 headers (X-Tenant-ID, X-User-ID, bearer token).
 *
 * Roles are taken from the bearer JWT's realm_access.roles claim (Keycloak
 * format). The token payload is base64-decoded WITHOUT signature verification —
 * the gateway is trusted to have authenticated the caller; this service only
 * reads claims. Keycloak plumbing roles (default-roles-*, offline_access,
 * uma_authorization) are filtered out. When no token is present or the claim
 * is absent/unreadable, a single role equal to the controller-decided user
 * type is synthesized as a fallback so validateRI keeps passing.
 *
 * The raw token is also forwarded untouched as authToken for downstream calls
 * (e.g. mdms-v2).
 */
@Slf4j
@Component
public class GatewayRequestInfoFactory {

	private static final String BEARER_PREFIX = "Bearer ";
	private static final String REALM_ACCESS_CLAIM = "realm_access";
	private static final String ROLES_CLAIM = "roles";
	private static final String KEYCLOAK_DEFAULT_ROLES_PREFIX = "default-roles-";
	private static final Set<String> KEYCLOAK_PLUMBING_ROLES = Set.of("offline_access", "uma_authorization");

	@Autowired
	private ObjectMapper objectMapper;

	public RequestInfo from(HttpServletRequest request, String userType) {
		String tenantId = request.getHeader(HeaderNames.TENANT_ID);
		String userId = request.getHeader(HeaderNames.USER_ID);
		String authToken = extractBearerToken(request);

		User userInfo = User.builder()
				.uuid(userId)
				.userName(userId)
				.type(userType)
				.tenantId(tenantId)
				.roles(resolveRoles(authToken, userType, tenantId))
				.build();

		return RequestInfo.builder()
				.apiId("egov-user-event")
				.ver("3.0")
				.ts(new Date().getTime())
				.msgId(request.getHeader(HeaderNames.REQUEST_ID))
				.correlationId(request.getHeader(HeaderNames.CORRELATION_ID))
				.authToken(authToken)
				.userInfo(userInfo)
				.build();
	}

	private List<Role> resolveRoles(String authToken, String userType, String tenantId) {
		List<String> jwtRoles = extractRealmRoles(authToken);
		if (jwtRoles.isEmpty()) {
			return Collections.singletonList(
					Role.builder().code(userType).name(userType).tenantId(tenantId).build());
		}
		return jwtRoles.stream()
				.map(role -> Role.builder().code(role).name(role).tenantId(tenantId).build())
				.collect(Collectors.toList());
	}

	/**
	 * Reads realm_access.roles from the JWT payload. Returns an empty list on
	 * any shape/parse problem — the caller falls back to the synthesized role.
	 */
	@SuppressWarnings("unchecked")
	private List<String> extractRealmRoles(String authToken) {
		if (null == authToken)
			return Collections.emptyList();
		String[] segments = authToken.split("\\.");
		if (segments.length < 2)
			return Collections.emptyList();
		try {
			String payload = new String(Base64.getUrlDecoder().decode(segments[1]), StandardCharsets.UTF_8);
			Map<String, Object> claims = objectMapper.readValue(payload, Map.class);
			Object realmAccess = claims.get(REALM_ACCESS_CLAIM);
			if (!(realmAccess instanceof Map))
				return Collections.emptyList();
			Object roles = ((Map<String, Object>) realmAccess).get(ROLES_CLAIM);
			if (!(roles instanceof List))
				return Collections.emptyList();
			return ((List<Object>) roles).stream()
					.filter(role -> role instanceof String)
					.map(role -> (String) role)
					.filter(role -> !role.startsWith(KEYCLOAK_DEFAULT_ROLES_PREFIX))
					.filter(role -> !KEYCLOAK_PLUMBING_ROLES.contains(role))
					.collect(Collectors.toList());
		} catch (Exception e) {
			log.warn("Could not decode roles from bearer token; falling back to synthesized role: {}",
					e.getMessage());
			return Collections.emptyList();
		}
	}

	private String extractBearerToken(HttpServletRequest request) {
		String authorization = request.getHeader(HeaderNames.AUTHORIZATION);
		if (authorization != null && authorization.startsWith(BEARER_PREFIX)) {
			return authorization.substring(BEARER_PREFIX.length());
		}
		return authorization;
	}
}
