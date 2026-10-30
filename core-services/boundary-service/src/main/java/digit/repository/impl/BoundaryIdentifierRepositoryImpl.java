package digit.repository.impl;

import digit.config.ApplicationProperties;
import digit.kafka.Producer;
import digit.repository.BoundaryIdentifierRepository;
import digit.repository.querybuilder.BoundaryIdentifierQueryBuilder;
import digit.repository.rowmapper.BoundaryIdentifierRowMapper;
import digit.web.models.BoundaryIdentifier;
import digit.web.models.BoundaryIdentifierGenerateCriteria;
import digit.web.models.BoundaryIdentifierRequest;
import digit.web.models.BoundaryIdentifierSearchCriteria;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@Slf4j
public class BoundaryIdentifierRepositoryImpl implements BoundaryIdentifierRepository {

    private final JdbcTemplate jdbcTemplate;

    private final BoundaryIdentifierRowMapper boundaryIdentifierRowMapper;

    private final BoundaryIdentifierQueryBuilder boundaryIdentifierQueryBuilder;

    private final Producer producer;

    private final ApplicationProperties applicationProperties;

    public BoundaryIdentifierRepositoryImpl(JdbcTemplate jdbcTemplate, BoundaryIdentifierRowMapper boundaryIdentifierRowMapper,
                                            BoundaryIdentifierQueryBuilder boundaryIdentifierQueryBuilder, Producer producer,
                                            ApplicationProperties applicationProperties) {
        this.jdbcTemplate = jdbcTemplate;
        this.boundaryIdentifierRowMapper = boundaryIdentifierRowMapper;
        this.boundaryIdentifierQueryBuilder = boundaryIdentifierQueryBuilder;
        this.producer = producer;
        this.applicationProperties = applicationProperties;
    }

    /**
     * Pushes the request to kafka for persister to pick it up and perform insert.
     * @param boundaryIdentifierRequest
     */
    @Override
    public void create(BoundaryIdentifierRequest boundaryIdentifierRequest) {
        producer.push(applicationProperties.getCreateBoundaryIdentifierTopic(), boundaryIdentifierRequest);
    }

    /**
     * This method is used to search for boundary identifiers
     * @param boundaryIdentifierSearchCriteria
     * @return
     */
    @Override
    public List<BoundaryIdentifier> search(BoundaryIdentifierSearchCriteria boundaryIdentifierSearchCriteria) {

        List<Object> preparedStmtList = new ArrayList<>();

        String query = boundaryIdentifierQueryBuilder.getBoundaryIdentifierSearchQuery(boundaryIdentifierSearchCriteria, preparedStmtList);

        return jdbcTemplate.query(query, preparedStmtList.toArray(), boundaryIdentifierRowMapper);
    }

    /**
     * Pushes the request to kafka for persister to pick it up and perform update.
     * @param boundaryIdentifierRequest
     */
    @Override
    public void update(BoundaryIdentifierRequest boundaryIdentifierRequest) {
        producer.push(applicationProperties.getUpdateBoundaryIdentifierTopic(), boundaryIdentifierRequest);
    }

    /**
     * Returns the codes of all boundaries of the given boundaryType within the hierarchy that do not
     * have an identifier yet. Not paginated.
     * @param generateCriteria
     * @return
     */
    @Override
    public List<String> searchCodesWithoutIdentifier(BoundaryIdentifierGenerateCriteria generateCriteria) {

        List<Object> preparedStmtList = new ArrayList<>();

        String query = boundaryIdentifierQueryBuilder.getCodesWithoutIdentifierQuery(generateCriteria, preparedStmtList);

        return jdbcTemplate.queryForList(query, String.class, preparedStmtList.toArray());
    }

    /**
     * Returns which of the given codes exist in the hierarchy (boundary_relationship). Not paginated.
     * @param tenantId
     * @param hierarchyType
     * @param codes
     * @return
     */
    @Override
    public Set<String> searchCodesInHierarchy(String tenantId, String hierarchyType, Collection<String> codes) {

        if (codes.isEmpty()) {
            return Collections.emptySet();
        }

        List<Object> preparedStmtList = new ArrayList<>();

        String query = boundaryIdentifierQueryBuilder.getCodesInHierarchyQuery(tenantId, hierarchyType, codes, preparedStmtList);

        return new HashSet<>(jdbcTemplate.queryForList(query, String.class, preparedStmtList.toArray()));
    }

    /**
     * Returns which of the given identifierIds are already present in the hierarchy. Not paginated.
     * @param tenantId
     * @param hierarchyType
     * @param identifierIds
     * @return
     */
    @Override
    public Set<String> searchExistingIdentifierIds(String tenantId, String hierarchyType, Collection<String> identifierIds) {

        if (identifierIds.isEmpty()) {
            return Collections.emptySet();
        }

        List<Object> preparedStmtList = new ArrayList<>();

        String query = boundaryIdentifierQueryBuilder.getExistingIdentifierIdsQuery(tenantId, hierarchyType, identifierIds, preparedStmtList);

        return new HashSet<>(jdbcTemplate.queryForList(query, String.class, preparedStmtList.toArray()));
    }

}
