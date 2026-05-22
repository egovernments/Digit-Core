package org.egov.payment.service.gateways.axis;

import org.egov.gateway.spi.GatewayProvider;
import org.egov.gateway.spi.GatewayProviderConfig;
import org.egov.gateway.spi.GatewayProviderFactory;

import java.util.List;
import java.util.Map;

/**
 * ServiceLoader-discovered factory for the Axis Bank gateway.
 * No Spring annotations.
 */
public class AxisGatewayProviderFactory implements GatewayProviderFactory {

    @Override
    public String getGatewayId() {
        return "axis";
    }

    @Override
    public String getDisplayName() {
        return "Axis Bank";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public List<GatewayProviderConfig> getConfigProperties() {
        return List.of(
                GatewayProviderConfig.of("axis.active", "Enable/disable this gateway", GatewayProviderConfig.Type.BOOLEAN, true, "false"),
                GatewayProviderConfig.of("axis.currency", "Transaction currency code", GatewayProviderConfig.Type.STRING, true, "INR"),
                GatewayProviderConfig.of("axis.locale", "Locale for the gateway", GatewayProviderConfig.Type.STRING, true, "en_IN"),
                GatewayProviderConfig.of("axis.merchant.id", "Axis merchant ID", GatewayProviderConfig.Type.STRING, true),
                GatewayProviderConfig.of("axis.merchant.secret.key", "Axis merchant secret key (hex)", GatewayProviderConfig.Type.SECRET, true),
                GatewayProviderConfig.of("axis.merchant.user", "Axis AMA username", GatewayProviderConfig.Type.STRING, true),
                GatewayProviderConfig.of("axis.merchant.pwd", "Axis AMA password", GatewayProviderConfig.Type.SECRET, true),
                GatewayProviderConfig.of("axis.merchant.access.code", "Axis VPC access code", GatewayProviderConfig.Type.STRING, true),
                GatewayProviderConfig.of("axis.merchant.vpc.version", "Axis VPC version", GatewayProviderConfig.Type.STRING, true, "1"),
                GatewayProviderConfig.of("axis.merchant.vpc.command.pay", "Axis VPC pay command", GatewayProviderConfig.Type.STRING, true, "pay"),
                GatewayProviderConfig.of("axis.merchant.vpc.command.status", "Axis VPC status command", GatewayProviderConfig.Type.STRING, true, "queryDR"),
                GatewayProviderConfig.of("axis.url.debit", "Axis debit/pay URL", GatewayProviderConfig.Type.STRING, true, "https://migs.mastercard.com.au/vpcpay"),
                GatewayProviderConfig.of("axis.url.status", "Axis status check URL", GatewayProviderConfig.Type.STRING, true, "https://migs.mastercard.com.au/vpcdps")
        );
    }

    @Override
    public GatewayProvider create(Map<String, String> config) {
        return new AxisGatewayProvider(config);
    }
}
