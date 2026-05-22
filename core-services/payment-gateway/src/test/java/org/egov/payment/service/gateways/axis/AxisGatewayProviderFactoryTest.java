package org.egov.payment.service.gateways.axis;

import org.egov.gateway.spi.GatewayProviderConfig;
import org.egov.gateway.spi.GatewayProviderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Map.entry;

import static org.junit.jupiter.api.Assertions.*;

class AxisGatewayProviderFactoryTest {

    private GatewayProviderFactory factory;

    @BeforeEach
    void setUp() {
        factory = new AxisGatewayProviderFactory();
    }

    @Test
    void gatewayIdShouldBeAxis() {
        assertEquals("axis", factory.getGatewayId());
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

        assertTrue(keys.contains("axis.merchant.id"), "Must declare axis.merchant.id");
        assertTrue(keys.contains("axis.merchant.secret.key"), "Must declare axis.merchant.secret.key");
        assertTrue(keys.contains("axis.url.debit"), "Must declare axis.url.debit");
        assertTrue(keys.contains("axis.url.status"), "Must declare axis.url.status");
    }

    @Test
    void allConfigKeysShouldBeUnique() {
        List<GatewayProviderConfig> configs = factory.getConfigProperties();
        long distinctCount = configs.stream().map(GatewayProviderConfig::getKey).distinct().count();
        assertEquals(configs.size(), distinctCount, "Config keys must be unique");
    }

    @Test
    void createShouldReturnProviderForValidConfig() {
        // Provide all required config keys — Map.of() only allows up to 10 pairs, use Map.ofEntries()
        Map<String, String> config = Map.ofEntries(
                entry("axis.url.debit", "https://migs.mastercard.com.au/vpcpay"),
                entry("axis.url.status", "https://migs.mastercard.com.au/vpcdps"),
                entry("axis.merchant.id", "TEST_MERCHANT"),
                entry("axis.merchant.secret.key", "0102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f20"),
                entry("axis.merchant.user", "test_user"),
                entry("axis.merchant.pwd", "test_pwd"),
                entry("axis.merchant.access.code", "ACCESS123"),
                entry("axis.merchant.vpc.version", "1"),
                entry("axis.merchant.vpc.command.pay", "pay"),
                entry("axis.merchant.vpc.command.status", "queryDR"),
                entry("axis.locale", "en_IN"),
                entry("axis.currency", "INR")
        );

        var provider = factory.create(config);
        assertNotNull(provider);
        assertEquals("axis", provider.getGatewayId());
    }

    @Test
    void configPropertiesShouldNotBeEmpty() {
        assertFalse(factory.getConfigProperties().isEmpty());
    }
}
