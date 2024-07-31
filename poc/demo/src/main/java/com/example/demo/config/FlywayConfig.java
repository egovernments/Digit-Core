package com.example.demo.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class FlywayConfig {

    @Autowired
    private Environment env;

    @Autowired
    private DataSource routingDataSource;

    @Value("${spring.flyway.locations}")
    private String flywayLocations;

    @Bean
    public Map<String, Flyway> flywayMap() {
        Map<String, Flyway> flywayMap = new HashMap<>();
        Map<Object, DataSource> targetDataSources = ((AbstractRoutingDataSource) routingDataSource).getResolvedDataSources();
        for (Object key : targetDataSources.keySet()) {
            DataSource dataSource = (DataSource) targetDataSources.get(key);
            boolean baselineOnMigrate = Boolean.parseBoolean(env.getProperty("tenant." + key + ".baseline-on-migrate", "true"));

            Flyway flyway = Flyway.configure()
                    .dataSource(dataSource)
                    .baselineOnMigrate(baselineOnMigrate)
                    .locations(flywayLocations)
                    .load();
            flyway.migrate();

            flywayMap.put((String) key, flyway);
        }
        return flywayMap;
    }
}
