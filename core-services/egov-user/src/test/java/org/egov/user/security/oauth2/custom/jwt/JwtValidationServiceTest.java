package org.egov.user.security.oauth2.custom.jwt;

import org.egov.user.domain.exception.sso.IdpJwtValidationException;
import org.junit.Test;

import java.util.Base64;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JwtValidationServiceTest {

    private static String token(String iss) {
        Base64.Encoder b = Base64.getUrlEncoder().withoutPadding();
        return b.encodeToString("{\"alg\":\"RS256\"}".getBytes()) + "."
                + b.encodeToString(("{\"iss\":\"" + iss + "\"}").getBytes()) + ".x";
    }

    private static String errorCode(JwtValidator validator, String iss) {
        try {
            new JwtValidationService(Collections.singletonList(validator)).validate(token(iss), "pb");
            fail("Expected IdpJwtValidationException");
            return null;
        } catch (IdpJwtValidationException e) {
            return e.getErrorCode();
        }
    }

    @Test
    public void validate_DisabledIssuer_IdpDisabled() {
        JwtValidator validator = mock(JwtValidator.class);
        when(validator.supports(anyString())).thenReturn(false);
        when(validator.isDisabled("https://off")).thenReturn(true);

        assertEquals(SsoErrorCodes.IDP_DISABLED, errorCode(validator, "https://off"));
    }

    @Test
    public void validate_UnknownIssuer_JwtInvalid() {
        JwtValidator validator = mock(JwtValidator.class);
        when(validator.supports(anyString())).thenReturn(false);
        when(validator.isDisabled(anyString())).thenReturn(false);

        assertEquals(SsoErrorCodes.JWT_INVALID, errorCode(validator, "https://unknown"));
    }
}
