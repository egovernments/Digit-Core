package digit.repository;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import digit.repository.querybuilder.BoundaryEntityQueryBuilder;
import digit.repository.rowmapper.BoundaryEntityRowMapper;
import digit.repository.rowmapper.GeometryRowMapper;
import digit.web.models.Boundary;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static digit.config.ServiceConstants.*;

@Repository
@Slf4j
public class ServiceRequestRepository {

    private final ObjectMapper mapper;
    private final RestTemplate restTemplate;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private BoundaryEntityRowMapper boundaryEntityRowMapper;
    @Autowired
    private GeometryRowMapper geometryRowMapper;
    @Autowired
    private BoundaryEntityQueryBuilder boundaryEntityQueryBuilder;

    @Autowired
    public ServiceRequestRepository(ObjectMapper mapper, RestTemplate restTemplate) {
        this.mapper = mapper;
        this.restTemplate = restTemplate;
    }


    public Object fetchResult(StringBuilder uri, Object request) {
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        Object response = null;
        try {
            response = restTemplate.postForObject(uri.toString(), request, Map.class);
        }catch(HttpClientErrorException e) {
            log.error(EXTERNAL_SERVICE_EXCEPTION,e);
            throw new ServiceCallException(e.getResponseBodyAsString());
        }catch(Exception e) {
            log.error(SEARCHER_SERVICE_EXCEPTION,e);
        }

        return response;
    }

    /**
     * Search boundary entity from database
     * @param codes
     * @return
     */
    public List<Boundary> searchBoundaryEntity(List<String> codes) {
        String query = boundaryEntityQueryBuilder.buildSearchBoundaryQuery(codes);
        Object[] params = new Object[codes.size()];
        for (int i = 0; i < codes.size(); i++) {
            params[i] = codes.get(i);
        }
        List<Boundary> boundaryList = jdbcTemplate.query(query, params, boundaryEntityRowMapper);
        List<String> idList = new ArrayList<>();
        Object[] idParams = new Object[codes.size()];
        for (Boundary boundary : boundaryList) {
            idList.add(boundary.getId());
        }
        String geometryQuery = boundaryEntityQueryBuilder.buildSearchGeometryQuery(idList);
//        jdbcTemplate.queryForList(geometryQuery, params, geometryRowMapper);
        return boundaryList;
    }

}