package org.egov.user.domain.exception.sso;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a JWT token replay is detected.
 * 
 * <p>This security exception prevents the same JWT token from being used multiple times
 * for OAuth2 token exchange, which could lead to unauthorized access. Token replay attacks
 * are mitigated by tracking used token IDs in the database and rejecting any subsequent
 * attempts to use the same token.</p>
 * 
 * <p>The exception is thrown when:</p>
 * <ul>
 *   <li>A token ID already exists in the user_idp_details table</li>
 *   <li>Database constraints prevent duplicate token usage</li>
 *   <li>Application-level replay detection identifies a reused token</li>
 * </ul>
 * 
 * <p>Results in HTTP 401 Unauthorized response to prevent potential security breaches.</p>
 */
public class TokenReplayException extends SsoException {
    
    private static final String ERROR_CODE = "token_replay";
    private static final HttpStatus HTTP_STATUS = HttpStatus.UNAUTHORIZED;
    
    public TokenReplayException(String tokenId) {
        super(ERROR_CODE, 
            "Token replay detected: tokenId '" + tokenId + "' has already been used", 
            HTTP_STATUS);
    }
}
