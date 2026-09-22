package org.egov.user.domain.service.utils;

import org.egov.encryption.EncryptionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EncryptionDecryptionUtilTenantMappingKeyTest {

    @Mock
    private EncryptionService encryptionService;

    private EncryptionDecryptionUtil util;

    @Before
    public void setUp() {
        util = new EncryptionDecryptionUtil(encryptionService);
    }

    @Test
    public void blankSharedTenant_returnsNullWithoutCallingEncryption() {
        ReflectionTestUtils.setField(util, "sharedLoginTenantId", "");
        assertNull(util.tenantMappingKey("jdoe"));
        verifyZeroInteractions(encryptionService);
    }

    @Test
    public void blankUsername_returnsNull() {
        ReflectionTestUtils.setField(util, "sharedLoginTenantId", "sh");
        assertNull(util.tenantMappingKey(" "));
        verifyZeroInteractions(encryptionService);
    }

    @Test
    public void encryptsTrimmedUsernameWithSharedTenantViaUserModel() throws Exception {
        ReflectionTestUtils.setField(util, "sharedLoginTenantId", "sh");
        com.fasterxml.jackson.databind.node.ObjectNode out = new com.fasterxml.jackson.databind.ObjectMapper().createObjectNode();
        out.put("username", "k|abc");
        when(encryptionService.encryptJson(java.util.Collections.singletonMap("username", "jdoe"), "User", "sh")).thenReturn(out);
        assertEquals("k|abc", util.tenantMappingKey(" jdoe "));
    }
}
