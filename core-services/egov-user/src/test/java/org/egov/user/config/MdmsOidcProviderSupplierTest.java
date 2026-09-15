package org.egov.user.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.user.config.AuthProperties.Provider;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MdmsOidcProviderSupplierTest {

    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;

    @Before
    public void setup() {
        restTemplate = mock(RestTemplate.class);
        objectMapper = new ObjectMapper();
    }

    @Test
    public void getProviders_EmptyTenantConfig_ReturnsEmptyList() {
        MdmsOidcProviderSupplier supplier = new MdmsOidcProviderSupplier(
                restTemplate,
                "http://mdms",
                "/mdms/search",
                "module",
                "master",
                "",
                1000L);

        List<Provider> providers = supplier.getProviders();

        assertNotNull(providers);
        assertTrue(providers.isEmpty());
    }

    @Test
    public void getProviders_MdmsReturnsValidJson_ParsesProviders() throws Exception {
        MdmsOidcProviderSupplier supplier = new MdmsOidcProviderSupplier(
                restTemplate,
                "http://mdms",
                "/mdms/search",
                "module",
                "master",
                "pb",
                1000L);

        String mdmsJson = "{\n" +
                "  \"" + OidcConfigConstants.MDMS_RES + "\": {\n" +
                "    \"module\": {\n" +
                "      \"master\": [\n" +
                "        {\n" +
                "          \"" + OidcConfigConstants.KEY_ID + "\": \"azure\",\n" +
                "          \"" + OidcConfigConstants.KEY_ISSUER_URI + "\": \"https://sts.windows.net/tenant-id/\",\n" +
                "          \"" + OidcConfigConstants.KEY_JWK_SET_URI + "\": \"https://sts.windows.net/tenant-id/discovery/keys\",\n" +
                "          \"" + OidcConfigConstants.KEY_AUDIENCES + "\": [\"aud1\"],\n" +
                "          \"" + OidcConfigConstants.KEY_TENANT_ID + "\": \"pb\",\n" +
                "          \"" + OidcConfigConstants.KEY_USER_TYPE + "\": \"EMPLOYEE\",\n" +
                "          \"" + OidcConfigConstants.KEY_ACTIVE + "\": true\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "}";
        JsonNode node = objectMapper.readTree(mdmsJson);
        when(restTemplate.postForObject(eq("http://mdms/mdms/search"), any(Object.class), eq(JsonNode.class)))
                .thenReturn(node);

        List<Provider> providers = supplier.getProviders();

        assertEquals(1, providers.size());
        Provider p = providers.get(0);
        assertEquals("azure", p.getId());
        assertEquals("https://sts.windows.net/tenant-id/", p.getIssuerUri());
        assertEquals(Collections.singletonList("aud1"), p.getAudiences());
        assertEquals("pb", p.getTenantId());
        assertEquals("EMPLOYEE", p.getUserType());
    }

    @Test
    public void getProviders_MdmsReturnsNull_KeepsPreviousCache() throws Exception {
        MdmsOidcProviderSupplier supplier = new MdmsOidcProviderSupplier(
                restTemplate,
                "http://mdms",
                "/mdms/search",
                "module",
                "master",
                "pb",
                10L);

        Provider provider = Provider.builder()
                .id("cached")
                .issuerUri("https://cached")
                .jwkSetUri("https://keys")
                .build();
        Class<?> cacheEntryClass = Class.forName("org.egov.user.config.MdmsOidcProviderSupplier$CacheEntry");
        Object cacheEntry = cacheEntryClass
                .getDeclaredConstructor(List.class, long.class)
                .newInstance(Collections.singletonList(provider), 0L);
        AtomicReference<?> ref = new AtomicReference<>(cacheEntry);
        java.lang.reflect.Field cacheField = MdmsOidcProviderSupplier.class.getDeclaredField("cache");
        cacheField.setAccessible(true);
        cacheField.set(supplier, ref);

        when(restTemplate.postForObject(eq("http://mdms/mdms/search"), any(Object.class), eq(JsonNode.class)))
                .thenReturn(null);

        List<Provider> providers = supplier.getProviders();

        assertEquals(1, providers.size());
        assertEquals("cached", providers.get(0).getId());
    }
}

