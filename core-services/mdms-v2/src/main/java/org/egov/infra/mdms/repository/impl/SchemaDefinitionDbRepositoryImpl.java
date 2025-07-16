package org.egov.infra.mdms.repository.impl;

import lombok.extern.slf4j.Slf4j;
import org.egov.infra.mdms.config.ApplicationConfig;
import org.egov.infra.mdms.model.SchemaDefCriteria;
import org.egov.infra.mdms.model.SchemaDefinition;
import org.egov.infra.mdms.model.SchemaDefinitionRequest;
import org.egov.infra.mdms.repository.SchemaDefinitionRepository;
import org.egov.infra.mdms.repository.querybuilder.SchemaDefinitionQueryBuilder;
import org.egov.infra.mdms.repository.rowmapper.SchemaDefinitionRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;

@Repository
@Slf4j
public class SchemaDefinitionDbRepositoryImpl implements SchemaDefinitionRepository {

    private JdbcTemplate jdbcTemplate;

    private ApplicationConfig applicationConfig;

    private SchemaDefinitionQueryBuilder schemaDefinitionQueryBuilder;

    private SchemaDefinitionRowMapper rowMapper;

    @Autowired
    public SchemaDefinitionDbRepositoryImpl(JdbcTemplate jdbcTemplate,
                                            ApplicationConfig applicationConfig, SchemaDefinitionRowMapper rowMapper, SchemaDefinitionQueryBuilder schemaDefinitionQueryBuilder){
        this.jdbcTemplate = jdbcTemplate;
        this.applicationConfig = applicationConfig;
        this.rowMapper = rowMapper;
        this.schemaDefinitionQueryBuilder = schemaDefinitionQueryBuilder;
    }

    /**
     * This method persists schema definition create request directly in the database
     * @param schemaDefinitionRequest
     */
    @Override
    public void create(SchemaDefinitionRequest schemaDefinitionRequest) {
        // Direct JDBC insert logic
        String insertQuery = "INSERT INTO eg_mdms_schema_definition (id, tenantid, code, definition, isactive, createdby, createdtime, lastmodifiedby, lastmodifiedtime) VALUES (?, ?, ?, ?::jsonb, ?, ?, ?, ?, ?)";
        SchemaDefinition schema = schemaDefinitionRequest.getSchemaDefinition();
        jdbcTemplate.update(insertQuery,
            schema.getId(),
            schema.getTenantId(),
            schema.getCode(),
            schema.getDefinition().toString(),
            schema.getIsActive(),
            schema.getAuditDetails().getCreatedBy(),
            schema.getAuditDetails().getCreatedTime(),
            schema.getAuditDetails().getLastModifiedBy(),
            schema.getAuditDetails().getLastModifiedTime()
        );
    }

    /**
     * This method queries the database and returns schema definition search response based on
     * the provided criteria.
     * @param schemaDefCriteria
     */
    @Override
    public List<SchemaDefinition> search(SchemaDefCriteria schemaDefCriteria) {
        List<Object> preparedStatementList = new ArrayList<>();

        // Invoke query builder to generate query based on the provided criteria
        String query = schemaDefinitionQueryBuilder.getSchemaSearchQuery(schemaDefCriteria, preparedStatementList);
        log.info("Schema definition search query: " + query);

        // Query the database to fetch schema definitions
        return jdbcTemplate.query(query, preparedStatementList.toArray(), rowMapper);
    }

    /**
     * Skeleton method for update as update API has not been implemented
     * @param schemaDefinitionRequest
     */
    @Override
    public void update(SchemaDefinitionRequest schemaDefinitionRequest) {
    }

}
