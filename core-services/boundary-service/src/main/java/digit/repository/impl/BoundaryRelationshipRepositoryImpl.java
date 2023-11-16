package digit.repository.impl;

import digit.config.ApplicationProperties;
import digit.kafka.Producer;
import digit.repository.BoundaryRelationshipRepository;
import digit.repository.querybuilder.BoundaryRelationshipQueryBuilder;
import digit.repository.rowmapper.BoundaryRelationshipRowMapper;
import digit.web.models.*;
import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class BoundaryRelationshipRepositoryImpl implements BoundaryRelationshipRepository {

    private Producer producer;

    private JdbcTemplate jdbcTemplate;

    private BoundaryRelationshipQueryBuilder boundaryRelationshipQueryBuilder;

    private BoundaryRelationshipRowMapper boundaryRelationshipRowMapper;

    private ApplicationProperties applicationProperties;

    public BoundaryRelationshipRepositoryImpl(Producer producer, JdbcTemplate jdbcTemplate,
                                              BoundaryRelationshipQueryBuilder boundaryRelationshipQueryBuilder, BoundaryRelationshipRowMapper boundaryRelationshipRowMapper, ApplicationProperties applicationProperties) {
        this.producer = producer;
        this.jdbcTemplate = jdbcTemplate;
        this.boundaryRelationshipQueryBuilder = boundaryRelationshipQueryBuilder;
        this.boundaryRelationshipRowMapper = boundaryRelationshipRowMapper;
        this.applicationProperties = applicationProperties;
    }

    /**
     * This method implements boundary relationship interface. In this implementation
     * it pushes the request to kafka for persister to pick it up and perform create.
     * @param boundaryRelationshipRequest
     */
    @Override
    public void create(BoundaryRelationshipRequest boundaryRelationshipRequest) {
        // Transform boundary relationship request
        BoundaryRelationshipRequestDTO boundaryRelationshipRequestDTO = convertContractPOJOToDTO(boundaryRelationshipRequest);

        // Push to event bus for creating asynchronously
        producer.push(applicationProperties.getCreateBoundaryRelationshipTopic(), boundaryRelationshipRequestDTO);
    }

    /**
     * This method implements boundary relationship interface's update method. In this implementation
     * it pushes the request to kafka for persister to pick it up and perform update.
     * @param boundaryRelationshipRequestDTO
     */
    @Override
    public void update(BoundaryRelationshipRequestDTO boundaryRelationshipRequestDTO) {
        // Push to event bus for updating asynchronously
        producer.push(applicationProperties.getUpdateBoundaryRelationshipTopic(), boundaryRelationshipRequestDTO);
    }

    /**
     * This method implements boundary relationship repository interface. In this implementation
     * it creates query to search data in PostgreSQL database and returns the search response back
     * to the caller.
     * @param boundaryRelationshipSearchCriteria
     * @return
     */
    @Override
    public List<BoundaryRelationshipDTO> search(BoundaryRelationshipSearchCriteria boundaryRelationshipSearchCriteria) {
        // Declare prepared statement list
        List<Object> preparedStmtList = new ArrayList<>();

        // Get query for searching boundary relationship
        String query = boundaryRelationshipQueryBuilder.getBoundaryRelationshipSearchQuery(boundaryRelationshipSearchCriteria, preparedStmtList);

        // Return search response based on provided search criteria
        return jdbcTemplate.query(query, preparedStmtList.toArray(), boundaryRelationshipRowMapper);
    }

    /**
     * Helper method to convert boundary relationship POJOs into boundary relationship DTOs
     * @param contractBean
     * @return
     */
    private BoundaryRelationshipRequestDTO convertContractPOJOToDTO(BoundaryRelationshipRequest contractBean) {
        // Declare boundary relationship request DTO
        BoundaryRelationshipRequestDTO boundaryRelationshipRequestDTO = new BoundaryRelationshipRequestDTO();

        // Copy boundary relationship properties
        BoundaryRelationshipDTO boundaryRelationshipDTO = new BoundaryRelationshipDTO();
        BeanUtils.copyProperties(contractBean.getBoundaryRelationship(), boundaryRelationshipDTO);
        BeanUtils.copyProperties(contractBean, boundaryRelationshipRequestDTO);

        // Enrich ancestral materialized path
        boundaryRelationshipDTO.setAncestralMaterializedPath(contractBean.getBoundaryRelationship().getAncestralMaterializedPath());

        // Enrich boundary relationship DTO in request
        boundaryRelationshipRequestDTO.setBoundaryRelationshipDTO(boundaryRelationshipDTO);

        return boundaryRelationshipRequestDTO;
    }
}
