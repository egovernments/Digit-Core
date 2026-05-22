package org.egov.payment.service.gateways.payu;

import org.egov.gateway.spi.GatewayProvider;
import org.egov.gateway.spi.GatewayProviderConfig;
import org.egov.gateway.spi.GatewayProviderFactory;

import java.util.List;
import java.util.Map;

/**
 * ServiceLoader-discovered factory for the PayU gateway.
 * No Spring annotations.
 */
public class PayuGatewayProviderFactory implements GatewayProviderFactory {

    @Override
    public String getGatewayId() {
        return "payu";
    }

    @Override
    public String getDisplayName() {
        return "PayU";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public List<GatewayProviderConfig> getConfigProperties() {
        return List.of(
                GatewayProviderConfig.of("payu.active", "Enable/disable this gateway", GatewayProviderConfig.Type.BOOLEAN, true, "false"),
                GatewayProviderConfig.of("payu.merchant.key", "PayU merchant key", GatewayProviderConfig.Type.STRING, true),
                GatewayProviderConfig.of("payu.merchant.salt", "PayU merchant SALT", GatewayProviderConfig.Type.SECRET, true),
                GatewayProviderConfig.of("payu.url", "PayU payment host", GatewayProviderConfig.Type.STRING, true, "test.payu.in"),
                GatewayProviderConfig.of("payu.url.status", "PayU status host", GatewayProviderConfig.Type.STRING, true, "test.payu.in"),
                GatewayProviderConfig.of("payu.path.pay", "PayU payment path", GatewayProviderConfig.Type.STRING, true, "_payment"),
                GatewayProviderConfig.of("payu.path.status", "PayU status path", GatewayProviderConfig.Type.STRING, true, "merchant/postservice.php")
        );
    }

    @Override
    public GatewayProvider create(Map<String, String> config) {
        return new PayuGatewayProvider(config);
    }
}
