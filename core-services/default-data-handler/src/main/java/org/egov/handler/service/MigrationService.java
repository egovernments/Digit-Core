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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    private final ExecutorService migrationExecutor;

    @Autowired
    public MigrationService(MdmsV2Util mdmsV2Util, LocalizationUtil localizationUtil,
                            TenantManagementUtil tenantManagementUtil, ServiceConfiguration serviceConfig,
                            CustomKafkaTemplate producer, @Lazy DataHandlerService dataHandlerService,
                            ConfigServiceUtil configServiceUtil) {
        this.mdmsV2Util = mdmsV2Util;
        this.localizationUtil = localizationUtil;
        this.tenantManagementUtil = tenantManagementUtil;
        this.serviceConfig = serviceConfig;
        this.producer = producer;
        this.dataHandlerService = dataHandlerService;
        this.configServiceUtil = configServiceUtil;
        this.migrationExecutor = Executors.newFixedThreadPool(serviceConfig.getMigrationWorkerCount());
    }

    public List<String> triggerMigration(MigrationRequest request) {
        List<String> sourceTenantIds = request.getTenantIds();

        if (sourceTenantIds == null || sourceTenantIds.isEmpty()) {
            sourceTenantIds = tenantManagementUtil.fetchAllTenantCodes(request.getRequestInfo());
        }

        String defaultTenantId = serviceConfig.getDefaultTenantId();
        Set<String> seen = new HashSet<>();
        List<String> queued = new ArrayList<>();
        List<String> failed = new ArrayList<>();
        int total = 0;

        for (String tenantId : sourceTenantIds) {
            if (tenantId == null || defaultTenantId.equals(tenantId) || !seen.add(tenantId)) {
                continue;
            }
            total++;
            try {
                producer.send(serviceConfig.getMigrateTopic(),
                        buildMessage(tenantId, request.getRequestInfo(), request.getMigrationSync(), request.getMigration()));
                queued.add(tenantId);
            } catch (Exception e) {
                log.error("Failed to queue tenant {}: {}", tenantId, e.getMessage());
                failed.add(tenantId);
            }

            int processed = queued.size() + failed.size();
            if (processed % 100 == 0) {
                log.info("Queuing progress: completed={} pending={} failed={} total={}",
                        queued.size(), total - processed, failed.size(), total);
            }
        }

        log.info("Migration queuing complete: total={} queued={} failed={} migrationSync={}",
                total, queued.size(), failed.size(), request.getMigrationSync());

        if (!failed.isEmpty()) {
            log.error("Failed to queue {} tenants (not retrying): {}", failed.size(), failed);
        }

        return queued;
    }

    private MigrationMessage buildMessage(String tenantId, RequestInfo requestInfo, Boolean migrationSync, Boolean isMigration) {
        return MigrationMessage.builder()
                .tenantId(tenantId)
                .requestInfo(requestInfo)
                .migrationSync(migrationSync)
                .migration(isMigration)
                .build();
    }

    public void migrateMdmsAndConfigData(String targetTenantId, RequestInfo requestInfo, Boolean migrationSync, Boolean isMigration) {
        log.info("Starting MDMS and config migration for tenant: {}", targetTenantId);

        List<String> schemaList = serviceConfig.getDefaultMdmsSchemaList().stream()
                .filter(s -> !TENANT_BOUNDARY_SCHEMA.equals(s))
                .collect(Collectors.toList());

        // Two bulk searches instead of 2 calls per schema
        Map<String, SchemaDefinition> defaultSchemas = fetchSchemaMap(serviceConfig.getDefaultTenantId(), schemaList, requestInfo);
        Map<String, SchemaDefinition> targetSchemas = fetchSchemaMap(targetTenantId, schemaList, requestInfo);

        List<CompletableFuture<Void>> futures = schemaList.stream()
                .map(schemaCode -> CompletableFuture.runAsync(
                        () -> migrateSchema(targetTenantId, schemaCode, defaultSchemas, targetSchemas, requestInfo, isMigration),
                        migrationExecutor))
                .collect(Collectors.toList());

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        try {
            configServiceUtil.copyConfigData(
                    requestInfo,
                    targetTenantId,
                    serviceConfig.getDefaultConfigServiceSchemaCodes());
        } catch (Exception e) {
            log.error("Failed to copy config-service data for tenant: {}", targetTenantId, e);
        }

        log.info("Completed MDMS and config migration for tenant: {}, publishing boundary event", targetTenantId);
        producer.send(serviceConfig.getMigrateBoundaryTopic(), buildMessage(targetTenantId, requestInfo, migrationSync, isMigration));
    }

    public void migrateBoundaryData(String targetTenantId, RequestInfo requestInfo, Boolean migrationSync, Boolean isMigration) {
        log.info("Starting boundary migration for tenant: {}", targetTenantId);

        ensureBoundaryExists(targetTenantId, requestInfo);

        log.info("Completed boundary migration for tenant: {}, publishing localization event", targetTenantId);
        producer.send(serviceConfig.getMigrateLocalizationTopic(), buildMessage(targetTenantId, requestInfo, migrationSync, isMigration));
    }

    public void migrateLocalizationData(String targetTenantId, RequestInfo requestInfo, boolean migrationSync) {
        log.info("Starting localization migration for tenant: {} (migrationSync={})", targetTenantId, migrationSync);

        List<String> modules = serviceConfig.getDefaultLocalizationModuleList();

        List<CompletableFuture<Void>> futures = serviceConfig.getDefaultLocalizationLocaleList().stream()
                .map(locale -> CompletableFuture.runAsync(() -> {
                    try {
                        DefaultLocalizationDataRequest locReq = DefaultLocalizationDataRequest.builder()
                                .requestInfo(requestInfo)
                                .targetTenantId(targetTenantId)
                                .locale(locale)
                                .modules(modules)
                                .defaultTenantId(serviceConfig.getDefaultTenantId())
                                .migrationSync(migrationSync)
                                .build();
                        localizationUtil.createLocalizationData(locReq);
                        log.info("Migrated localization locale={} tenant={}", locale, targetTenantId);
                    } catch (Exception e) {
                        log.error("Migration failed for localization locale={} tenant={}: {}", locale, targetTenantId, e.getMessage());
                    }
                }, migrationExecutor))
                .collect(Collectors.toList());

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        log.info("Completed localization migration for tenant: {}", targetTenantId);
    }

    // --- private helpers ---

    private Map<String, SchemaDefinition> fetchSchemaMap(String tenantId, List<String> codes, RequestInfo requestInfo) {
        try {
            SchemaDefCriteria criteria = SchemaDefCriteria.builder()
                    .tenantId(tenantId)
                    .codes(codes)
                    .limit(codes.size() + 10)
                    .build();
            SchemaDefinitionResponse response = mdmsV2Util.searchMdmsSchema(
                    SchemaDefSearchRequest.builder().requestInfo(requestInfo).schemaDefCriteria(criteria).build());
            if (response == null || response.getSchemaDefinitions() == null) return Collections.emptyMap();
            return response.getSchemaDefinitions().stream()
                    .collect(Collectors.toMap(SchemaDefinition::getCode, Function.identity()));
        } catch (Exception e) {
            log.error("Bulk schema fetch failed for tenant {}, will fall back to per-schema create: {}", tenantId, e.getMessage());
            return Collections.emptyMap();
        }
    }

    private void migrateSchema(String targetTenantId, String schemaCode,
                               Map<String, SchemaDefinition> defaultSchemas,
                               Map<String, SchemaDefinition> targetSchemas,
                               RequestInfo requestInfo, boolean isMigration) {
        try {
            if (!targetSchemas.containsKey(schemaCode)) {
                SchemaDefinition source = defaultSchemas.get(schemaCode);
                if (source == null) {
                    log.warn("Schema {} not found in default tenant, skipping schema creation", schemaCode);
                } else {
                    try {
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
                    } catch (Exception e) {
                        log.error("Failed to create schema {} for tenant {}: {}", schemaCode, targetTenantId, e.getMessage());
                    }
                }
            }
            upsertMdmsForSchema(targetTenantId, schemaCode, requestInfo, isMigration);
        } catch (Exception e) {
            log.error("Migration failed for schema {} tenant {}: {}", schemaCode, targetTenantId, e.getMessage());
        }
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

    private void upsertMdmsForSchema(String targetTenantId, String schemaCode, RequestInfo requestInfo, boolean isMigration) {
        DefaultMdmsDataRequest defaultMdmsDataRequest = DefaultMdmsDataRequest.builder()
                .requestInfo(requestInfo)
                .targetTenantId(targetTenantId)
                .schemaCodes(Collections.singletonList(schemaCode))
                .onlySchemas(Boolean.FALSE)
                .defaultTenantId(serviceConfig.getDefaultTenantId())
                .migration(isMigration)
                .build();
        mdmsV2Util.createDefaultMdmsData(defaultMdmsDataRequest);
        log.info("Schema {} data copied for tenant {} (isMigration={})", schemaCode, targetTenantId, isMigration);
    }
}
