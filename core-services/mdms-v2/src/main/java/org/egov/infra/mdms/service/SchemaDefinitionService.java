package org.egov.infra.mdms.service;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.infra.mdms.config.ApplicationConfig;
import org.egov.infra.mdms.model.*;
import org.egov.infra.mdms.repository.SchemaDefinitionRepository;
import org.egov.infra.mdms.service.enrichment.SchemaDefinitionEnricher;
import org.egov.infra.mdms.service.validator.SchemaDefinitionValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@Builder
@Slf4j
public class SchemaDefinitionService {

    private SchemaDefinitionRepository schemaDefinitionRepository;
    private ApplicationConfig applicationConfig;
    private SchemaDefinitionEnricher schemaDefinitionEnricher;
    private SchemaDefinitionValidator schemaDefinitionValidator;
    private MultiStateInstanceUtil multiStateInstanceUtil;

    @Autowired
    public SchemaDefinitionService(SchemaDefinitionRepository schemaDefinitionRepository, ApplicationConfig applicationConfig,
                                   SchemaDefinitionEnricher schemaDefinitionEnricher, SchemaDefinitionValidator schemaDefinitionValidator, MultiStateInstanceUtil multiStateInstanceUtil){
        this.schemaDefinitionRepository = schemaDefinitionRepository;
        this.applicationConfig = applicationConfig;
        this.schemaDefinitionEnricher = schemaDefinitionEnricher;
        this.schemaDefinitionValidator = schemaDefinitionValidator;
        this.multiStateInstanceUtil = multiStateInstanceUtil;
    }

    /**
     * This method processes requests for schema definition creation.
     * @param schemaDefinitionRequest
     * @return
     */
    public List<SchemaDefinition> create(SchemaDefinitionRequest schemaDefinitionRequest) {

        // Set incoming tenantId as state level tenantId as schema is always created at state level
        String tenantId = schemaDefinitionRequest.getSchemaDefinition().getTenantId();
        schemaDefinitionRequest.getSchemaDefinition().setTenantId(multiStateInstanceUtil.getStateLevelTenant(tenantId));

        // Validate schema create request
        schemaDefinitionValidator.validateCreateRequest(schemaDefinitionRequest);

        // Enrich schema create request
        schemaDefinitionEnricher.enrichCreateRequest(schemaDefinitionRequest);

        // Invoke repository method to emit schema creation event
        schemaDefinitionRepository.create(schemaDefinitionRequest);

        return Arrays.asList(schemaDefinitionRequest.getSchemaDefinition());
    }

    /**
     * This method processes the requests for schema definition search.
     * @param schemaDefSearchRequest
     * @return
     */
    public List<SchemaDefinition> search(SchemaDefSearchRequest schemaDefSearchRequest) {

        // Set incoming tenantId as state level tenantId as schema is created at state level
        String tenantId = schemaDefSearchRequest.getSchemaDefCriteria().getTenantId();
        schemaDefSearchRequest.getSchemaDefCriteria().setTenantId(multiStateInstanceUtil.getStateLevelTenant(tenantId));

        // Fetch schema definitions based on the given criteria
        List<SchemaDefinition> schemaDefinitions = schemaDefinitionRepository.search(schemaDefSearchRequest.getSchemaDefCriteria());

        return schemaDefinitions;
    }

}
