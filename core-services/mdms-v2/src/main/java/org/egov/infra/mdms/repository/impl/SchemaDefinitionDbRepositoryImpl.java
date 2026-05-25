package org.egov.infra.mdms.repository.impl;

import lombok.extern.slf4j.Slf4j;
import org.egov.infra.mdms.config.ApplicationConfig;
import org.egov.infra.mdms.model.SchemaDefCriteria;
import org.egov.infra.mdms.model.SchemaDefinition;
import org.egov.infra.mdms.model.SchemaDefinitionRequest;
import org.egov.infra.mdms.producer.Producer;
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

    private Producer producer;

    private JdbcTemplate jdbcTemplate;

    private ApplicationConfig applicationConfig;

    private SchemaDefinitionQueryBuilder schemaDefinitionQueryBuilder;

    private SchemaDefinitionRowMapper rowMapper;

    @Autowired
    public SchemaDefinitionDbRepositoryImpl(Producer producer, JdbcTemplate jdbcTemplate,
                                            ApplicationConfig applicationConfig, SchemaDefinitionRowMapper rowMapper, SchemaDefinitionQueryBuilder schemaDefinitionQueryBuilder){
        this.producer = producer;
        this.jdbcTemplate = jdbcTemplate;
        this.applicationConfig = applicationConfig;
        this.rowMapper = rowMapper;
        this.schemaDefinitionQueryBuilder = schemaDefinitionQueryBuilder;
    }


    /**
     * This method emits schema definition create request on kafka for async persistence
     * @param schemaDefinitionRequest
     */
    @Override
    public void create(SchemaDefinitionRequest schemaDefinitionRequest) {
        producer.push(applicationConfig.getSaveSchemaDefinitionTopicName(), schemaDefinitionRequest);
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
