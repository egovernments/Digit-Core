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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        List<String> explicitTenantIds = request.getTenantIds();

        if (explicitTenantIds != null && !explicitTenantIds.isEmpty()) {
            Set<String> seen = new HashSet<>();
            List<String> queued = new ArrayList<>();
            for (String tenantId : explicitTenantIds) {
                if (seen.add(tenantId)) {
                    queueMigration(tenantId, request);
                    queued.add(tenantId);
                }
            }
            log.info("Queued migration for {} explicit tenants (migrationSync={})", queued.size(), request.getMigrationSync());
            return queued;
        }

        // Page-by-page fetch — never loads all tenants into memory at once.
        // A Set guards against both duplicates and API wrap-around (stops when a
        // full page yields zero new codes).
        Set<String> queued = new HashSet<>();
        int offset = 0;
        int limit = serviceConfig.getTenantSearchPageSize();
        String defaultTenantId = serviceConfig.getDefaultTenantId();

        while (true) {
            List<String> page = tenantManagementUtil.fetchTenantCodesPage(request.getRequestInfo(), offset, limit);

            if (page.isEmpty()) {
                break;
            }

            int newInPage = 0;
            for (String code : page) {
                if (!defaultTenantId.equals(code) && queued.add(code)) {
                    queueMigration(code, request);
                    newInPage++;
                }
            }

            log.info("Page offset={} size={} newQueued={} totalQueued={}", offset, page.size(), newInPage, queued.size());

            // Stop if: API returned a partial page (real end) OR entire page was duplicates (wrap-around)
            if (page.size() < limit || newInPage == 0) {
                break;
            }

            offset += limit;
        }

        log.info("Migration queued for {} tenants (migrationSync={})", queued.size(), request.getMigrationSync());
        return new ArrayList<>(queued);
    }

    private void queueMigration(String tenantId, MigrationRequest request) {
        MigrationMessage msg = MigrationMessage.builder()
                .tenantId(tenantId)
                .requestInfo(request.getRequestInfo())
                .migrationSync(request.getMigrationSync())
                .build();
        producer.send(serviceConfig.getMigrateTopic(), msg);
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
