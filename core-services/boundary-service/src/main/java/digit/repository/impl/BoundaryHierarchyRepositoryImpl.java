package digit.repository.impl;

import digit.config.ApplicationProperties;
import digit.kafka.Producer;
import digit.repository.BoundaryHierarchyRepository;
import digit.repository.querybuilder.BoundaryHierarchyTypeQueryBuilder;
import digit.repository.rowmapper.BoundaryHierarchyTypeRowMapper;
import digit.web.models.BoundaryTypeHierarchyDefinition;
import digit.web.models.BoundaryTypeHierarchyRequest;
import digit.web.models.BoundaryTypeHierarchySearchCriteria;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
public class BoundaryHierarchyRepositoryImpl implements BoundaryHierarchyRepository {

    private Producer producer;

    private BoundaryHierarchyTypeQueryBuilder boundaryHierarchyTypeQueryBuilder;

    private JdbcTemplate jdbcTemplate;

    private BoundaryHierarchyTypeRowMapper boundaryHierarchyTypeRowMapper;

    private ApplicationProperties applicationProperties;

    public BoundaryHierarchyRepositoryImpl(Producer producer, BoundaryHierarchyTypeQueryBuilder boundaryHierarchyTypeQueryBuilder,
                                           JdbcTemplate jdbcTemplate, BoundaryHierarchyTypeRowMapper boundaryHierarchyTypeRowMapper, ApplicationProperties applicationProperties) {
        this.producer = producer;
        this.boundaryHierarchyTypeQueryBuilder = boundaryHierarchyTypeQueryBuilder;
        this.jdbcTemplate = jdbcTemplate;
        this.boundaryHierarchyTypeRowMapper = boundaryHierarchyTypeRowMapper;
        this.applicationProperties = applicationProperties;
    }

    /**
     * This method implements boundary type hierarchy repository interface. In this implementation
     * it pushes the request to kafka for persister to pick it up and perform insert.
     * @param boundaryTypeHierarchyRequest
     */
    @Override
    public void create(BoundaryTypeHierarchyRequest boundaryTypeHierarchyRequest) {
        producer.push(applicationProperties.getCreateBoundaryHierarchyTopic(), boundaryTypeHierarchyRequest);
    }

    /**
     * This method implements boundary type hierarchy repository interface. In this implementation
     * it pushes the request to kafka for persister to pick it up and perform update.
     * @param boundaryTypeHierarchyRequest
     */
    @Override
    public void update(BoundaryTypeHierarchyRequest boundaryTypeHierarchyRequest) {
        producer.push(applicationProperties.getUpdateBoundaryHierarchyTopic(), boundaryTypeHierarchyRequest);
    }

    /**
     * This method implements boundary type hierarchy repository interface. In this implementation
     * it creates query to search data in PostgreSQL database and returns the search response back
     * to the caller.
     * @param boundaryTypeHierarchySearchCriteria
     * @return
     */
    @Override
    public List<BoundaryTypeHierarchyDefinition> search(BoundaryTypeHierarchySearchCriteria boundaryTypeHierarchySearchCriteria) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = boundaryHierarchyTypeQueryBuilder.getBoundaryHierarchyTypeSearchQuery(boundaryTypeHierarchySearchCriteria, preparedStmtList);
        return jdbcTemplate.query(query, preparedStmtList.toArray(), boundaryHierarchyTypeRowMapper);
    }

}