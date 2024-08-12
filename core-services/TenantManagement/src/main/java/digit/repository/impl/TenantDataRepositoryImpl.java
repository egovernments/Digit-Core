package digit.repository.impl;

import digit.kafka.Producer;
import digit.repository.TenantDataRepository;
import digit.repository.querybuilder.TenantDataQueryBuilder;
import digit.repository.rowmapper.TenantDataRowMapper;
import digit.web.models.Tenant;
import digit.web.models.TenantDataSearchCriteria;
import digit.web.models.TenantRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@Slf4j
public class TenantDataRepositoryImpl implements TenantDataRepository {

    private Producer producer;
    private JdbcTemplate jdbcTemplate;
    private TenantDataQueryBuilder tenantDataQueryBuilder;
    private TenantDataRowMapper tenantDataRowMapper;

    public TenantDataRepositoryImpl(Producer producer, JdbcTemplate jdbcTemplate, TenantDataQueryBuilder tenantDataQueryBuilder, TenantDataRowMapper tenantDataRowMapper) {
        this.producer = producer;
        this.jdbcTemplate = jdbcTemplate;
        this.tenantDataQueryBuilder = tenantDataQueryBuilder;
        this.tenantDataRowMapper = tenantDataRowMapper;
    }

    @Override
    public void create(TenantRequest tenantRequest) {
        producer.push("tenant_data",tenantRequest);
    }

    @Override
    public void update(TenantRequest tenantRequest) {

    }

    @Override
    public List<Tenant> search(TenantDataSearchCriteria searchCriteria) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = tenantDataQueryBuilder.getTenantDataSearchQuery(searchCriteria, preparedStmtList);
        log.info(query);
        return jdbcTemplate.query(query, preparedStmtList.toArray(),tenantDataRowMapper);
    }
}
