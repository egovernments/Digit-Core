package org.egov.handler.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class MigrationKafkaConfig {

    @Value("${migration.worker.core.pool.size:10}")
    private int corePoolSize;

    @Value("${migration.worker.max.pool.size:20}")
    private int maxPoolSize;

    // Queue holds pending migration tasks. Set high enough to absorb all
    // tenants submitted from Kafka before workers drain the queue.
    @Value("${migration.worker.queue.capacity:5000}")
    private int queueCapacity;

    @Bean("migrationTaskExecutor")
    public ThreadPoolTaskExecutor migrationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("migration-worker-");
        // If queue is full, the calling thread runs the task (natural backpressure)
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
