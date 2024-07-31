package com.example.demo.config;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DataSourceConfig {

    @Bean(name = "tenant1DataSource")
    public DataSource tenant1DataSource() {
        return DataSourceBuilder.create()
                .url("jdbc:postgresql://localhost:5432/postgres")
                .username("postgres")
                .password("postgres")
                .build();
    }

    @Bean(name = "tenant2DataSource")
    public DataSource tenant2DataSource() {
        return DataSourceBuilder.create()
                .url("jdbc:postgresql://localhost:5433/postgres")
                .username("postgres")
                .password("postgres")
                .build();
    }

    @Primary
    @Bean(name = "routingDataSource")
    public DataSource routingDataSource(@Qualifier("tenant1DataSource") DataSource tenant1DataSource,
                                        @Qualifier("tenant2DataSource") DataSource tenant2DataSource) {

        AbstractRoutingDataSource routingDataSource = new AbstractRoutingDataSource() {
            @Override
            protected Object determineCurrentLookupKey() {
                return MDC.get("tenantId");
            }
        };

        Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put("tenant1", tenant1DataSource);
        dataSourceMap.put("tenant2", tenant2DataSource);

        routingDataSource.setDefaultTargetDataSource(tenant1DataSource);
        routingDataSource.setTargetDataSources(dataSourceMap);

        return routingDataSource;
    }
}

