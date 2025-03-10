package com.example.gateway.filters.pre;
import com.example.gateway.config.ApplicationProperties;
import com.example.gateway.config.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Mono;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
//@Component
public class RbacKeycloakFilter implements GlobalFilter, Ordered {

    private  RestTemplate restTemplate;
    private  ObjectMapper objectMapper;
    private  ApplicationProperties properties;
    private SecurityConfig securityConfig;

    public RbacKeycloakFilter(RestTemplate restTemplate,ObjectMapper objectMapper, ApplicationProperties properties ,SecurityConfig securityConfig ) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.securityConfig = securityConfig;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String requestPath = exchange.getRequest().getURI().getPath();
        log.warn("Incoming request path: {}", requestPath);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Unauthorized access attempt to {} - Missing or invalid Authorization header", requestPath);
            return unauthorizedResponse(exchange, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        log.debug("Validating access for path: {} with token: {}", requestPath, token);

        try {
            // Extract realm dynamically from the token
            String issuer = securityConfig.extractIssuer(token);
            String realm = extractRealmFromIssuer(issuer);
            if (realm == null) {
                log.warn("Could not extract realm from token");
                return unauthorizedResponse(exchange, "Invalid token: Unable to extract realm");
            }

            boolean isAuthorized = validatePermission(token, requestPath, realm);

            if (isAuthorized) {
                log.info("Access granted for path: {}", requestPath);
                return chain.filter(exchange);
            } else {
                log.warn("Access denied for path: {}", requestPath);
                return unauthorizedResponse(exchange, "Access Denied");
            }
        } catch (Exception e) {
            return unauthorizedResponse(exchange, e.getMessage());
        }
    }

    private boolean validatePermission(String token, String permissionPath, String realm) {
        String keycloakUrl = String.format("%s/realms/%s/protocol/openid-connect/token", properties.getKeycloakBaseUrl(), realm);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBearerAuth(token);

        String requestBody = UriComponentsBuilder.newInstance()
                .queryParam("grant_type", "urn:ietf:params:oauth:grant-type:uma-ticket")
                .queryParam("audience", properties.getClientId())
                .queryParam("response_mode", "decision")
                .queryParam("response_include_resource_name", "true")
                .queryParam("permission_resource_format", "uri")
                .queryParam("permission", permissionPath)
                .build()
                .toString()
                .substring(1);

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(keycloakUrl, HttpMethod.POST, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseMap = objectMapper.readValue(response.getBody(), Map.class);
                return Boolean.TRUE.equals(responseMap.get("result"));
            } else {
                log.error("Keycloak validation failed: {}", response.getBody());
                throw new RuntimeException(response.getBody());
            }
        } catch (HttpStatusCodeException e) {
            log.error("Keycloak validation failed: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Keycloak validation failed for path {}: {}", permissionPath, e.getMessage());
            throw new RuntimeException("Keycloak error: " + e.getMessage());
        }
    }


    private String extractRealmFromIssuer(String issuer) {
        if (issuer == null || issuer.isEmpty()) {
            return null; // Return null if the issuer is null or empty
        }
        try {
            String[] parts = issuer.split("/");
            return parts[parts.length - 1];
        } catch (Exception e) {
            log.error("Error while extracting realm from issuer: {}", e.getMessage());
            return null;
        }
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String errorResponse = String.format("{\"error\": \"%s\"}", message);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(errorResponse.getBytes(StandardCharsets.UTF_8));

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return 5;
    }
}
