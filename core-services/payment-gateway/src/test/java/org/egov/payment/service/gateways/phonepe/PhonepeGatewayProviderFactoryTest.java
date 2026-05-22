package org.egov.payment.service.gateways.phonepe;

import org.egov.gateway.spi.GatewayProviderConfig;
import org.egov.gateway.spi.GatewayProviderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class PhonepeGatewayProviderFactoryTest {

    private GatewayProviderFactory factory;

    @BeforeEach
    void setUp() {
        factory = new PhonepeGatewayProviderFactory();
    }

    @Test
    void gatewayIdShouldBePhonepe() {
        assertEquals("phonepe", factory.getGatewayId());
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

        assertTrue(keys.contains("phonepe.merchant.id"), "Must declare phonepe.merchant.id");
        assertTrue(keys.contains("phonepe.merchant.secret.key"), "Must declare phonepe.merchant.secret.key");
        assertTrue(keys.contains("phonepe.merchant.secret.index"), "Must declare phonepe.merchant.secret.index");
        assertTrue(keys.contains("phonepe.merchant.host"), "Must declare phonepe.merchant.host");
        assertTrue(keys.contains("phonepe.url.debit"), "Must declare phonepe.url.debit");
        assertTrue(keys.contains("phonepe.url.status"), "Must declare phonepe.url.status");
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
                "phonepe.merchant.id", "TESTMERCHANT",
                "phonepe.merchant.secret.key", "TEST_SALT",
                "phonepe.merchant.secret.index", "1",
                "phonepe.merchant.host", "mercury-uat.phonepe.com",
                "phonepe.url.debit", "/v3/debit",
                "phonepe.url.status", "/v3/transaction"
        );

        var provider = factory.create(config);
        assertNotNull(provider);
        assertEquals("phonepe", provider.getGatewayId());
    }

    @Test
    void configPropertiesShouldNotBeEmpty() {
        assertFalse(factory.getConfigProperties().isEmpty());
    }
}
