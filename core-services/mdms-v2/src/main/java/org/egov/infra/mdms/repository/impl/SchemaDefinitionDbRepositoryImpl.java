package org.egov.infra.mdms.repository.impl;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.exception.InvalidTenantIdException;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.infra.mdms.config.ApplicationConfig;
import org.egov.infra.mdms.model.SchemaDefCriteria;
import org.egov.infra.mdms.model.SchemaDefinition;
import org.egov.infra.mdms.model.SchemaDefinitionRequest;
import org.egov.infra.mdms.producer.Producer;
import org.egov.infra.mdms.repository.SchemaDefinitionRepository;
import org.egov.infra.mdms.repository.querybuilder.SchemaDefinitionQueryBuilder;
import org.egov.infra.mdms.repository.rowmapper.SchemaDefinitionRowMapper;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;

import static org.egov.infra.mdms.errors.ErrorCodes.INVALID_TENANT_ID_ERR_CODE;

@Repository
@Slf4j
public class SchemaDefinitionDbRepositoryImpl implements SchemaDefinitionRepository {

    private final Producer producer;

    private final JdbcTemplate jdbcTemplate;

    private final ApplicationConfig applicationConfig;

    private final SchemaDefinitionQueryBuilder schemaDefinitionQueryBuilder;

    private final SchemaDefinitionRowMapper rowMapper;

    private final MultiStateInstanceUtil multiStateInstanceUtil;

    /**
     * Constructs a new instance of SchemaDefinitionDbRepositoryImpl.
     *
     * @param producer The Producer instance used to emit messages to Kafka topics.
     * @param jdbcTemplate The JdbcTemplate instance for database interactions.
     * @param applicationConfig The ApplicationConfig instance for accessing application-level configurations.
     * @param rowMapper The SchemaDefinitionRowMapper instance for mapping ResultSet rows to SchemaDefinition objects.
     * @param schemaDefinitionQueryBuilder The SchemaDefinitionQueryBuilder instance to build database queries for schema definitions.
     * @param multiStateInstanceUtil The MultiStateInstanceUtil instance to handle tenant-specific operations.
     */
    @Autowired
    public SchemaDefinitionDbRepositoryImpl(Producer producer, JdbcTemplate jdbcTemplate,
                                            ApplicationConfig applicationConfig, SchemaDefinitionRowMapper rowMapper, SchemaDefinitionQueryBuilder schemaDefinitionQueryBuilder, MultiStateInstanceUtil multiStateInstanceUtil){
        this.producer = producer;
        this.jdbcTemplate = jdbcTemplate;
        this.applicationConfig = applicationConfig;
        this.rowMapper = rowMapper;
        this.schemaDefinitionQueryBuilder = schemaDefinitionQueryBuilder;
        this.multiStateInstanceUtil = multiStateInstanceUtil;
    }


    /**
     * This method emits schema definition create request on kafka for async persistence
     * @param schemaDefinitionRequest
     */
    @Override
    public void create(SchemaDefinitionRequest schemaDefinitionRequest) {
        producer.push(schemaDefinitionRequest.getSchemaDefinition().getTenantId(), applicationConfig.getSaveSchemaDefinitionTopicName(), schemaDefinitionRequest);
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
        try {
            // Replaced schema placeholder in the query with tenant specific schema name
            query = multiStateInstanceUtil.replaceSchemaPlaceholder(query, schemaDefCriteria.getTenantId());
        } catch (InvalidTenantIdException e) {
            throw new CustomException(INVALID_TENANT_ID_ERR_CODE, e.getMessage());
        }
        log.info("Schema definition search query: {}", query);

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
