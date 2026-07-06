package digit.repository.impl;

import digit.config.ApplicationProperties;
import digit.kafka.Producer;
import digit.repository.BoundaryRelationshipRepository;
import digit.repository.querybuilder.BoundaryRelationshipQueryBuilder;
import digit.repository.rowmapper.BoundaryRelationshipRowMapper;
import digit.web.models.*;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

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
     * Persists the given validated and enriched boundary relationships through egov-persister rather than
     * a direct JDBC write. Each relationship is published as its OWN message to the SAME topic the single
     * create uses ({@code save-boundary-relationship}) via {@link #create}, so both paths write identical
     * rows through the identical, idempotent
     * {@code INSERT ... ON CONFLICT (tenantId, code, hierarchyType) DO NOTHING} mapping.
     *
     * <p>Reusing that topic (instead of a dedicated {@code -batch} topic) is deliberate for
     * deployment-safety: {@code save-boundary-relationship} is always consumed by the persister's normal
     * listener, so bulk creation works on any persister deployment with no extra configuration. Adding
     * {@code save-boundary-relationship} to the persister's {@code persister.batch.topics} (with
     * {@code persister.bulk.enabled=true}) is a pure, optional throughput optimization: its batch listener
     * then aggregates a poll into one multi-row insert. A dedicated {@code -batch} topic, by contrast, is
     * dropped by the normal listener and would be silently orphaned if batch mode were not enabled.</p>
     *
     * <p>One message per record (rather than one message carrying the whole batch) preserves per-record
     * isolation on either listener: a single un-insertable record fails/dead-letters on its own without
     * affecting the rest. Because the insert is idempotent, at-least-once redelivery is a safe no-op.</p>
     *
     * @param boundaryRelationships validated and enriched relationships to persist
     * @param requestInfo request info propagated onto each published message
     */
    @Override
    public void createBulk(List<BoundaryRelation> boundaryRelationships, RequestInfo requestInfo) {
        if (CollectionUtils.isEmpty(boundaryRelationships))
            return;

        for (BoundaryRelation boundaryRelationship : boundaryRelationships) {
            create(BoundaryRelationshipRequest.builder()
                    .requestInfo(requestInfo)
                    .boundaryRelationship(boundaryRelationship)
                    .build());
        }
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
