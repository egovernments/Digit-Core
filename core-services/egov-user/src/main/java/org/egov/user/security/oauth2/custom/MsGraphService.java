package org.egov.user.security.oauth2.custom;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.user.config.AuthProperties;
import org.egov.user.domain.model.User;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enriches user with MFA details from Microsoft Graph API (authentication methods).
 * Uses client-credentials flow to call GET /users/{oid}/authentication/methods.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MsGraphService {

    private static final String PHONE_TYPE = "#microsoft.graph.phoneAuthenticationMethod";
    private static final Pattern LAST_FOUR_DIGITS = Pattern.compile("(\\d{4})\\s*$");

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Enrich user with MFA details from Graph (phone last 4, device name, registered date, method summary).
     * No-op if Graph is not configured or call fails.
     *
     * @param user           user to enrich (MFA fields will be set on this instance)
     * @param provider       auth provider with Graph config (graphClientId, graphClientSecret, graphTenantId)
     * @param userOidForGraph Azure object id (oid) for Graph API; use this instead of idpSubject (sub) for Microsoft
     */
    public void enrichUserWithMfaDetails(User user, AuthProperties.Provider provider, String userOidForGraph) {
        if (user == null || provider == null) return;
        if (!StringUtils.hasText(provider.getGraphClientId()) || !StringUtils.hasText(provider.getGraphClientSecret())
                || !StringUtils.hasText(provider.getGraphTenantId())) {
            log.debug("Graph API not configured; skipping MFA enrichment");
            return;
        }
        String userOid = StringUtils.hasText(userOidForGraph) ? userOidForGraph : user.getIdpSubject();
        if (!StringUtils.hasText(userOid)) {
            log.debug("No user oid for Graph; cannot call Graph methods API");
            return;
        }
        try {
            String accessToken = getGraphAccessToken(provider);
            if (accessToken == null) return;
            fetchAndApplyAuthenticationMethods(user, provider, userOid, accessToken);
        } catch (Exception e) {
            log.warn("Failed to enrich user with Graph MFA details: {}", e.getMessage());
        }
    }

    private String getGraphAccessToken(AuthProperties.Provider provider) {
        String tokenUrl = String.format(provider.getGraphTokenUrl(), provider.getGraphTenantId());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", provider.getGraphClientId());
        body.add("client_secret", provider.getGraphClientSecret());
        body.add("scope", provider.getGraphScope() != null ? provider.getGraphScope() : "https://graph.microsoft.com/.default");
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    tokenUrl,
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    String.class);
            if (response.getBody() == null) return null;
            JsonNode node = objectMapper.readTree(response.getBody());
            JsonNode token = node.get("access_token");
            return token != null ? token.asText() : null;
        } catch (Exception e) {
            log.warn("Failed to get Graph access token: {}", e.getMessage());
            return null;
        }
    }

    private void fetchAndApplyAuthenticationMethods(User user, AuthProperties.Provider provider,
                                                    String userOid, String accessToken) {
        String methodsUrl = String.format(provider.getGraphMethodsUrl(), userOid);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    methodsUrl,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class);
            if (response.getBody() == null) return;
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode value = root.get("value");
            if (value == null || !value.isArray()) return;

            String firstPhoneLast4 = null;
            String firstDeviceName = null;
            Date earliestCreated = null;
            StringBuilder details = new StringBuilder();

            for (JsonNode method : value) {
                String type = method.has("@odata.type") ? method.get("@odata.type").asText(null) : null;
                String displayName = method.has("displayName") ? method.get("displayName").asText(null) : null;
                String createdStr = method.has("creationDateTime") ? method.get("creationDateTime").asText(null) : null;
                if (createdStr == null && method.has("createdDateTime")) createdStr = method.get("createdDateTime").asText(null);
                Date created = parseIso8601(createdStr);
                if (type != null) {
                    if (details.length() > 0) details.append(", ");
                    details.append(type.replace("#microsoft.graph.", ""));
                }
                if (displayName != null && firstDeviceName == null) firstDeviceName = displayName;
                if (created != null && (earliestCreated == null || created.before(earliestCreated)))
                    earliestCreated = created;

                if (PHONE_TYPE.equals(type) && method.has("phoneNumber")) {
                    String phone = method.get("phoneNumber").asText(null);
                    if (phone != null && firstPhoneLast4 == null) {
                        String last4 = extractLastFourDigits(phone);
                        if (last4 != null) firstPhoneLast4 = last4;
                    }
                }
            }

            if (firstPhoneLast4 != null) user.setMfaPhoneLast4(firstPhoneLast4);
            if (firstDeviceName != null) user.setMfaDeviceName(firstDeviceName);
            if (earliestCreated != null) user.setMfaRegisteredOn(earliestCreated);
            if (details.length() > 0) user.setMfaDetails(details.toString());
            if (firstPhoneLast4 != null || firstDeviceName != null || earliestCreated != null || details.length() > 0) {
                log.info("Enriched user MFA from Graph: phoneLast4={}, device={}, registeredOn={}, details={}",
                        firstPhoneLast4, firstDeviceName, earliestCreated, details.length() > 0 ? details.toString() : null);
            }
        } catch (Exception e) {
            log.warn("Failed to parse Graph methods response: {}", e.getMessage());
        }
    }

    private static String extractLastFourDigits(String phone) {
        if (phone == null) return null;
        Matcher m = LAST_FOUR_DIGITS.matcher(phone.trim());
        return m.find() ? m.group(1) : null;
    }

    private static Date parseIso8601(String s) {
        if (s == null || s.isEmpty()) return null;
        try {
            return Date.from(Instant.parse(s));
        } catch (Exception e) {
            return null;
        }
    }
}
