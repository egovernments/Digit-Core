package org.egov.handler.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.handler.config.ServiceConfiguration;
import org.egov.handler.util.ConfigServiceUtil;
import org.egov.handler.util.LocalizationUtil;
import org.egov.handler.util.MdmsV2Util;
import org.egov.handler.util.TenantManagementUtil;
import org.egov.handler.web.models.*;
import org.egov.tracer.kafka.CustomKafkaTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.egov.handler.config.ServiceConstants.TENANT_BOUNDARY_SCHEMA;

@Slf4j
@Service
public class MigrationService {

    private final MdmsV2Util mdmsV2Util;
    private final LocalizationUtil localizationUtil;
    private final TenantManagementUtil tenantManagementUtil;
    private final ServiceConfiguration serviceConfig;
    private final CustomKafkaTemplate producer;
    private final DataHandlerService dataHandlerService;
    private final ConfigServiceUtil configServiceUtil;

    @Autowired
    public MigrationService(MdmsV2Util mdmsV2Util, LocalizationUtil localizationUtil,
                            TenantManagementUtil tenantManagementUtil, ServiceConfiguration serviceConfig,
                            CustomKafkaTemplate producer, @Lazy DataHandlerService dataHandlerService, ConfigServiceUtil configServiceUtil) {
        this.mdmsV2Util = mdmsV2Util;
        this.localizationUtil = localizationUtil;
        this.tenantManagementUtil = tenantManagementUtil;
        this.serviceConfig = serviceConfig;
        this.producer = producer;
        this.dataHandlerService = dataHandlerService;
        this.configServiceUtil = configServiceUtil;
    }

    public List<String> triggerMigration(MigrationRequest request) {
        List<String> tenantIds = request.getTenantIds();

        if (tenantIds == null || tenantIds.isEmpty()) {
            tenantIds = tenantManagementUtil.fetchAllTenantCodes(request.getRequestInfo()).stream()
                    .filter(code -> !serviceConfig.getDefaultTenantId().equals(code))
                    .collect(Collectors.toList());
        }

        log.info("Queuing migration for {} tenants (migrationSync={})", tenantIds.size(), request.getMigrationSync());
        for (String tenantId : tenantIds) {
            MigrationMessage msg = MigrationMessage.builder()
                    .tenantId(tenantId)
                    .requestInfo(request.getRequestInfo())
                    .migrationSync(request.getMigrationSync())
                    .build();
            producer.send(serviceConfig.getMigrateTopic(), msg);
        }
        return tenantIds;
    }

    public void migrateDefaultData(String targetTenantId, RequestInfo requestInfo, boolean migrationSync) {
        log.info("Starting migration for tenant: {} (migrationSync={})", targetTenantId, migrationSync);

        ensureBoundaryExists(targetTenantId, requestInfo);

        for (String schemaCode : serviceConfig.getDefaultMdmsSchemaList()) {
            if (TENANT_BOUNDARY_SCHEMA.equals(schemaCode)) {
                continue;
            }
            try {
                ensureSchemaExists(targetTenantId, schemaCode, requestInfo);
                upsertMdmsForSchema(targetTenantId, schemaCode, requestInfo);
                
            } catch (Exception e) {
                log.error("Migration failed for schema {} tenant {}: {}", schemaCode, targetTenantId, e.getMessage());
            }
        }
        
        // Copy WhatsApp notification configs from default tenant
        try {
            configServiceUtil.copyConfigData(
            		requestInfo,
                    targetTenantId,
                    serviceConfig.getDefaultConfigServiceSchemaCodes());
        } catch (Exception e) {
            log.error("Failed to copy config-service data for tenant: {}", targetTenantId, e);
        }

        for (String locale : serviceConfig.getDefaultLocalizationLocaleList()) {
            try {
                DefaultLocalizationDataRequest locReq = DefaultLocalizationDataRequest.builder()
                        .requestInfo(requestInfo)
                        .targetTenantId(targetTenantId)
                        .locale(locale)
                        .modules(serviceConfig.getDefaultLocalizationModuleList())
                        .defaultTenantId(serviceConfig.getDefaultTenantId())
                        .migrationSync(migrationSync)
                        .build();
                localizationUtil.createLocalizationData(locReq);
                log.info("Migrated localization locale={} tenant={}", locale, targetTenantId);
            } catch (Exception e) {
                log.error("Migration failed for localization locale={} tenant={}: {}", locale, targetTenantId, e.getMessage());
            }
        }

        log.info("Completed migration for tenant: {}", targetTenantId);
    }

    private void ensureSchemaExists(String targetTenantId, String schemaCode, RequestInfo requestInfo) {
        SchemaDefCriteria targetCriteria = SchemaDefCriteria.builder()
                .tenantId(targetTenantId)
                .codes(Collections.singletonList(schemaCode))
                .build();
        SchemaDefinitionResponse targetResponse = mdmsV2Util.searchMdmsSchema(
                SchemaDefSearchRequest.builder().requestInfo(requestInfo).schemaDefCriteria(targetCriteria).build());

        if (targetResponse != null && targetResponse.getSchemaDefinitions() != null
                && !targetResponse.getSchemaDefinitions().isEmpty()) {
            return;
        }

        SchemaDefCriteria defaultCriteria = SchemaDefCriteria.builder()
                .tenantId(serviceConfig.getDefaultTenantId())
                .codes(Collections.singletonList(schemaCode))
                .build();
        SchemaDefinitionResponse defaultResponse = mdmsV2Util.searchMdmsSchema(
                SchemaDefSearchRequest.builder().requestInfo(requestInfo).schemaDefCriteria(defaultCriteria).build());

        if (defaultResponse == null || defaultResponse.getSchemaDefinitions() == null
                || defaultResponse.getSchemaDefinitions().isEmpty()) {
            log.warn("Schema {} not found in default tenant, skipping schema creation", schemaCode);
            return;
        }

        SchemaDefinition source = defaultResponse.getSchemaDefinitions().get(0);
        SchemaDefinition newSchema = SchemaDefinition.builder()
                .tenantId(targetTenantId)
                .code(source.getCode())
                .description(source.getDescription())
                .definition(source.getDefinition())
                .isActive(source.getIsActive())
                .build();
        mdmsV2Util.createMdmsSchema(
                SchemaDefinitionRequest.builder().requestInfo(requestInfo).schemaDefinition(newSchema).build());
        log.info("Created schema {} for tenant {}", schemaCode, targetTenantId);
    }

    private void ensureBoundaryExists(String targetTenantId, RequestInfo requestInfo) {
        try {
            if (dataHandlerService.boundaryEntityDataExists(targetTenantId, requestInfo)) {
                log.info("Boundary data already exists for tenant {}", targetTenantId);
                return;
            }
            log.info("Boundary data not found for tenant {}, creating...", targetTenantId);
            DefaultDataRequest boundaryRequest = DefaultDataRequest.builder()
                    .requestInfo(requestInfo)
                    .targetTenantId(targetTenantId)
                    .build();
            dataHandlerService.createBoundaryDataFromFile(boundaryRequest);
            log.info("Boundary data created for tenant {}", targetTenantId);
        } catch (Exception e) {
            log.error("Failed to ensure boundary data for tenant {}: {}", targetTenantId, e.getMessage());
        }
    }

    private void upsertMdmsForSchema(String targetTenantId, String schemaCode, RequestInfo requestInfo) {
        List<Mdms> defaultRecords = mdmsV2Util.getAllMdmsResults(serviceConfig.getDefaultTenantId(), schemaCode, requestInfo);
        if (defaultRecords.isEmpty()) return;

        List<Mdms> existingRecords = mdmsV2Util.getAllMdmsResults(targetTenantId, schemaCode, requestInfo);
        Map<String, Mdms> existingByUniqueId = existingRecords.stream()
                .filter(m -> m.getUniqueIdentifier() != null)
                .collect(Collectors.toMap(Mdms::getUniqueIdentifier, Function.identity(), (a, b) -> a));

        int created = 0, updated = 0, skipped = 0;

        for (Mdms defaultRecord : defaultRecords) {
            String uniqueId = defaultRecord.getUniqueIdentifier();
            Mdms existing = existingByUniqueId.get(uniqueId);

            if (existing == null) {
                Mdms newRecord = Mdms.builder()
                        .tenantId(targetTenantId)
                        .schemaCode(schemaCode)
                        .uniqueIdentifier(uniqueId)
                        .data(defaultRecord.getData())
                        .isActive(defaultRecord.getIsActive())
                        .build();
                mdmsV2Util.createMdmsData(MdmsRequest.builder().requestInfo(requestInfo).mdms(newRecord).build());
                created++;
            } else if (!existing.getData().equals(defaultRecord.getData())) {
                existing.setData(defaultRecord.getData());
                mdmsV2Util.updateMdmsData(MdmsRequest.builder().requestInfo(requestInfo).mdms(existing).build());
                updated++;
            } else {
                skipped++;
            }
        }

        log.info("Schema {} tenant {}: created={} updated={} skipped={}", schemaCode, targetTenantId, created, updated, skipped);
    }
}
