package org.egov.user.security.oauth2.custom;

import lombok.extern.slf4j.Slf4j;
import org.egov.user.domain.exception.sso.SsoException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.error.DefaultWebResponseExceptionTranslator;
import org.springframework.security.oauth2.provider.error.WebResponseExceptionTranslator;
import org.springframework.stereotype.Component;

/**
 * Translates SSO failures from the OAuth2 token endpoint into structured OAuth2Exception responses
 * where the error code is the machine-readable SSO error code and the description is the
 * human-readable message.
 *
 * <p>Frontend receives:
 * <pre>
 * { "error": "sso.jwt.invalid", "error_description": "JWT validation failed: ..." }
 * </pre>
 *
 * <p><b>Why non-SSO cases are delegated rather than re-implemented.</b> This translator is installed
 * via {@code AuthorizationServerEndpointsConfigurer.exceptionTranslator(...)}, which replaces the
 * default for the <em>entire</em> {@code /oauth/token} endpoint — so it also sees {@code password}
 * and {@code refresh_token} failures, which are already in production. Every non-SSO case is
 * therefore handed verbatim to {@link DefaultWebResponseExceptionTranslator} so that the error
 * contract those grants already have is preserved exactly: the 405 branch for non-POST requests,
 * the {@code Cache-Control: no-store} and {@code Pragma: no-cache} headers, the
 * {@code WWW-Authenticate} header on 401s, and {@code ThrowableAnalyzer}-based cause-chain
 * unwrapping. Re-implementing those branches here silently changed all four.
 */
@Slf4j
@Component
public class CustomWebResponseExceptionTranslator implements WebResponseExceptionTranslator {

    private final DefaultWebResponseExceptionTranslator delegate = new DefaultWebResponseExceptionTranslator();

    @Override
    public ResponseEntity<OAuth2Exception> translate(Exception e) throws Exception {
        SsoException ssoException = unwrapSsoException(e);
        if (ssoException == null) {
            // Not an SSO failure: preserve the deployed error contract exactly.
            return delegate.translate(e);
        }

        log.warn("SSO authentication error [{}]: {}", ssoException.getErrorCode(), ssoException.getMessage());
        OAuth2Exception oAuth2Exception = new OAuth2Exception(ssoException.getMessage()) {
            @Override
            public String getOAuth2ErrorCode() {
                return ssoException.getErrorCode();
            }
        };

        // Token-endpoint error responses must not be cached; matches what the default translator emits.
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CACHE_CONTROL, "no-store");
        headers.set(HttpHeaders.PRAGMA, "no-cache");

        return ResponseEntity.status(ssoException.getHttpStatus().value())
                .headers(headers)
                .body(oAuth2Exception);
    }

    private SsoException unwrapSsoException(Throwable e) {
        Throwable current = e;
        int depth = 0;
        while (current != null && depth < 5) {
            if (current instanceof SsoException) {
                return (SsoException) current;
            }
            current = current.getCause();
            depth++;
        }
        return null;
    }
}
