package com.example.demo.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.flyway.FlywayDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class FlywayConfig {

    @Value("${spring.flyway.locations}")
    private String flywayLocations;

    @FlywayDataSource
    @Bean(initMethod = "migrate")
    public Flyway flywayTenant1(@Qualifier("tenant1DataSource") DataSource tenant1DataSource) {
        return Flyway.configure()
                .dataSource(tenant1DataSource)
                .baselineOnMigrate(true)
                .locations(flywayLocations)
                .load();
    }

    @FlywayDataSource
    @Bean(initMethod = "migrate")
    public Flyway flywayTenant2(@Qualifier("tenant2DataSource") DataSource tenant2DataSource) {
        return Flyway.configure()
                .dataSource(tenant2DataSource)
                .baselineOnMigrate(true)
                .locations(flywayLocations)
                .load();
    }
}
