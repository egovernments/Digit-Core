package org.egov.filter.route;

import java.net.URI;
import java.util.Map;
import java.util.Map.Entry;

import org.egov.utils.RoutingConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class RequestRoutFilter {

	@Autowired
	private RoutingConfig routingConfig;

	@Bean
	public RouterFunction<ServerResponse> gatewayRoutes() {
		return GatewayRouterFunctions.route("tenant-routing")
				.route(request -> true, HandlerFunctions.http())
				.before(this::applyTenantRouting)
				.build();
	}

	private ServerRequest applyTenantRouting(ServerRequest request) {
		String requestURI = request.uri().getPath();
		String requestTenantId = request.headers().firstHeader("tenantId");
		log.info("Route filter routing for URI: {} and tenantId: {}", requestURI, requestTenantId);

		String routingHost = resolveRoute(requestURI, requestTenantId);

		if (routingHost != null) {
			String targetUrl = routingHost + requestURI;
			String query = request.uri().getQuery();
			if (query != null) {
				targetUrl = targetUrl + "?" + query;
			}
			log.info("Routing request to: {}", targetUrl);

			return ServerRequest.from(request)
					.uri(URI.create(targetUrl))
					.build();
		}

		return request;
	}

	private String resolveRoute(String requestURI, String requestTenantId) {
		if (requestTenantId == null || routingConfig.getTenantRoutingConfigWrapper() == null) {
			return null;
		}

		for (Entry<String, Map<String, String>> tenantRoutingConfig :
				routingConfig.getTenantRoutingConfigWrapper().entrySet()) {

			if (requestURI.matches(tenantRoutingConfig.getKey())) {
				Map<String, String> tenantRoutingMap = tenantRoutingConfig.getValue();
				return findTenant(tenantRoutingMap, requestTenantId);
			}
		}
		return null;
	}

	private String findTenant(Map<String, String> tenantRoutingMap, String reqTenantId) {
		int count = StringUtils.countOccurrencesOf(reqTenantId, ".") + 1;
		String tmpTenantId = new String(reqTenantId);
		for (int i = 0; i < count; i++) {
			if (tenantRoutingMap.containsKey(tmpTenantId)) {
				return tenantRoutingMap.get(tmpTenantId);
			}
			tmpTenantId = tmpTenantId.substring(0, tmpTenantId.lastIndexOf("."));
		}
		return null;
	}
}
