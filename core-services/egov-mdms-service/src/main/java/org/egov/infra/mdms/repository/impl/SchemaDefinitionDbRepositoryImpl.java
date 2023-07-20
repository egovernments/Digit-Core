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
                                            ApplicationConfig applicationConfig, SchemaDefinitionRowMapper rowMapper){
        this.producer = producer;
        this.jdbcTemplate = jdbcTemplate;
        this.applicationConfig = applicationConfig;
        this.rowMapper = rowMapper;
    }


    /**
     * @param schemaDefinitionRequest
     */
    @Override


    public void create(SchemaDefinitionRequest schemaDefinitionRequest) {
        producer.push(applicationConfig.getSaveSchemaDefinitionTopicName(), schemaDefinitionRequest);
    }

    /**
     * @param schemaDefinitionRequest
     */
    @Override
    public void update(SchemaDefinitionRequest schemaDefinitionRequest) {
    }

    /**
     * @param schemaDefCriteria
     */
    @Override
    public List<SchemaDefinition> search(SchemaDefCriteria schemaDefCriteria) {
        List<Object> params = new ArrayList<>();
        String query = SchemaDefinitionQueryBuilder.getSchemaSearchQuery(schemaDefCriteria, params);
        log.info(query);
        return jdbcTemplate.query(query, params.toArray(), rowMapper);
    }

}
