package org.egov.user.web.controller;

import org.egov.user.domain.exception.sso.SsoMissingParamException;
import org.egov.user.domain.model.UserTenantMapping;
import org.egov.user.domain.service.TenantLookupService;
import org.egov.user.web.contract.auth.TenantLookupResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class TenantLookupControllerTest {

    @Mock
    private TenantLookupService tenantLookupService;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        TenantLookupController controller = new TenantLookupController(tenantLookupService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    public void tenants_success_returns200WithBodyShape() throws Exception {
        TenantLookupResponse response = TenantLookupResponse.builder()
                .username("jdoe")
                .tenants(Collections.singletonList(
                        UserTenantMapping.builder().tenantId("pb.amritsar").userId(1L).uuid("uuid-1").build()))
                .build();
        when(tenantLookupService.lookup(anyString(), anyString())).thenReturn(response);

        mockMvc.perform(post("/oauth/tenants")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("assertion", "jwt-assertion")
                        .param("tenantId", "shared.tenant"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("jdoe"))
                .andExpect(jsonPath("$.tenants[0].tenantId").value("pb.amritsar"))
                .andExpect(jsonPath("$.tenants[0].userId").value(1));
    }

    @Test
    public void tenants_ssoException_returnsStatusAndErrorCodeFromException() throws Exception {
        when(tenantLookupService.lookup(anyString(), any()))
                .thenThrow(SsoMissingParamException.tenantNotShared("wrong-tenant"));

        mockMvc.perform(post("/oauth/tenants")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("assertion", "jwt-assertion")
                        .param("tenantId", "wrong-tenant"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("sso.param.tenant_not_shared"))
                .andExpect(jsonPath("$.error_description").value("tenantId must be the shared login tenant"));
    }
}
