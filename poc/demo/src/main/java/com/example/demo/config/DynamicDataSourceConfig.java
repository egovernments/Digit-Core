package com.example.demo.config;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DynamicDataSourceConfig {

    @Autowired
    private Environment env;

    @Bean(name = "routingDataSource")
    @ConfigurationProperties(prefix = "tenant")
    public DataSource dataSource() {
        Map<Object, Object> dataSourceMap = new HashMap<>();
        for (String tenantId : getTenantIds()) {
            dataSourceMap.put(tenantId, createDataSource(tenantId));
        }
        AbstractRoutingDataSource routingDataSource = new AbstractRoutingDataSource() {
            @Override
            protected Object determineCurrentLookupKey() {
                return MDC.get("tenantId");
            }
        };
        // Default is mandatory
        /*
         TODO
         Remove hardcoded default tenant
         */
        routingDataSource.setDefaultTargetDataSource(createDataSource("tenant1"));
        routingDataSource.setTargetDataSources(dataSourceMap);
        routingDataSource.afterPropertiesSet();
        return routingDataSource;
    }

    private DataSource createDataSource(String tenantId) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(env.getProperty("tenant." + tenantId + ".url"));
        dataSource.setUsername(env.getProperty("tenant." + tenantId + ".username"));
        dataSource.setPassword(env.getProperty("tenant." + tenantId + ".password"));
        dataSource.setDriverClassName(env.getProperty("tenant." + tenantId + ".driver-class-name"));
        return dataSource;
    }

    private Iterable<String> getTenantIds() {
        // Extract tenant IDs from properties (assuming keys start with "tenant.")
        String tenantIds = env.getProperty("tenant.ids", "tenant1,tenant2");
        return Arrays.asList(tenantIds.split(","));
    }
}
