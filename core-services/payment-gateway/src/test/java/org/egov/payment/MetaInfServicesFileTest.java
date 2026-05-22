package org.egov.payment;

import org.egov.gateway.spi.GatewayProviderFactory;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that the META-INF/services file is present and lists all 4 gateway factories,
 * and that ServiceLoader can discover them.
 */
class MetaInfServicesFileTest {

    @Test
    void metaInfServicesFileShouldListAllFourFactories() throws Exception {
        InputStream is = MetaInfServicesFileTest.class.getResourceAsStream(
                "/META-INF/services/org.egov.gateway.spi.GatewayProviderFactory");
        assertNotNull(is, "META-INF/services file must exist on classpath");

        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                    lines.add(trimmed);
                }
            }
        }

        assertEquals(4, lines.size(), "Should have exactly 4 factory entries");
        assertTrue(lines.contains("org.egov.payment.service.gateways.axis.AxisGatewayProviderFactory"));
        assertTrue(lines.contains("org.egov.payment.service.gateways.paytm.PaytmGatewayProviderFactory"));
        assertTrue(lines.contains("org.egov.payment.service.gateways.phonepe.PhonepeGatewayProviderFactory"));
        assertTrue(lines.contains("org.egov.payment.service.gateways.payu.PayuGatewayProviderFactory"));
    }

    @Test
    void serviceLoaderShouldDiscoverAllFourFactories() {
        ServiceLoader<GatewayProviderFactory> loader = ServiceLoader.load(GatewayProviderFactory.class);
        List<GatewayProviderFactory> factories = new ArrayList<>();
        loader.forEach(factories::add);

        assertEquals(4, factories.size(), "ServiceLoader should find 4 factories");

        List<String> ids = factories.stream().map(GatewayProviderFactory::getGatewayId).toList();
        assertTrue(ids.contains("axis"));
        assertTrue(ids.contains("paytm"));
        assertTrue(ids.contains("phonepe"));
        assertTrue(ids.contains("payu"));
    }

    @Test
    void allFactoriesShouldHaveUniqueIds() {
        ServiceLoader<GatewayProviderFactory> loader = ServiceLoader.load(GatewayProviderFactory.class);
        List<String> ids = new ArrayList<>();
        loader.forEach(f -> ids.add(f.getGatewayId()));

        long distinctCount = ids.stream().distinct().count();
        assertEquals(ids.size(), distinctCount, "All gateway IDs must be unique");
    }
}
