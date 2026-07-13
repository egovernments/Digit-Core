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
     * a direct JDBC write. The WHOLE validated list is published as ONE message to the SAME topic the
     * single create uses ({@code save-boundary-relationship}), under the same {@code BoundaryRelationship}
     * key but carrying an ARRAY (whereas single-create carries one object). The persister's mapping reads
     * it with an array base path ({@code $.BoundaryRelationship.*}), so {@code PersistRepository.getRows}
     * emits one row per element and the listener performs ONE {@code jdbcTemplate.batchUpdate} for the
     * whole message through the same idempotent
     * {@code INSERT ... ON CONFLICT (tenantId, code, hierarchyType) DO NOTHING} query. One message per
     * batch (instead of one message per record) is what restores batched throughput while keeping the
     * write owned by the persister.
     *
     * <p>Reusing that topic (instead of a dedicated {@code -batch} topic) is deliberate for
     * deployment-safety: {@code save-boundary-relationship} is always consumed by the persister's normal
     * single-record listener. Batching here is WITHIN a single message (the array), so it does not depend
     * on {@code persister.bulk.enabled}: the normal listener maps the array to N rows and batch-inserts
     * them in one transaction. Because the insert is idempotent, at-least-once redelivery is a safe no-op;
     * duplicates within/across messages are silently skipped by ON CONFLICT and never abort the batch.</p>
     *
     * <p>The message is keyed by the batch's parent code (callers batch siblings under one already-persisted
     * parent), so batches for the same parent stay ordered on the same partition. A null/mixed parent falls
     * back to the keyless behaviour of the single path.</p>
     *
     * @param boundaryRelationships validated and enriched relationships to persist
     * @param requestInfo request info propagated onto the published message
     */
    @Override
    public void createBulk(List<BoundaryRelation> boundaryRelationships, RequestInfo requestInfo) {
        if (CollectionUtils.isEmpty(boundaryRelationships))
            return;

        // Convert each validated+enriched contract POJO to the DTO that exposes ancestralMaterializedPath
        // on the wire (it is @JsonIgnore on BoundaryRelation but @JsonProperty on the DTO), mirroring the
        // single-create serialization so both paths persist identical rows.
        List<BoundaryRelationshipDTO> boundaryRelationshipDTOs = new ArrayList<>(boundaryRelationships.size());
        for (BoundaryRelation boundaryRelationship : boundaryRelationships) {
            boundaryRelationshipDTOs.add(convertRelationPOJOToDTO(boundaryRelationship));
        }

        BulkBoundaryRelationshipRequestDTO batchMessage = BulkBoundaryRelationshipRequestDTO.builder()
                .requestInfo(requestInfo)
                .boundaryRelationship(boundaryRelationshipDTOs)
                .build();

        // Publish the whole validated list as ONE message to the unchanged topic.
        producer.push(applicationProperties.getCreateBoundaryRelationshipTopic(), resolveBatchKey(boundaryRelationships), batchMessage);
    }

    /**
     * Kafka key for a batch: the shared parent code when every record in the batch has the same
     * (non-null) parent, else null (keyless, i.e. the single-create default-partitioner behaviour).
     * Keying by parent keeps sibling batches under one parent ordered on the same partition; a mixed or
     * root batch must not be forced onto one partition, so it falls back to keyless.
     */
    private String resolveBatchKey(List<BoundaryRelation> boundaryRelationships) {
        String firstParent = boundaryRelationships.get(0).getParent();
        if (firstParent == null)
            return null;
        for (BoundaryRelation boundaryRelationship : boundaryRelationships) {
            if (!firstParent.equals(boundaryRelationship.getParent()))
                return null;
        }
        return firstParent;
    }

    /**
     * Copies a validated+enriched {@link BoundaryRelation} into a {@link BoundaryRelationshipDTO},
     * carrying over the enriched {@code ancestralMaterializedPath} explicitly (it is not copied by
     * BeanUtils onto the wire because it is {@code @JsonIgnore} on the source). Mirrors the field copy
     * that {@link #convertContractPOJOToDTO} performs for the single-create path.
     */
    private BoundaryRelationshipDTO convertRelationPOJOToDTO(BoundaryRelation boundaryRelationship) {
        BoundaryRelationshipDTO boundaryRelationshipDTO = new BoundaryRelationshipDTO();
        BeanUtils.copyProperties(boundaryRelationship, boundaryRelationshipDTO);
        boundaryRelationshipDTO.setAncestralMaterializedPath(boundaryRelationship.getAncestralMaterializedPath());
        return boundaryRelationshipDTO;
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
