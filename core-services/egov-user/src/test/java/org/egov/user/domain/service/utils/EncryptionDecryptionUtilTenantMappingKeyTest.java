package org.egov.user.domain.service.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.egov.encryption.EncryptionService;
import org.egov.encryption.config.EncProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * The mapping key is encrypted with the tenant the encryption client uses, not with the
 * shared-login tenant: that tenant is common to every tenant, so one username produces one
 * key everywhere, and egov-enc-service always holds keys for it.
 */
@RunWith(MockitoJUnitRunner.class)
public class EncryptionDecryptionUtilTenantMappingKeyTest {

    private static final String STATE_TENANT = "pg";

    @Mock
    private EncryptionService encryptionService;

    @Mock
    private EncProperties encProperties;

    private EncryptionDecryptionUtil util;

    @Before
    public void setUp() {
        util = new EncryptionDecryptionUtil(encryptionService);
        ReflectionTestUtils.setField(util, "encProperties", encProperties);
    }

    @Test
    public void encryptsTheTrimmedUsernameWithTheEncClientStateTenant() throws Exception {
        when(encProperties.getStateLevelTenantId()).thenReturn(STATE_TENANT);
        ObjectNode out = new ObjectMapper().createObjectNode();
        out.put("username", "k|abc");
        when(encryptionService.encryptJson(Collections.singletonMap("username", "jdoe"), "User", STATE_TENANT))
                .thenReturn(out);

        assertEquals("k|abc", util.tenantMappingKey(" jdoe "));
    }

    @Test
    public void theKeyDoesNotDependOnTheUsersOwnTenant() throws Exception {
        when(encProperties.getStateLevelTenantId()).thenReturn(STATE_TENANT);
        ObjectNode out = new ObjectMapper().createObjectNode();
        out.put("username", "same-key");
        when(encryptionService.encryptJson(Collections.singletonMap("username", "jdoe"), "User", STATE_TENANT))
                .thenReturn(out);

        // called twice as it would be for a user present in two tenants
        assertEquals(util.tenantMappingKey("jdoe"), util.tenantMappingKey("jdoe"));
    }

    @Test
    public void blankUsernameReturnsNullWithoutCallingEncryption() {
        assertNull(util.tenantMappingKey(" "));
        verifyZeroInteractions(encryptionService);
    }

    @Test
    public void nullUsernameReturnsNullWithoutCallingEncryption() {
        assertNull(util.tenantMappingKey(null));
        verifyZeroInteractions(encryptionService);
    }
}
