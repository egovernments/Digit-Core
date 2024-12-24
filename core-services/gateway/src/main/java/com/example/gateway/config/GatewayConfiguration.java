/*
package com.example.gateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfiguration {

    @Autowired
    private GatewayRoutesProperties gatewayRoutesProperties;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        RouteLocatorBuilder.Builder routesBuilder = builder.routes();

        gatewayRoutesProperties.getRoutes().forEach(route -> {
            routesBuilder.route(route.getId(), r -> {
                r.path(route.getPredicates().get(0).substring(5)); // Adjust if your predicates are more complex
                // Add additional configuration based on your route's properties
                route.getFilters().forEach(filter -> {
                    // Example: Configuring rate limiter filter
                    if ("RequestRateLimiter".equals(filter.getName())) {
                        r.filters(f -> f.requestRateLimiter(config -> {
                            config.setRateLimiter(redisRateLimiter());
                            config.setKeyResolver(keyResolverBean());
                            // Set other properties like replenishRate, burstCapacity
                        }));
                    }
                });
                return r.uri(route.getUri());
            });
        });

        return routesBuilder.build();
    }

    // Define your RedisRateLimiter bean
    // Define your KeyResolver bean
}
*/
