package org.egov.user.security.oauth2.custom;

import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.egov.user.domain.exception.sso.SsoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.common.exceptions.InvalidRequestException;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.error.WebResponseExceptionTranslator;
import org.springframework.stereotype.Component;

import static org.egov.user.config.UserServiceConstants.INVALID_TENANT_ID_ERR_CODE;

/**
 * Translates exceptions from the OAuth2 token endpoint into structured OAuth2Exception responses
 * where the error code is the machine-readable SSO error code and the description is the
 * human-readable message.
 *
 * <p>Frontend receives:
 * <pre>
 * { "error": "sso.jwt.invalid", "error_description": "JWT validation failed: ..." }
 * </pre>
 */
@Slf4j
@Component
@SuppressWarnings("rawtypes")
public class CustomWebResponseExceptionTranslator implements WebResponseExceptionTranslator {

    private static final String OAUTH2_ERROR_ACCESS_DENIED = "access_denied";
    private static final String OAUTH2_ERROR_SERVER_ERROR = "server_error";
    private static final String MSG_UNEXPECTED_ERROR = "An unexpected error occurred. Please try again.";

    @Override
    public ResponseEntity translate(Exception e) {
        SsoException ssoException = unwrapSsoException(e);
        if (ssoException != null) {
            log.warn("SSO authentication error [{}]: {}", ssoException.getErrorCode(), ssoException.getMessage());
            OAuth2Exception oAuth2Exception = new OAuth2Exception(ssoException.getMessage()) {
                @Override
                public String getOAuth2ErrorCode() {
                    return ssoException.getErrorCode();
                }
            };
            int statusCode = ssoException.getHttpStatus().value();
            return ResponseEntity.status(statusCode).body(oAuth2Exception);
        }

        if (e instanceof OAuth2Exception) {
            OAuth2Exception oAuth2Exception = (OAuth2Exception) e;
            log.warn("OAuth2 error [{}]: {}", oAuth2Exception.getOAuth2ErrorCode(), oAuth2Exception.getMessage());
            return ResponseEntity.status(oAuth2Exception.getHttpErrorCode()).body(oAuth2Exception);
        }

        if (e instanceof AuthenticationException) {
            log.warn("Authentication error: {}", e.getMessage());
            OAuth2Exception oAuth2Exception = new InvalidGrantException(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(oAuth2Exception);
        }

        if (e instanceof AccessDeniedException) {
            log.warn("Access denied: {}", e.getMessage());
            OAuth2Exception oAuth2Exception = new OAuth2Exception(e.getMessage()) {
                @Override
                public String getOAuth2ErrorCode() {
                    return OAUTH2_ERROR_ACCESS_DENIED;
                }
            };
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(oAuth2Exception);
        }

        if (e instanceof CustomException && INVALID_TENANT_ID_ERR_CODE.equals(((CustomException) e).getCode())) {
            log.warn("Invalid tenantId on token request");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new InvalidRequestException("Invalid tenantId"));
        }

        log.error("Unexpected error during token request", e);
        OAuth2Exception serverError = new OAuth2Exception(MSG_UNEXPECTED_ERROR) {
            @Override
            public String getOAuth2ErrorCode() {
                return OAUTH2_ERROR_SERVER_ERROR;
            }
        };
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(serverError);
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
