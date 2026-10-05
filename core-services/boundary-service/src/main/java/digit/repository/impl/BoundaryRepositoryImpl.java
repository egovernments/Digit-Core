package digit.repository.impl;


import com.fasterxml.jackson.databind.ObjectMapper;
import digit.config.ApplicationProperties;
import digit.errors.ErrorCodes;
import digit.kafka.Producer;
import digit.repository.BoundaryRepository;
import digit.repository.querybuilder.BoundaryEntityQueryBuilder;
import digit.repository.rowmapper.BoundaryEntityRowMapper;
import digit.web.models.Boundary;
import digit.web.models.BoundaryRequest;
import digit.web.models.BoundarySearchCriteria;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.exception.InvalidTenantIdException;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class BoundaryRepositoryImpl implements BoundaryRepository {

    private final ObjectMapper mapper;

    private final RestTemplate restTemplate;

    private final JdbcTemplate jdbcTemplate;

    private final BoundaryEntityRowMapper boundaryEntityRowMapper;

    private final BoundaryEntityQueryBuilder boundaryEntityQueryBuilder;

    private final Producer producer;

    private final ApplicationProperties applicationProperties;

    private final MultiStateInstanceUtil multiStateInstanceUtil;

    public BoundaryRepositoryImpl(ObjectMapper mapper , RestTemplate restTemplate , JdbcTemplate jdbcTemplate , BoundaryEntityRowMapper boundaryEntityRowMapper
            , BoundaryEntityQueryBuilder boundaryEntityQueryBuilder , Producer producer , ApplicationProperties applicationProperties
            , MultiStateInstanceUtil multiStateInstanceUtil) {
        this.mapper = mapper;
        this.restTemplate = restTemplate;
        this.jdbcTemplate = jdbcTemplate;
        this.boundaryEntityRowMapper = boundaryEntityRowMapper;
        this.boundaryEntityQueryBuilder = boundaryEntityQueryBuilder;
        this.producer = producer;
        this.applicationProperties = applicationProperties;
        this.multiStateInstanceUtil = multiStateInstanceUtil;
    }

    /**
     * This method implements boundary entity repository interface. In this implementation
     * it pushes the request to kafka for persister to pick it up and perform insert.
     * @param boundaryRequest
     */
    @Override
    public void create(BoundaryRequest boundaryRequest) {
        producer.push(boundaryRequest.getBoundary().get(0).getTenantId() , applicationProperties.getCreateBoundaryTopic() , boundaryRequest);
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

        try {
            // Replacing schema placeholder with the schema name for the tenant id
            query = multiStateInstanceUtil.replaceSchemaPlaceholder(query, boundarySearchCriteria.getTenantId());
        } catch (InvalidTenantIdException e) {
            throw new CustomException(ErrorCodes.INVALID_TENANT_ID_CODE, e.getMessage());
        }

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
        producer.push(boundaryRequest.getBoundary().get(0).getTenantId() , applicationProperties.getUpdateBoundaryTopic() , boundaryRequest);
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