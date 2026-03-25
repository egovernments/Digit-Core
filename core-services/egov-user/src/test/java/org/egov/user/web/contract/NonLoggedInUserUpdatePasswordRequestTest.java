package org.egov.user.web.contract;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class NonLoggedInUserUpdatePasswordRequestTest {

    @Test
    public void test_should_map_from_contract_to_domain() {
        final NonLoggedInUserUpdatePasswordRequest request = NonLoggedInUserUpdatePasswordRequest.builder()
                .newPassword("newPassword")
                .userName("userName")
                .otpReference("otpReference")
                .tenantId("tenant")
                .build();

        final org.egov.user.domain.model.NonLoggedInUserUpdatePasswordRequest domain = request.toDomain();

        assertNotNull(domain);
        assertEquals("userName", domain.getUserName());
        assertEquals("newPassword", domain.getNewPassword());
        assertEquals("otpReference", domain.getOtpReference());
        assertEquals("tenant", domain.getTenantId());
    }

}