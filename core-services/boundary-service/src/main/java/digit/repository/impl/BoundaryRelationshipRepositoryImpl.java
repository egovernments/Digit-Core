package digit.repository.impl;

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

    public BoundaryRelationshipRepositoryImpl(Producer producer, JdbcTemplate jdbcTemplate,
                                              BoundaryRelationshipQueryBuilder boundaryRelationshipQueryBuilder, BoundaryRelationshipRowMapper boundaryRelationshipRowMapper) {
        this.producer = producer;
        this.jdbcTemplate = jdbcTemplate;
        this.boundaryRelationshipQueryBuilder = boundaryRelationshipQueryBuilder;
        this.boundaryRelationshipRowMapper = boundaryRelationshipRowMapper;
    }

    @Override
    public void create(BoundaryRelationshipRequest boundaryRelationshipRequest) {
        // Transform boundary relationship request
        BoundaryRelationshipRequestDTO boundaryRelationshipRequestDTO = convertContractPOJOToDTO(boundaryRelationshipRequest);

        // Push to event bus for creating asynchronously
        producer.push("save-boundary-relationship", boundaryRelationshipRequestDTO);
    }

    @Override
    public void update(BoundaryRelationshipRequest boundaryRelationshipRequest) {
        // Push to event bus for updating asynchronously
        producer.push("update-boundary-relationship", boundaryRelationshipRequest);
    }

    @Override
    public List<BoundaryRelationshipDTO> search(BoundaryRelationshipSearchCriteria boundaryRelationshipSearchCriteria) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = boundaryRelationshipQueryBuilder.getBoundaryRelationshipSearchQuery(boundaryRelationshipSearchCriteria, preparedStmtList);
        return jdbcTemplate.query(query, preparedStmtList.toArray(), boundaryRelationshipRowMapper);
    }

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
