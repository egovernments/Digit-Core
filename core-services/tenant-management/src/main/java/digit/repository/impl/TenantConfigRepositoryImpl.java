package digit.repository.impl;

import digit.config.ApplicationConfig;
import digit.kafka.Producer;
import digit.repository.TenantConfigRepository;
import digit.repository.querybuilder.TenantConfigQueryBuilder;
import digit.repository.querybuilder.TenantDataQueryBuilder;
import digit.repository.rowmapper.TenantConfigRowMapper;
import digit.repository.rowmapper.TenantDataRowMapper;
import digit.web.models.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@Slf4j
public class TenantConfigRepositoryImpl implements TenantConfigRepository {

    private Producer producer;
    private JdbcTemplate jdbcTemplate;
    private TenantConfigQueryBuilder tenantConfigQueryBuilder;
    private TenantConfigRowMapper tenantConfigRowMapper;
    private ApplicationConfig config;

    public TenantConfigRepositoryImpl(Producer producer, JdbcTemplate jdbcTemplate, TenantConfigQueryBuilder tenantConfigQueryBuilder, TenantConfigRowMapper tenantConfigRowMapper, ApplicationConfig config) {
        this.producer = producer;
        this.jdbcTemplate = jdbcTemplate;
        this.tenantConfigQueryBuilder = tenantConfigQueryBuilder;
        this.tenantConfigRowMapper = tenantConfigRowMapper;
        this.config = config;
    }

    @Override
    public void create(TenantConfigRequest tenantConfigRequest) {
        producer.push(config.getCreateConfigTopic(), tenantConfigRequest);
    }

    @Override
    public void update(TenantConfigRequest tenantRequest) {
        producer.push(config.getUpdateConfigTopic(), tenantRequest);

    }

    @Override
    public List<TenantConfig> search(TenantConfigSearchCriteria searchCriteria) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = tenantConfigQueryBuilder.getTenantConfigSearchQuery(searchCriteria, preparedStmtList);
        log.info(query);
        return jdbcTemplate.query(query, preparedStmtList.toArray(),tenantConfigRowMapper);
    }

}
