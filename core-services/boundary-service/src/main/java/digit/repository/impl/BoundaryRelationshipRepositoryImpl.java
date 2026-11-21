package digit.repository.impl;

import digit.config.ApplicationProperties;
import digit.kafka.Producer;
import digit.repository.BoundaryRelationshipRepository;
import digit.repository.querybuilder.BoundaryRelationshipQueryBuilder;
import digit.repository.rowmapper.BoundaryRelationshipRowMapper;
import digit.web.models.*;
import org.egov.common.contract.models.AuditDetails;
import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
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
     * Synchronously persists the given validated and enriched boundary relationships using a single
     * JDBC batch insert. The method is transactional so the batch is committed atomically: if any
     * row in the batch cannot be inserted, the transaction rolls back and no row is persisted. This
     * provides a deterministic, committed write (in contrast to the asynchronous Kafka path used by
     * {@link #create}) so the caller can be told exactly which records are durably created.
     *
     * @param boundaryRelationships validated and enriched relationships to persist
     */
    @Override
    @Transactional
    public void createBulk(List<BoundaryRelation> boundaryRelationships) {
        if (boundaryRelationships == null || boundaryRelationships.isEmpty())
            return;

        jdbcTemplate.batchUpdate(boundaryRelationshipQueryBuilder.getBoundaryRelationshipBulkInsertQuery(),
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        BoundaryRelation boundaryRelationship = boundaryRelationships.get(i);
                        AuditDetails auditDetails = boundaryRelationship.getAuditDetails();

                        ps.setString(1, boundaryRelationship.getId());
                        ps.setString(2, boundaryRelationship.getTenantId());
                        ps.setString(3, boundaryRelationship.getCode());
                        ps.setString(4, boundaryRelationship.getHierarchyType());
                        ps.setString(5, boundaryRelationship.getBoundaryType());
                        ps.setString(6, boundaryRelationship.getParent());
                        ps.setString(7, boundaryRelationship.getAncestralMaterializedPath());
                        ps.setObject(8, auditDetails.getCreatedTime());
                        ps.setString(9, auditDetails.getCreatedBy());
                        ps.setObject(10, auditDetails.getLastModifiedTime());
                        ps.setString(11, auditDetails.getLastModifiedBy());
                    }

                    @Override
                    public int getBatchSize() {
                        return boundaryRelationships.size();
                    }
                });
    }

    /**
     * Persists a single relationship in its own transaction — the per-record fallback used when an
     * atomic {@link #createBulk} aborts on a row-level error. Reuses the same idempotent
     * INSERT ... ON CONFLICT DO NOTHING, so a row that already exists is a no-op rather than an error.
     *
     * @param boundaryRelationship a validated and enriched relationship to persist
     */
    @Override
    @Transactional
    public void createOne(BoundaryRelation boundaryRelationship) {
        AuditDetails auditDetails = boundaryRelationship.getAuditDetails();
        jdbcTemplate.update(boundaryRelationshipQueryBuilder.getBoundaryRelationshipBulkInsertQuery(),
                boundaryRelationship.getId(),
                boundaryRelationship.getTenantId(),
                boundaryRelationship.getCode(),
                boundaryRelationship.getHierarchyType(),
                boundaryRelationship.getBoundaryType(),
                boundaryRelationship.getParent(),
                boundaryRelationship.getAncestralMaterializedPath(),
                auditDetails.getCreatedTime(),
                auditDetails.getCreatedBy(),
                auditDetails.getLastModifiedTime(),
                auditDetails.getLastModifiedBy());
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
