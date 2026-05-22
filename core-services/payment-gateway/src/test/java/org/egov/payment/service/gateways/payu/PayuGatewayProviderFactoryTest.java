package org.egov.payment.service.gateways.payu;

import org.egov.gateway.spi.GatewayProviderConfig;
import org.egov.gateway.spi.GatewayProviderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class PayuGatewayProviderFactoryTest {

    private GatewayProviderFactory factory;

    @BeforeEach
    void setUp() {
        factory = new PayuGatewayProviderFactory();
    }

    @Test
    void gatewayIdShouldBePayu() {
        assertEquals("payu", factory.getGatewayId());
    }

    @Test
    void displayNameShouldBeNonEmpty() {
        assertNotNull(factory.getDisplayName());
        assertFalse(factory.getDisplayName().isBlank());
    }

    @Test
    void versionShouldBeNonEmpty() {
        assertNotNull(factory.getVersion());
        assertFalse(factory.getVersion().isBlank());
    }

    @Test
    void configPropertiesShouldContainRequiredKeys() {
        List<GatewayProviderConfig> configs = factory.getConfigProperties();
        List<String> keys = configs.stream().map(GatewayProviderConfig::getKey).collect(Collectors.toList());

        assertTrue(keys.contains("payu.merchant.key"), "Must declare payu.merchant.key");
        assertTrue(keys.contains("payu.merchant.salt"), "Must declare payu.merchant.salt");
        assertTrue(keys.contains("payu.url"), "Must declare payu.url");
        assertTrue(keys.contains("payu.url.status"), "Must declare payu.url.status");
    }

    @Test
    void allConfigKeysShouldBeUnique() {
        List<GatewayProviderConfig> configs = factory.getConfigProperties();
        long distinctCount = configs.stream().map(GatewayProviderConfig::getKey).distinct().count();
        assertEquals(configs.size(), distinctCount, "Config keys must be unique");
    }

    @Test
    void createShouldReturnProviderForValidConfig() {
        Map<String, String> config = Map.of(
                "payu.merchant.key", "TEST_KEY",
                "payu.merchant.salt", "TEST_SALT",
                "payu.url", "test.payu.in",
                "payu.url.status", "test.payu.in",
                "payu.path.pay", "_payment",
                "payu.path.status", "merchant/postservice.php"
        );

        var provider = factory.create(config);
        assertNotNull(provider);
        assertEquals("payu", provider.getGatewayId());
    }

    @Test
    void configPropertiesShouldNotBeEmpty() {
        assertFalse(factory.getConfigProperties().isEmpty());
    }
}
