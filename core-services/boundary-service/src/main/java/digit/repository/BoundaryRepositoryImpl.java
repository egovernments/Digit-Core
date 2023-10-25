package digit.repository;


import com.fasterxml.jackson.databind.ObjectMapper;
import digit.repository.querybuilder.BoundaryEntityQueryBuilder;
import digit.repository.rowmapper.BoundaryEntityRowMapper;
import digit.web.models.Boundary;
import digit.web.models.BoundarySearchCriteria;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Repository
@Slf4j
public class BoundaryRepositoryImpl implements BoundaryRepository {

    private final ObjectMapper mapper;
    private final RestTemplate restTemplate;
    private final JdbcTemplate jdbcTemplate;
    private final BoundaryEntityRowMapper boundaryEntityRowMapper;
    private final BoundaryEntityQueryBuilder boundaryEntityQueryBuilder;

    @Autowired
    public BoundaryRepositoryImpl(ObjectMapper mapper, RestTemplate restTemplate, JdbcTemplate jdbcTemplate, BoundaryEntityRowMapper boundaryEntityRowMapper, BoundaryEntityQueryBuilder boundaryEntityQueryBuilder) {
        this.mapper = mapper;
        this.restTemplate = restTemplate;
        this.jdbcTemplate = jdbcTemplate;
        this.boundaryEntityRowMapper = boundaryEntityRowMapper;
        this.boundaryEntityQueryBuilder = boundaryEntityQueryBuilder;
    }

    /**
     * This method is used to search for boundary entity
     * @param boundarySearchCriteria
     * @return
     */
    public List<Boundary> searchBoundaryEntity(BoundarySearchCriteria boundarySearchCriteria) {

        List<Object> preparedStmtList = new ArrayList<>();

        String query = boundaryEntityQueryBuilder.getBoundaryDataSearchQuery(boundarySearchCriteria,preparedStmtList);

        List<Boundary> boundaryList = jdbcTemplate.query(query, preparedStmtList.toArray(), boundaryEntityRowMapper);

        return boundaryList;
    }

    /**
     * This method returns the set of codes for a given tenantId
     * @param tenantId
     * @return
     */
    public Set<String> getCodeListByTenantId(String tenantId) {
        Set<String> codesList = new HashSet<>();
        BoundarySearchCriteria boundarySearchCriteria = new BoundarySearchCriteria();
        boundarySearchCriteria.setTenantId(tenantId);
        List<Boundary> boundaryList = searchBoundaryEntity(boundarySearchCriteria);
        for(Boundary boundary:boundaryList) {
            codesList.add(boundary.getCode());
        }
        return codesList;
    }
}