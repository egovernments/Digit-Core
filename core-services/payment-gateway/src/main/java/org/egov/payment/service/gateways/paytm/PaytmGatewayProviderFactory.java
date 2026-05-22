package org.egov.payment.service.gateways.paytm;

import org.egov.gateway.spi.GatewayProvider;
import org.egov.gateway.spi.GatewayProviderConfig;
import org.egov.gateway.spi.GatewayProviderFactory;

import java.util.List;
import java.util.Map;

/**
 * ServiceLoader-discovered factory for the Paytm gateway.
 * No Spring annotations.
 */
public class PaytmGatewayProviderFactory implements GatewayProviderFactory {

    @Override
    public String getGatewayId() {
        return "paytm";
    }

    @Override
    public String getDisplayName() {
        return "Paytm";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public List<GatewayProviderConfig> getConfigProperties() {
        return List.of(
                GatewayProviderConfig.of("paytm.active", "Enable/disable this gateway", GatewayProviderConfig.Type.BOOLEAN, true, "false"),
                GatewayProviderConfig.of("paytm.merchant.id", "Paytm merchant ID", GatewayProviderConfig.Type.STRING, true),
                GatewayProviderConfig.of("paytm.merchant.secret.key", "Paytm merchant secret key", GatewayProviderConfig.Type.SECRET, true),
                GatewayProviderConfig.of("paytm.merchant.industry.type", "Paytm industry type", GatewayProviderConfig.Type.STRING, true, "Retail"),
                GatewayProviderConfig.of("paytm.merchant.channel.id", "Paytm channel ID", GatewayProviderConfig.Type.STRING, true, "WEB"),
                GatewayProviderConfig.of("paytm.merchant.website", "Paytm website", GatewayProviderConfig.Type.STRING, true, "WEBSTAGING"),
                GatewayProviderConfig.of("paytm.url.debit", "Paytm debit/payment URL", GatewayProviderConfig.Type.STRING, true, "https://securegw-stage.paytm.in/theia/processTransaction"),
                GatewayProviderConfig.of("paytm.url.status", "Paytm status check URL", GatewayProviderConfig.Type.STRING, true, "https://securegw-stage.paytm.in/merchant-status/getTxnStatus")
        );
    }

    @Override
    public GatewayProvider create(Map<String, String> config) {
        return new PaytmGatewayProvider(config);
    }
}
