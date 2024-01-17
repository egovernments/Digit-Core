package digit.repository.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import digit.config.ApplicationProperties;
import digit.kafka.Producer;
import digit.repository.BoundaryRepository;
import digit.repository.querybuilder.BoundaryEntityQueryBuilder;
import digit.repository.rowmapper.BoundaryEntityRowMapper;
import digit.web.models.Boundary;
import digit.web.models.BoundaryRequest;
import digit.web.models.BoundarySearchCriteria;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class BoundaryRepoSyncImpl implements BoundaryRepository {

    private final ObjectMapper mapper;

    private final RestTemplate restTemplate;

    private final JdbcTemplate jdbcTemplate;

    private final BoundaryEntityRowMapper boundaryEntityRowMapper;

    private final BoundaryEntityQueryBuilder boundaryEntityQueryBuilder;

    private final Producer producer;

    private final ApplicationProperties applicationProperties;

    private String createBoundary =  "INSERT INTO boundary (id, tenantId, code, geometry, additionalDetails, createdBy, lastModifiedBy, createdTime, lastModifiedTime, geom) VALUES (?, ?, ?, ?::jsonb, ?::jsonb, ?, ?, ?, ?, ST_GeomFromGeoJSON(?))";


    public BoundaryRepoSyncImpl(ObjectMapper mapper, RestTemplate restTemplate, JdbcTemplate jdbcTemplate, BoundaryEntityRowMapper boundaryEntityRowMapper, BoundaryEntityQueryBuilder boundaryEntityQueryBuilder, Producer producer, ApplicationProperties applicationProperties) {
        this.mapper = mapper;
        this.restTemplate = restTemplate;
        this.jdbcTemplate = jdbcTemplate;
        this.boundaryEntityRowMapper = boundaryEntityRowMapper;
        this.boundaryEntityQueryBuilder = boundaryEntityQueryBuilder;
        this.producer = producer;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public void create(BoundaryRequest boundaryRequest) {
        boundaryRequest.getBoundary().forEach(boundary -> {
            try {
                jdbcTemplate.update(createBoundary,
                        boundary.getId(),
                        boundary.getTenantId(),
                        boundary.getCode(),
                        mapper.writeValueAsString(boundary.getGeometry()),
                        mapper.writeValueAsString(boundary.getAdditionalDetails()),
                        boundary.getAuditDetails().getCreatedBy(),
                        boundary.getAuditDetails().getLastModifiedBy(),
                        boundary.getAuditDetails().getCreatedTime(),
                        boundary.getAuditDetails().getLastModifiedTime(),
                        mapper.writeValueAsString(boundary.getGeometry())
                );
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            };
        });
    }

    /**
     * This method is used to search for boundary entity
     * @param boundarySearchCriteria
     * @return
     */
    @Override
    public List<Boundary> search(BoundarySearchCriteria boundarySearchCriteria) {

        List<Object> preparedStmtList = new ArrayList<>();

        String query = boundaryEntityQueryBuilder.getBoundaryDataSearchQuery(boundarySearchCriteria , preparedStmtList);

        List<Boundary> boundaryList = jdbcTemplate.query(query , preparedStmtList.toArray() , boundaryEntityRowMapper);

        return boundaryList;
    }

    /**
     * This method implements boundary type hierarchy repository interface. In this implementation
     * it pushes the request to kafka for persister to pick it up and perform update.
     * @param boundaryRequest
     */
    @Override
    public void update(BoundaryRequest boundaryRequest) {
        producer.push(applicationProperties.getUpdateBoundaryTopic() , boundaryRequest);
    }

    /**
     * This method returns the set of codes for a given tenantId
     * @param tenantId
     * @return
     */
    public Set<String> getCodeListByTenantId(String tenantId) {

        // create a boundary search criteria object with the given tenantId
        BoundarySearchCriteria boundarySearchCriteria = new BoundarySearchCriteria();
        boundarySearchCriteria.setTenantId(tenantId);

        // get all the boundary entities for the given tenantId from the database
        List<Boundary> boundaryList = search(boundarySearchCriteria);

        // return the set of codes from the boundary entities
        return boundaryList.stream().map(Boundary::getCode).collect(Collectors.toSet());

    }
}
