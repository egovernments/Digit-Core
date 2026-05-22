package org.egov.payment.service.gateways.phonepe;

import org.egov.gateway.spi.GatewayProvider;
import org.egov.gateway.spi.GatewayProviderConfig;
import org.egov.gateway.spi.GatewayProviderFactory;

import java.util.List;
import java.util.Map;

/**
 * ServiceLoader-discovered factory for the PhonePe gateway.
 * No Spring annotations.
 */
public class PhonepeGatewayProviderFactory implements GatewayProviderFactory {

    @Override
    public String getGatewayId() {
        return "phonepe";
    }

    @Override
    public String getDisplayName() {
        return "PhonePe";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public List<GatewayProviderConfig> getConfigProperties() {
        return List.of(
                GatewayProviderConfig.of("phonepe.active", "Enable/disable this gateway", GatewayProviderConfig.Type.BOOLEAN, true, "false"),
                GatewayProviderConfig.of("phonepe.merchant.id", "PhonePe merchant ID", GatewayProviderConfig.Type.STRING, true),
                GatewayProviderConfig.of("phonepe.merchant.secret.key", "PhonePe SALT/secret key", GatewayProviderConfig.Type.SECRET, true),
                GatewayProviderConfig.of("phonepe.merchant.secret.index", "PhonePe SALT index", GatewayProviderConfig.Type.STRING, true),
                GatewayProviderConfig.of("phonepe.merchant.host", "PhonePe merchant host", GatewayProviderConfig.Type.STRING, true, "mercury-uat.phonepe.com"),
                GatewayProviderConfig.of("phonepe.url.debit", "PhonePe debit path", GatewayProviderConfig.Type.STRING, true, "/v3/debit"),
                GatewayProviderConfig.of("phonepe.url.status", "PhonePe status path", GatewayProviderConfig.Type.STRING, true, "/v3/transaction")
        );
    }

    @Override
    public GatewayProvider create(Map<String, String> config) {
        return new PhonepeGatewayProvider(config);
    }
}
