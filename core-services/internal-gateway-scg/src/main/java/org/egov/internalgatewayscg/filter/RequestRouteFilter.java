package org.egov.internalgatewayscg.filter;

import lombok.extern.slf4j.Slf4j;
import org.egov.internalgatewayscg.utils.RoutingConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

@Slf4j
@Component
public class RequestRouteFilter implements GlobalFilter, Ordered {

    @Autowired
    private RoutingConfig routingConfig;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String requestURI = exchange.getRequest().getURI().getPath();
        String requestTenantId = exchange.getRequest().getHeaders().getFirst("tenantId");
        log.info(" Route filter routing for URI ....... " + requestURI + " and tenantId : " + requestTenantId);
        URL url = null;

        for (Map.Entry<String, Map<String, String>> tenantRoutingConfig :
                routingConfig.getTeanantRoutingConfigWrapper().entrySet()) {

            if (requestURI.matches(tenantRoutingConfig.getKey())) {

                Map<String, String> tenantRoutingMap = tenantRoutingConfig.getValue();
                String routingHost = findTenant(tenantRoutingMap, requestTenantId);
                if (routingHost != null) {
                    try {
                        URI uri = new URI(routingHost);
                        exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, uri);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                break;
            }
        }
        return null;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private String findTenant(Map<String, String> tenantRoutingMap, String reqTenantId) {
        int count = StringUtils.countOccurrencesOf(reqTenantId, ".") + 1;
        String tmpTenantId = new String(reqTenantId);
        for (int i = 0; i < count; i++) {
            if (tenantRoutingMap.containsKey(tmpTenantId)) {
                return tenantRoutingMap.get(tmpTenantId);
            }
            tmpTenantId = tmpTenantId.substring(0,tmpTenantId.lastIndexOf("."));
        }
        return null;
    }
}
