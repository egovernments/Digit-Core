package org.egov.user.security.oauth2.custom;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.user.domain.model.SecureUser;
import org.egov.user.domain.model.TokenWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class CustomRedisTokenStore {

    private static final String ACCESS_PREFIX = "user:access:";
    private static final String AUTH_PREFIX = "user:auth:";
    private static final String REFRESH_PREFIX = "user:refresh:";
    private static final String AUTH_TO_ACCESS_PREFIX = "user:auth_to_access:";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${access.token.validity.in.minutes}")
    private int accessTokenValidityInMinutes;

    @Value("${refresh.token.validity.in.minutes}")
    private int refreshTokenValidityInMinutes;

    @Value("${key.generator.hash.algorithm}")
    private String hashAlgorithm;

    public Map<String, Object> createAccessToken(Authentication authentication, String tenantId) {
        String accessToken = UUID.randomUUID().toString();
        String refreshToken = UUID.randomUUID().toString();

        Map<String, Object> tokenData = new LinkedHashMap<>();
        tokenData.put("access_token", accessToken);
        tokenData.put("token_type", "bearer");
        tokenData.put("refresh_token", refreshToken);
        tokenData.put("expires_in", accessTokenValidityInMinutes * 60);
        tokenData.put("scope", "read write");

        // Store access token → authentication mapping
        String authKey = extractKey(authentication, tenantId);
        redisTemplate.opsForValue().set(ACCESS_PREFIX + accessToken, authKey,
                accessTokenValidityInMinutes, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(AUTH_PREFIX + accessToken, authentication.getName(),
                accessTokenValidityInMinutes, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(REFRESH_PREFIX + refreshToken, accessToken,
                refreshTokenValidityInMinutes, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(AUTH_TO_ACCESS_PREFIX + authKey, accessToken,
                accessTokenValidityInMinutes, TimeUnit.MINUTES);

        // Store the SecureUser principal for token lookup
        if (authentication.getPrincipal() instanceof SecureUser) {
            SecureUser secureUser = (SecureUser) authentication.getPrincipal();
            redisTemplate.opsForValue().set("user:principal:" + accessToken, secureUser,
                    accessTokenValidityInMinutes, TimeUnit.MINUTES);
        }

        return tokenData;
    }

    public Authentication readAuthentication(String accessToken) {
        String username = (String) redisTemplate.opsForValue().get(AUTH_PREFIX + accessToken);
        if (username == null) {
            return null;
        }
        SecureUser principal = (SecureUser) redisTemplate.opsForValue().get("user:principal:" + accessToken);
        if (principal == null) {
            return null;
        }
        return new UsernamePasswordAuthenticationToken(principal, "", principal.getAuthorities());
    }

    public boolean removeAccessToken(String accessToken) {
        Boolean deleted = redisTemplate.delete(ACCESS_PREFIX + accessToken);
        redisTemplate.delete(AUTH_PREFIX + accessToken);
        redisTemplate.delete("user:principal:" + accessToken);
        return deleted != null && deleted;
    }

    public String refreshAccessToken(String refreshToken, String tenantId) {
        String oldAccessToken = (String) redisTemplate.opsForValue().get(REFRESH_PREFIX + refreshToken);
        if (oldAccessToken == null) {
            return null;
        }

        Authentication auth = readAuthentication(oldAccessToken);
        if (auth == null) {
            return null;
        }

        // Remove old access token
        removeAccessToken(oldAccessToken);

        // Create new access token
        String newAccessToken = UUID.randomUUID().toString();
        String authKey = extractKey(auth, tenantId);

        redisTemplate.opsForValue().set(ACCESS_PREFIX + newAccessToken, authKey,
                accessTokenValidityInMinutes, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(AUTH_PREFIX + newAccessToken, auth.getName(),
                accessTokenValidityInMinutes, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(REFRESH_PREFIX + refreshToken, newAccessToken,
                refreshTokenValidityInMinutes, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(AUTH_TO_ACCESS_PREFIX + authKey, newAccessToken,
                accessTokenValidityInMinutes, TimeUnit.MINUTES);

        if (auth.getPrincipal() instanceof SecureUser) {
            redisTemplate.opsForValue().set("user:principal:" + newAccessToken, auth.getPrincipal(),
                    accessTokenValidityInMinutes, TimeUnit.MINUTES);
        }

        return newAccessToken;
    }

    private String extractKey(Authentication authentication, String tenantId) {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("username", authentication.getName());
        values.put("client_id", "egov-user-client");
        values.put("scope", "read write");
        if (tenantId != null && !tenantId.isEmpty()) {
            values.put("tenantId", tenantId);
        }

        try {
            MessageDigest digest = MessageDigest.getInstance(hashAlgorithm);
            byte[] bytes = digest.digest(values.toString().getBytes("UTF-8"));
            return String.format("%032x", new BigInteger(1, bytes));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new IllegalStateException("Hash algorithm error", e);
        }
    }
}
