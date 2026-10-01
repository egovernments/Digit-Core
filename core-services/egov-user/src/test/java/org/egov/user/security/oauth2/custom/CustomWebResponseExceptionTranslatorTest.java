package org.egov.user.security.oauth2.custom;

import org.egov.tracer.model.CustomException;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;

import static org.junit.Assert.*;

public class CustomWebResponseExceptionTranslatorTest {

    private final CustomWebResponseExceptionTranslator translator = new CustomWebResponseExceptionTranslator();

    @Test
    public void invalidTenantIdMapsTo400WithoutEcho() {
        ResponseEntity response = translator.translate(new CustomException("INVALID_TENANT_ID", "Invalid tenantId"));

        assertEquals(400, response.getStatusCodeValue());
        OAuth2Exception body = (OAuth2Exception) response.getBody();
        assertEquals("invalid_request", body.getOAuth2ErrorCode());
        assertEquals("Invalid tenantId", body.getMessage());
    }

    @Test
    public void otherCustomExceptionStaysGenericServerError() {
        ResponseEntity response = translator.translate(new CustomException("SOME_CODE", "SELECT * FROM eg_user"));

        assertEquals(500, response.getStatusCodeValue());
        assertFalse(((OAuth2Exception) response.getBody()).getMessage().contains("SELECT"));
    }
}
