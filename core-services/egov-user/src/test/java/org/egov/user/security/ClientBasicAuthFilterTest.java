package org.egov.user.security;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.NoSuchClientException;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ClientBasicAuthFilterTest {

    @Mock
    private ClientDetailsService clientDetailsService;

    private ClientBasicAuthFilter filter;

    @Before
    public void setup() {
        filter = new ClientBasicAuthFilter(clientDetailsService);
    }

    private String basicHeader(String clientId, String secret) {
        String raw = clientId + ":" + secret;
        return "Basic " + Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void noHeader_returns401() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/user/oauth/tenants");
        request.setServletPath("/oauth/tenants");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals(401, response.getStatus());
        assertNull(chain.getRequest());
    }

    @Test
    public void unknownClient_returns401() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/user/oauth/tenants");
        request.setServletPath("/oauth/tenants");
        request.addHeader("Authorization", basicHeader("bad-client", "secret"));
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(clientDetailsService.loadClientByClientId("bad-client"))
                .thenThrow(new NoSuchClientException("no such client"));

        filter.doFilter(request, response, chain);

        assertEquals(401, response.getStatus());
        assertNull(chain.getRequest());
    }

    @Test
    public void knownClient_blankSecret_passesChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/user/oauth/tenants");
        request.setServletPath("/oauth/tenants");
        request.addHeader("Authorization", basicHeader("known-client", ""));
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        BaseClientDetails clientDetails = new BaseClientDetails();
        clientDetails.setClientId("known-client");
        when(clientDetailsService.loadClientByClientId("known-client")).thenReturn(clientDetails);

        filter.doFilter(request, response, chain);

        assertEquals(request, chain.getRequest());
    }

    @Test
    public void nonTenantsPath_chainUntouched() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/user/users/_search");
        request.setServletPath("/users/_search");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals(request, chain.getRequest());
        assertEquals(200, response.getStatus());
    }
}
