package org.egov.user.config;

import org.egov.user.config.AuthProperties.Provider;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AuthPropertiesTest {

    @Test
    public void builder_defaults_usernameClaimKeyAndJitEnabled() {
        Provider provider = Provider.builder().id("azure").build();

        assertEquals(OidcConfigConstants.DEFAULT_USERNAME_CLAIM_KEY, provider.getUsernameClaimKey());
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

        assertEquals(OidcConfigConstants.DEFAULT_USERNAME_CLAIM_KEY, provider.getUsernameClaimKey());
        assertFalse(provider.isJitEnabled());
    }
}
