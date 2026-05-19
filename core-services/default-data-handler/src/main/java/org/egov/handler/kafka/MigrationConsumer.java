package org.egov.handler.kafka;

import java.util.HashMap;

import org.egov.handler.service.MigrationService;
import org.egov.handler.web.models.MigrationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MigrationConsumer {

    private final ObjectMapper mapper;
    private final MigrationService migrationService;

    @Autowired
    public MigrationConsumer(ObjectMapper mapper, MigrationService migrationService) {
        this.mapper = mapper;
        this.migrationService = migrationService;
    }

    @KafkaListener(topics = {"${kafka.topics.migrate.tenant}"})
    public void listen(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        MigrationMessage message = mapper.convertValue(record, MigrationMessage.class);
        log.info("Processing MDMS+config migration for tenant: {}", message.getTenantId());
        migrationService.migrateMdmsAndConfigData(message.getTenantId(), message.getRequestInfo(), message.getMigrationSync());
    }
}
