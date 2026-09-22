package org.egov.user.security;

import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class ClientBasicAuthFilter extends OncePerRequestFilter {

    private static final String BASIC_PREFIX = "Basic ";
    private static final String UNAUTHORIZED_CLIENT_BODY = "{\"error\":\"unauthorized_client\"}";

    private final ClientDetailsService clientDetailsService;

    public ClientBasicAuthFilter(ClientDetailsService clientDetailsService) {
        this.clientDetailsService = clientDetailsService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getServletPath().endsWith("/oauth/tenants");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        String[] credentials = parseBasicCredentials(header);
        if (credentials == null) {
            rejectUnauthorizedClient(response);
            return;
        }

        ClientDetails clientDetails;
        try {
            clientDetails = clientDetailsService.loadClientByClientId(credentials[0]);
        } catch (ClientRegistrationException e) {
            rejectUnauthorizedClient(response);
            return;
        }

        String clientSecret = clientDetails.getClientSecret();
        if (StringUtils.hasText(clientSecret) && !clientSecret.equals(credentials[1])) {
            rejectUnauthorizedClient(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String[] parseBasicCredentials(String header) {
        if (header == null || !header.startsWith(BASIC_PREFIX)) {
            return null;
        }
        String decoded;
        try {
            decoded = new String(Base64.getDecoder().decode(header.substring(BASIC_PREFIX.length())),
                    StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return null;
        }
        int separatorIndex = decoded.indexOf(':');
        if (separatorIndex < 0) {
            return null;
        }
        return new String[] { decoded.substring(0, separatorIndex), decoded.substring(separatorIndex + 1) };
    }

    private void rejectUnauthorizedClient(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(UNAUTHORIZED_CLIENT_BODY);
    }
}
