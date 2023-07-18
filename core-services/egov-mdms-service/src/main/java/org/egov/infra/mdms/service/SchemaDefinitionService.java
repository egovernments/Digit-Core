package org.egov.infra.mdms.service;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.infra.mdms.config.ApplicationConfig;
import org.egov.infra.mdms.config.MeasureTime;
import org.egov.infra.mdms.model.*;
import org.egov.infra.mdms.producer.Producer;
import org.egov.infra.mdms.repository.SchemaDefinitionRepository;
import org.egov.infra.mdms.repository.impl.SchemaDefinitionRedisRepository;
import org.egov.infra.mdms.service.enrichment.SchemaDefinitionEnricher;
import org.egov.infra.mdms.service.validator.SchemaDefinitionValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Service
@Builder
@Slf4j
public class SchemaDefinitionService {

    private SchemaDefinitionRepository schemaDefinitionRepository;
    private ApplicationConfig applicationConfig;
    private SchemaDefinitionEnricher schemaDefinitionEnricher;
    private SchemaDefinitionValidator schemaDefinitionValidator;

    @Autowired
    public SchemaDefinitionService(SchemaDefinitionRepository schemaDefinitionRepository, ApplicationConfig applicationConfig,
                                   SchemaDefinitionEnricher schemaDefinitionEnricher, SchemaDefinitionValidator schemaDefinitionValidator){
        this.schemaDefinitionRepository = schemaDefinitionRepository;
        this.applicationConfig = applicationConfig;
        this.schemaDefinitionEnricher = schemaDefinitionEnricher;
        this.schemaDefinitionValidator = schemaDefinitionValidator;
    }

    public List<SchemaDefinition> create(SchemaDefinitionRequest schemaDefinitionRequest) {
        setTenantIDParent(schemaDefinitionRequest.getSchemaDefinition().getTenantId());
        schemaDefinitionValidator.validateCreateRequest(schemaDefinitionRequest);
        schemaDefinitionEnricher.enrichCreateReq(schemaDefinitionRequest);
        schemaDefinitionRepository.create(schemaDefinitionRequest);
        return Arrays.asList(schemaDefinitionRequest.getSchemaDefinition());
    }

    public List<SchemaDefinition> search(SchemaDefSearchRequest schemaDefSearchRequest) {
        setTenantIDParent(schemaDefSearchRequest.getSchemaDefCriteria().getTenantId());
        List<SchemaDefinition> schemaDefinitions = new ArrayList<>();
        log.info("Fetch from database");
        schemaDefinitions = schemaDefinitionRepository.search(schemaDefSearchRequest.getSchemaDefCriteria());
        return schemaDefinitions;
    }
    public SchemaDefinitionResponse update(){
        return null;
    }

    private void setTenantIDParent(String tenantID) {
        if(tenantID.contains("."))
            tenantID = tenantID.split("//.")[0];
    }

}
