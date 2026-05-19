package org.egov.handler.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.handler.config.ServiceConfiguration;
import org.egov.handler.util.LocalizationUtil;
import org.egov.handler.util.MdmsV2Util;
import org.egov.handler.util.TenantManagementUtil;
import org.egov.handler.web.models.*;
import org.egov.tracer.kafka.CustomKafkaTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Autowired
    public MigrationService(MdmsV2Util mdmsV2Util, LocalizationUtil localizationUtil,
                            TenantManagementUtil tenantManagementUtil, ServiceConfiguration serviceConfig,
                            CustomKafkaTemplate producer) {
        this.mdmsV2Util = mdmsV2Util;
        this.localizationUtil = localizationUtil;
        this.tenantManagementUtil = tenantManagementUtil;
        this.serviceConfig = serviceConfig;
        this.producer = producer;
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

        for (String schemaCode : serviceConfig.getDefaultMdmsSchemaList()) {
            if (TENANT_BOUNDARY_SCHEMA.equals(schemaCode)) {
                continue;
            }
            try {
                upsertMdmsForSchema(targetTenantId, schemaCode, requestInfo);
            } catch (Exception e) {
                log.error("Migration failed for schema {} tenant {}: {}", schemaCode, targetTenantId, e.getMessage());
            }
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
