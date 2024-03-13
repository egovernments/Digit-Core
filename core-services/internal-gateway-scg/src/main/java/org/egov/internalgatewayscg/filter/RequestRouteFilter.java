package org.egov.internalgatewayscg.filter;

import lombok.extern.slf4j.Slf4j;
import org.egov.internalgatewayscg.utils.CustomException;
import org.egov.internalgatewayscg.utils.RoutingConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.*;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
public class RequestRouteFilter extends RouteToRequestUrlFilter {

    @Autowired
    private RoutingConfig routingConfig;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String requestURI = exchange.getRequest().getURI().getPath();
        String requestTenantId = exchange.getRequest().getHeaders().getFirst("tenantId");

        if(Objects.isNull(requestTenantId)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "TenantId is mandatory");
        }

        log.info(" Route filter routing for URI ....... " + requestURI + " and tenantId : " + requestTenantId);
        URL url = null;

        for (Map.Entry<String, Map<String, String>> tenantRoutingConfig :
                routingConfig.getTeanantRoutingConfigWrapper().entrySet()) {

            if (requestURI.matches(tenantRoutingConfig.getKey())) {

                Map<String, String> tenantRoutingMap = tenantRoutingConfig.getValue();
                String routingHost = findTenant(tenantRoutingMap, requestTenantId);
                if (routingHost != null) {
                    URI uri = exchange.getRequest().getURI();
                    boolean encoded = ServerWebExchangeUtils.containsEncodedParts(uri);
                    try {
                        URI routeuri = new URI(routingHost);
                        URI mergedUrl = UriComponentsBuilder.fromUri(uri)
                                .scheme(routeuri.getScheme())
                                .host(routeuri.getHost())
                                .port(routeuri.getPort())
                                .build(encoded)
                                .toUri();
                        exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, mergedUrl);
                        break;
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    }

                }
                break;
            }
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 1;
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
