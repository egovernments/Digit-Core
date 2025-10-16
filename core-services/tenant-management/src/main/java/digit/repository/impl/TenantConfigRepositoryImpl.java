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

  @Override
  public void updateLastLoginTime(String code, Long loginTime) {
    String query = "UPDATE tenant_config SET lastLoginTime = ? WHERE code = ?";
    jdbcTemplate.update(query, loginTime, code);
    log.info("Updated lastLoginTime for tenant config with code: {}", code);
  }

  @Override
  public List<TenantConfig> findLeastActiveAccounts(Integer limit, Long inactiveSinceTimestamp, Integer inactiveSinceDays) {
    StringBuilder queryBuilder = new StringBuilder();
    queryBuilder.append("SELECT ")
        .append("tc.id AS tenantConfigId, ")
        .append("tc.code, ")
        .append("tc.name, ")
        .append("tc.defaultLoginType, ")
        .append("tc.otpLength, ")
        .append("tc.languages, ")
        .append("tc.enableUserBasedLogin, ")
        .append("tc.additionalAttributes, ")
        .append("tc.isActive AS isActive, ")
        .append("tc.lastLoginTime, ")
        .append("tc.createdBy AS CreatedBy, ")
        .append("tc.lastModifiedBy AS LastModifiedBy, ")
        .append("tc.createdTime AS CreatedTime, ")
        .append("tc.lastModifiedTime AS LastModifiedTime, ")
        .append("td.id AS documentId, ")
        .append("td.tenantId, ")
        .append("td.type, ")
        .append("td.fileStoreId, ")
        .append("td.url, ")
        .append("td.isActive AS documentIsActive, ")
        .append("td.createdBy AS documentCreatedBy, ")
        .append("td.lastModifiedBy AS documentLastModifiedBy, ")
        .append("td.createdTime AS documentCreatedTime, ")
        .append("td.lastModifiedTime AS documentLastModifiedTime ")
        .append("FROM tenant_config tc ")
        .append("LEFT JOIN tenant_documents td ON tc.id = td.tenantConfigId ")
        .append("WHERE tc.isActive = true ");

    List<Object> params = new ArrayList<>();

    // Calculate threshold timestamp
    Long thresholdTimestamp = null;
    if (inactiveSinceTimestamp != null) {
      thresholdTimestamp = inactiveSinceTimestamp;
    } else if (inactiveSinceDays != null && inactiveSinceDays > 0) {
      // Convert days to milliseconds and subtract from current time
      thresholdTimestamp = System.currentTimeMillis() - (inactiveSinceDays * 24L * 60L * 60L * 1000L);
    }

    // Add time-based filter if threshold is set
    if (thresholdTimestamp != null) {
      // Include accounts where:
      // 1. They logged in but lastLoginTime is older than threshold
      // 2. They NEVER logged in (IS NULL) AND were created before threshold
      queryBuilder.append("AND (")
          .append("(tc.lastLoginTime IS NOT NULL AND tc.lastLoginTime < ?) ")
          .append("OR ")
          .append("(tc.lastLoginTime IS NULL AND tc.createdTime < ?)")
          .append(") ");
      params.add(thresholdTimestamp);
      params.add(thresholdTimestamp);
      log.info("Filtering accounts inactive since timestamp: {} ({})", thresholdTimestamp,
          new java.util.Date(thresholdTimestamp));
    }

    queryBuilder.append("ORDER BY COALESCE(tc.lastLoginTime, tc.createdTime) ASC ");
    queryBuilder.append("LIMIT ?");
    params.add(limit);

    log.info("Fetching {} least active accounts with filters - inactiveSinceTimestamp: {}, inactiveSinceDays: {}",
        limit, inactiveSinceTimestamp, inactiveSinceDays);

    return jdbcTemplate.query(queryBuilder.toString(), params.toArray(), tenantConfigRowMapper);
  }

}
