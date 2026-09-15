package org.egov.user.security.oauth2.custom.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.egov.user.config.AuthProperties;
import org.egov.user.config.GraphClientSecretResolver;
import org.egov.user.domain.service.utils.EncryptionDecryptionUtil;
import org.egov.user.security.SecurityConstants;
import org.egov.user.security.oauth2.custom.service.GraphAccessTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

/**
 * Obtains Microsoft Graph API access tokens via client-credentials flow.
 * Tokens are cached in Redis (graph:token:providerId:tenantId) with TTL.
 */
@Slf4j
@Component
public class GraphAccessTokenProviderImpl implements GraphAccessTokenProvider {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final GraphClientSecretResolver secretResolver;
    private final EncryptionDecryptionUtil encryptionDecryptionUtil;

    @Value("${auth.graph.token-cache-ttl-buffer-seconds:300}")
    private int tokenCacheTtlBufferSeconds;

    public GraphAccessTokenProviderImpl(RestTemplate restTemplate, ObjectMapper objectMapper,
                                        StringRedisTemplate stringRedisTemplate, GraphClientSecretResolver secretResolver,
                                        EncryptionDecryptionUtil encryptionDecryptionUtil) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.secretResolver = secretResolver;
        this.encryptionDecryptionUtil = encryptionDecryptionUtil;
    }

    @Override
    public String getAccessToken(AuthProperties.Provider provider) {
        String cacheKey = SecurityConstants.GRAPH_TOKEN_REDIS_KEY_PREFIX
                + (provider.getId() != null ? provider.getId() : "")
                + ":"
                + (provider.getGraphTenantId() != null ? provider.getGraphTenantId() : "");

        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (StringUtils.hasText(cached)) {
            log.debug("Graph token cache HIT for key: {}", cacheKey);
            try {
                return encryptionDecryptionUtil.decryptGraphToken(cached);
            } catch (CustomException e) {
                log.warn("Graph token decryption failed (e.g. stale raw token in cache), invalidating key: {}", cacheKey);
                stringRedisTemplate.delete(cacheKey);
            }
        }

        log.info("Graph token cache MISS for key: {}, fetching new token", cacheKey);
        String tokenUrl = String.format(provider.getGraphTokenUrl(), provider.getGraphTenantId());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add(SecurityConstants.KEY_GRANT_TYPE, SecurityConstants.GRANT_TYPE_CLIENT_CREDENTIALS);
        body.add(SecurityConstants.KEY_CLIENT_ID, provider.getGraphClientId());
        body.add(SecurityConstants.KEY_CLIENT_SECRET, secretResolver.resolve(provider));
        body.add(SecurityConstants.KEY_SCOPE, provider.getGraphScope() != null ? provider.getGraphScope() : SecurityConstants.DEFAULT_GRAPH_SCOPE);
        ResponseEntity<String> response = restTemplate.exchange(
                tokenUrl,
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                String.class);
        if (response.getBody() == null) return null;

        JsonNode node;
        try {
            node = objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            log.warn("Failed to parse Graph access token response: {}", e.getMessage());
            return null;
        }
        JsonNode tokenNode = node.get(SecurityConstants.KEY_ACCESS_TOKEN);
        String token = tokenNode != null ? tokenNode.asText() : null;
        if (!StringUtils.hasText(token)) return null;

        JsonNode expiresInNode = node.get(SecurityConstants.KEY_EXPIRES_IN);
        int expiresInSeconds = expiresInNode != null && !expiresInNode.isNull() ? expiresInNode.asInt(3600) : 3600;
        long ttlSeconds = Math.max(1L, expiresInSeconds - tokenCacheTtlBufferSeconds);

        String encryptedToken = encryptionDecryptionUtil.encryptGraphToken(token);
        log.info("Caching Graph token for key: {} with TTL: {} seconds", cacheKey, ttlSeconds);
        stringRedisTemplate.opsForValue().set(cacheKey, encryptedToken, ttlSeconds, TimeUnit.SECONDS);
        return token;
    }
}
