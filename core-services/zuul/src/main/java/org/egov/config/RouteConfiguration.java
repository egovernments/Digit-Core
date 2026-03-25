package org.egov.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Configuration
@PropertySource({"${gateway.routes.filepath:classpath:routes.properties}"})
@Slf4j
public class RouteConfiguration {

    private final Environment environment;

    public RouteConfiguration(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        Map<String, RouteInfo> routes = parseZuulRoutes();

        RouteLocatorBuilder.Builder routeBuilder = builder.routes();

        for (Map.Entry<String, RouteInfo> entry : routes.entrySet()) {
            String routeId = entry.getKey();
            RouteInfo info = entry.getValue();
            if (info.path != null && info.url != null) {
                final String path = info.path;
                final String url = info.url;
                routeBuilder = routeBuilder.route(routeId, r -> r
                        .path(path)
                        .uri(url));
                log.debug("Registered route: {} -> {} -> {}", routeId, path, url);
            }
        }

        log.info("Registered {} gateway routes from zuul route properties", routes.size());
        return routeBuilder.build();
    }

    private Map<String, RouteInfo> parseZuulRoutes() {
        Map<String, RouteInfo> routes = new LinkedHashMap<>();

        // Load routes.properties file to extract all zuul routes
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("routes.properties")) {
            if (is == null) {
                log.warn("routes.properties not found on classpath");
                return routes;
            }
            Properties props = new Properties();
            props.load(is);

            for (String key : props.stringPropertyNames()) {
                if (key.startsWith("zuul.routes.") && key.endsWith(".path")) {
                    String routeName = key.replace("zuul.routes.", "").replace(".path", "");
                    RouteInfo info = routes.computeIfAbsent(routeName, k -> new RouteInfo());
                    info.path = props.getProperty(key);
                }
                if (key.startsWith("zuul.routes.") && key.endsWith(".url")) {
                    String routeName = key.replace("zuul.routes.", "").replace(".url", "");
                    RouteInfo info = routes.computeIfAbsent(routeName, k -> new RouteInfo());
                    info.url = props.getProperty(key);
                }
            }
        } catch (IOException e) {
            log.error("Failed to load routes.properties", e);
        }

        // Also check application.properties zuul.routes entries
        // (like the hooks-test route defined in application.properties)
        String hooksPath = environment.getProperty("zuul.routes.hooks-test.path");
        String hooksUrl = environment.getProperty("zuul.routes.hooks-test.url");
        if (hooksPath != null && hooksUrl != null) {
            RouteInfo info = routes.computeIfAbsent("hooks-test", k -> new RouteInfo());
            info.path = hooksPath;
            info.url = hooksUrl;
        }

        return routes;
    }

    private static class RouteInfo {
        String path;
        String url;
    }
}
