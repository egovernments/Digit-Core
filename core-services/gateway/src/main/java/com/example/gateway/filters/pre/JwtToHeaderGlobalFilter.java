package com.example.gateway.filters.pre;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.egov.common.contract.request.Role;
import org.egov.common.contract.request.User;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.gateway.constants.GatewayConstants.AUTH_BOOLEAN_FLAG_NAME;
import static com.example.gateway.constants.GatewayConstants.USER_INFO_KEY;

@Component
@Slf4j
public class JwtToHeaderGlobalFilter implements GlobalFilter, Ordered {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        Boolean doAuth = exchange.getAttribute(AUTH_BOOLEAN_FLAG_NAME);
        boolean isGetRequest = HttpMethod.GET.equals(exchange.getRequest().getMethod());
        String contentType = exchange.getRequest().getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);

        if ((Boolean.FALSE.equals(doAuth))) {
            return chain.filter(exchange);
        }

        return ReactiveSecurityContextHolder.getContext().flatMap(securityContext -> {
            Object principal = securityContext.getAuthentication().getPrincipal();
            if (principal instanceof Jwt) {
                Jwt jwt = (Jwt) principal;
                String userId = jwt.getClaim("sub");
                User user = buildUserFromJwt(jwt);
                try {
                    MDC.put(USER_INFO_KEY, objectMapper.writeValueAsString(user));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                ServerHttpRequest request = exchange.getRequest().mutate().header("X-user-id", userId).build();
                ServerWebExchange mutatedExchange = exchange.mutate().request(request).build();
                return chain.filter(mutatedExchange);
            }
            return chain.filter(exchange);
        });


    }

    private User buildUserFromJwt(Jwt jwt) {
        String userName = jwt.hasClaim("preferred_username") ? jwt.getClaim("preferred_username") : null;
        String name = jwt.hasClaim("name") ? jwt.getClaim("name") : null;
        String mobileNumber = jwt.hasClaim("phone_number") ? jwt.getClaim("phone_number") : null;
        String emailId = jwt.hasClaim("email") ? jwt.getClaim("email") : null;
        String uuid = jwt.hasClaim("sub") ? jwt.getClaim("sub") : null;

        String issuer = jwt.hasClaim("iss") ? jwt.getClaim("iss") : null;
        String tenantId = extractRealmFromIssuer(issuer);

        Map<String, Object> realmAccess = jwt.hasClaim("realm_access") ? jwt.getClaimAsMap("realm_access") : null;
        List<String> rolesString = Collections.emptyList();
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            Object rolesObj = realmAccess.get("roles");
            if (rolesObj instanceof List<?>) {
                rolesString = ((List<?>) rolesObj).stream()
                        .filter(String.class::isInstance)
                        .map(String.class::cast)
                        .toList();
            }
        }

        // Determine the user type based on roles
        String type = rolesString.contains("CITIZEN") ? "CITIZEN" : "EMPLOYEE";
        List<Role> roles = rolesString.stream().map(role -> Role.builder().name(role).code(role).tenantId(tenantId).build()).toList();

        return User.builder().userName(userName).name(name).type(type).mobileNumber(mobileNumber).emailId(emailId).roles(roles).tenantId(tenantId).uuid(uuid).build();
    }

    private String extractRealmFromIssuer(String issuer) {
        if (issuer == null || issuer.isEmpty()) {
            return null; // Return null if the issuer is null or empty
        }
        try {
            // Split the issuer URL by "/" and return the last segment
            String[] parts = issuer.split("/");
            return parts[parts.length - 1];
        } catch (Exception e) {
            log.error("Error while extracting realm from issuer: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public int getOrder() {
        return 4;
    }
}