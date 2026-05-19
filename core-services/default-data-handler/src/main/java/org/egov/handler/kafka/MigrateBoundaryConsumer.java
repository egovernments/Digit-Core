package org.egov.handler.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.handler.service.MigrationService;
import org.egov.handler.web.models.MigrationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Slf4j
@Component
public class MigrateBoundaryConsumer {

    private final ObjectMapper mapper;
    private final MigrationService migrationService;

    @Autowired
    public MigrateBoundaryConsumer(ObjectMapper mapper, MigrationService migrationService) {
        this.mapper = mapper;
        this.migrationService = migrationService;
    }

    @KafkaListener(topics = {"${kafka.topics.migrate.boundary}"})
    public void listen(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        MigrationMessage message = mapper.convertValue(record, MigrationMessage.class);
        log.info("Processing boundary migration for tenant: {}", message.getTenantId());
        migrationService.migrateBoundaryData(message.getTenantId(), message.getRequestInfo(), message.getMigrationSync());
    }
}
