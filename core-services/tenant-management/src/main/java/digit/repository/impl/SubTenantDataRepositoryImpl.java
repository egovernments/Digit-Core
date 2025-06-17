package digit.repository.impl;

import digit.config.ApplicationConfig;
import digit.kafka.Producer;
import digit.repository.SubTenantDataRepository;
import digit.repository.TenantDataRepository;
import digit.repository.querybuilder.SubTenantDataQueryBuilder;
import digit.repository.querybuilder.TenantDataQueryBuilder;
import digit.repository.rowmapper.SubTenantDataRowMapper;
import digit.repository.rowmapper.TenantDataRowMapper;
import digit.web.models.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@Slf4j
public class SubTenantDataRepositoryImpl implements SubTenantDataRepository {

    private Producer producer;
    private JdbcTemplate jdbcTemplate;
    private final SubTenantDataQueryBuilder subTenantDataQueryBuilder;
    private final SubTenantDataRowMapper subTenantDataRowMapper;
    private ApplicationConfig config;

    public SubTenantDataRepositoryImpl(Producer producer, JdbcTemplate jdbcTemplate, SubTenantDataQueryBuilder subTenantDataQueryBuilder, SubTenantDataRowMapper subTenantDataRowMapper, ApplicationConfig config) {
        this.producer = producer;
        this.jdbcTemplate = jdbcTemplate;
        this.subTenantDataQueryBuilder = subTenantDataQueryBuilder;
        this.subTenantDataRowMapper = subTenantDataRowMapper;
        this.config = config;
    }

    @Override
    public void create(SubTenantRequest tenantRequest) {
        producer.push(config.getCreateSubTenantTopic(), tenantRequest);
    }

    @Override
    public void update(SubTenantRequest tenantRequest) {
        producer.push(config.getUpdateTopic(), tenantRequest);

    }

    @Override
    public List<SubTenant> search(SubTenantDataSearchCriteria searchCriteria) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = subTenantDataQueryBuilder.getTenantDataSearchQuery(searchCriteria, preparedStmtList);
        log.info(query);
        return jdbcTemplate.query(query, preparedStmtList.toArray(), subTenantDataRowMapper);
    }
}
