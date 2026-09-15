package org.egov.user.web.controller;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.user.security.oauth2.custom.jwt.IDPJwtValidator;
import org.egov.user.web.contract.SsoCacheClearRequest;
import org.egov.user.web.contract.factory.ResponseInfoFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class SsoCacheAdminControllerTest {

    @Mock
    private IDPJwtValidator idpJwtValidator;
    
    @Mock
    private ResponseInfoFactory responseInfoFactory;

    @InjectMocks
    private SsoCacheAdminController ssoCacheAdminController;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(ssoCacheAdminController).build();
        when(responseInfoFactory.createResponseInfoFromRequestInfo(any(RequestInfo.class), eq(true)))
                .thenReturn(new ResponseInfo("apiId", "ver", System.currentTimeMillis(), "resMsgId", "msgId", "successful"));
    }

    @Test
    public void testClearDecodersForTenantAndProvider_WithProviderId_Success() throws Exception {
        String tenantId = "pb.amritsar";
        String providerId = "azure";
        RequestInfo requestInfo = new RequestInfo();
        
        SsoCacheClearRequest request = new SsoCacheClearRequest();
        request.setRequestInfo(requestInfo);
        request.setTenantId(tenantId);
        request.setProviderId(providerId);
        
        // Verify the method call
        ssoCacheAdminController.clearDecodersForTenantAndProvider(request);
        
        verify(idpJwtValidator, times(1)).clearDecoderCacheFor(tenantId, providerId);
    }

    @Test
    public void testClearDecodersForTenantAndProvider_WithoutProviderId_Success() throws Exception {
        String tenantId = "pb.amritsar";
        RequestInfo requestInfo = new RequestInfo();
        
        SsoCacheClearRequest request = new SsoCacheClearRequest();
        request.setRequestInfo(requestInfo);
        request.setTenantId(tenantId);
        request.setProviderId(null);
        
        // Verify the method call with null providerId
        ssoCacheAdminController.clearDecodersForTenantAndProvider(request);
        
        verify(idpJwtValidator, times(1)).clearDecoderCacheFor(tenantId, null);
    }

    @Test
    public void testClearDecodersForTenantAndProvider_WithProviderId_ReturnsCorrectResponse() {
        String tenantId = "pb.amritsar";
        String providerId = "azure";
        RequestInfo requestInfo = new RequestInfo();
        
        SsoCacheClearRequest request = new SsoCacheClearRequest();
        request.setRequestInfo(requestInfo);
        request.setTenantId(tenantId);
        request.setProviderId(providerId);
        
        // Call the method and verify response
        ResponseInfo response = ssoCacheAdminController.clearDecodersForTenantAndProvider(request);
        
        assertNotNull(response);
        assertEquals("successful", response.getStatus());
    }

    @Test
    public void testClearDecodersForTenantAndProvider_WithoutProviderId_ReturnsCorrectResponse() {
        String tenantId = "pb.amritsar";
        RequestInfo requestInfo = new RequestInfo();
        
        SsoCacheClearRequest request = new SsoCacheClearRequest();
        request.setRequestInfo(requestInfo);
        request.setTenantId(tenantId);
        request.setProviderId(null);
        
        // Call the method and verify response
        ResponseInfo response = ssoCacheAdminController.clearDecodersForTenantAndProvider(request);
        
        assertNotNull(response);
        assertEquals("successful", response.getStatus());
    }

    @Test
    public void testClearDecodersForTenantAndProvider_Endpoint_WithoutProviderId_Success() throws Exception {
        String tenantId = "pb.amritsar";
        RequestInfo requestInfo = new RequestInfo();
        
        SsoCacheClearRequest request = new SsoCacheClearRequest();
        request.setRequestInfo(requestInfo);
        request.setTenantId(tenantId);
        
        mockMvc.perform(post("/sso/decoders/_clear")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"RequestInfo\":{},\"tenantId\":\"" + tenantId + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("successful"));
        
        verify(idpJwtValidator, times(1)).clearDecoderCacheFor(tenantId, null);
    }

}
