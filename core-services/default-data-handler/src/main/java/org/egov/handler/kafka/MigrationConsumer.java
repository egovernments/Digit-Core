package org.egov.handler.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.handler.service.MigrationService;
import org.egov.handler.web.models.MigrationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Slf4j
@Component
public class MigrationConsumer {

    private final ObjectMapper mapper;
    private final MigrationService migrationService;
    private final ThreadPoolTaskExecutor migrationTaskExecutor;

    @Autowired
    public MigrationConsumer(ObjectMapper mapper, MigrationService migrationService,
                             @Qualifier("migrationTaskExecutor") ThreadPoolTaskExecutor migrationTaskExecutor) {
        this.mapper = mapper;
        this.migrationService = migrationService;
        this.migrationTaskExecutor = migrationTaskExecutor;
    }

    @KafkaListener(topics = {"${kafka.topics.migrate.tenant}"})
    public void listen(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            MigrationMessage message = mapper.convertValue(record, MigrationMessage.class);
            boolean migrationSync = Boolean.TRUE.equals(message.getMigrationSync());
            log.info("Submitting migration task for tenant: {} pool=[active={} queue={}]",
                    message.getTenantId(),
                    migrationTaskExecutor.getActiveCount(),
                    migrationTaskExecutor.getThreadPoolExecutor().getQueue().size());

            migrationTaskExecutor.submit(() -> {
                try {
                    migrationService.migrateDefaultData(message.getTenantId(), message.getRequestInfo(), migrationSync);
                    log.info("Completed migration for tenant: {}", message.getTenantId());
                } catch (Exception e) {
                    log.error("Migration failed for tenant {}: {}", message.getTenantId(), e.getMessage(), e);
                }
            });
        } catch (Exception e) {
            log.error("Failed to submit migration task: {}", e.getMessage(), e);
        }
    }
}
