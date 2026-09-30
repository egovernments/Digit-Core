package org.egov.user.config;

import org.egov.user.config.AuthProperties.Provider;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AuthPropertiesTest {

    @Test
    public void builder_defaults_usernameClaimKeyAndJitEnabled() {
        Provider provider = Provider.builder().id("azure").build();

        assertEquals(null, provider.getUsernameClaimKey());
        assertFalse(provider.isJitEnabled());
    }

    @Test
    public void builder_explicitValues_retained() {
        Provider provider = Provider.builder()
                .id("azure")
                .usernameClaimKey("upn")
                .jitEnabled(true)
                .build();

        assertEquals("upn", provider.getUsernameClaimKey());
        assertTrue(provider.isJitEnabled());
    }

    @Test
    public void noArgConstructor_defaults_usernameClaimKeyAndJitEnabled() {
        Provider provider = new Provider();

        assertEquals(null, provider.getUsernameClaimKey());
        assertFalse(provider.isJitEnabled());
    }

    @Test
    public void resolveUsername_configuredKeyPresent_usesIt() {
        Provider provider = Provider.builder().usernameClaimKey("upn").build();
        Map<String, Object> claims = claims("upn", "jane", "unique_name", "jane@unique", "email", "jane@email");

        assertEquals("jane", provider.resolveUsername(claims));
    }

    @Test
    public void resolveUsername_configuredKeyAbsent_returnsNull() {
        Provider provider = Provider.builder().usernameClaimKey("upn").build();
        Map<String, Object> claims = claims("unique_name", "jane@unique", "email", "jane@email");

        assertNull(provider.resolveUsername(claims));
    }

    @Test
    public void resolveUsername_unconfigured_fallsBackToUniqueNameThenEmail() {
        Provider provider = Provider.builder().build();

        assertEquals("jane@unique", provider.resolveUsername(claims("unique_name", "jane@unique", "email", "jane@email")));
        assertEquals("jane@email", provider.resolveUsername(claims("email", "jane@email")));
        assertNull(provider.resolveUsername(claims()));
    }

    @Test
    public void resolveEmail_configuredKeyPresent_usesIt() {
        Provider provider = Provider.builder().emailClaimKey("mail").build();
        Map<String, Object> claims = claims("mail", "jane@corp.com", "unique_name", "jane@unique");

        assertEquals("jane@corp.com", provider.resolveEmail(claims));
    }

    @Test
    public void resolveEmail_configuredKeyAbsent_returnsNull() {
        Provider provider = Provider.builder().emailClaimKey("mail").build();

        assertNull(provider.resolveEmail(claims("unique_name", "jane@unique")));
    }

    @Test
    public void resolveEmail_unconfigured_fallsBackToUniqueNameThenEmail() {
        Provider provider = Provider.builder().build();

        assertEquals("jane@unique", provider.resolveEmail(claims("unique_name", "jane@unique", "email", "jane@email")));
        assertEquals("jane@email", provider.resolveEmail(claims("email", "jane@email")));
    }

    @Test
    public void resolveName_configuredKeyPresent_usesIt() {
        Provider provider = Provider.builder().nameClaimKey("displayName").build();

        assertEquals("Jane Doe", provider.resolveName(claims("displayName", "Jane Doe", "name", "Other")));
    }

    @Test
    public void resolveName_configuredKeyAbsent_returnsNull() {
        Provider provider = Provider.builder().nameClaimKey("displayName").build();

        assertNull(provider.resolveName(claims("name", "Other")));
    }

    @Test
    public void resolveName_unconfigured_fallsBackToNameThenSub() {
        Provider provider = Provider.builder().build();

        assertEquals("Jane Doe", provider.resolveName(claims("name", "Jane Doe", "sub", "sub-123")));
        assertEquals("sub-123", provider.resolveName(claims("sub", "sub-123")));
    }

    @Test
    public void resolveMobileNumber_configuredKeyPresent_usesIt() {
        Provider provider = Provider.builder().mobileNumberClaimKey("phone").build();

        assertEquals("9999999999", provider.resolveMobileNumber(claims("phone", "9999999999")));
    }

    @Test
    public void resolveMobileNumber_unconfigured_returnsNull() {
        Provider provider = Provider.builder().build();

        assertNull(provider.resolveMobileNumber(claims("phone", "9999999999")));
    }

    private static Map<String, Object> claims(String... keyValuePairs) {
        Map<String, Object> claims = new HashMap<>();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            claims.put(keyValuePairs[i], keyValuePairs[i + 1]);
        }
        return claims;
    }
}
