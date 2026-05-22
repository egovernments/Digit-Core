package org.egov.payment.service.gateways.paytm;

import org.egov.gateway.spi.GatewayProviderConfig;
import org.egov.gateway.spi.GatewayProviderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class PaytmGatewayProviderFactoryTest {

    private GatewayProviderFactory factory;

    @BeforeEach
    void setUp() {
        factory = new PaytmGatewayProviderFactory();
    }

    @Test
    void gatewayIdShouldBePaytm() {
        assertEquals("paytm", factory.getGatewayId());
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

        assertTrue(keys.contains("paytm.merchant.id"), "Must declare paytm.merchant.id");
        assertTrue(keys.contains("paytm.merchant.secret.key"), "Must declare paytm.merchant.secret.key");
        assertTrue(keys.contains("paytm.url.debit"), "Must declare paytm.url.debit");
        assertTrue(keys.contains("paytm.url.status"), "Must declare paytm.url.status");
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
                "paytm.merchant.id", "TEST_MID",
                "paytm.merchant.secret.key", "TEST_SECRET_KEY",
                "paytm.merchant.industry.type", "Retail",
                "paytm.merchant.channel.id", "WEB",
                "paytm.merchant.website", "WEBSTAGING",
                "paytm.url.debit", "https://securegw-stage.paytm.in/theia/processTransaction",
                "paytm.url.status", "https://securegw-stage.paytm.in/merchant-status/getTxnStatus"
        );

        var provider = factory.create(config);
        assertNotNull(provider);
        assertEquals("paytm", provider.getGatewayId());
    }

    @Test
    void configPropertiesShouldNotBeEmpty() {
        assertFalse(factory.getConfigProperties().isEmpty());
    }
}
