package org.egov.infra.mdms.repository.impl;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.egov.common.exception.InvalidTenantIdException;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.infra.mdms.config.ApplicationConfig;
import org.egov.infra.mdms.model.Mdms;
import org.egov.infra.mdms.model.MdmsCriteria;
import org.egov.infra.mdms.model.MdmsCriteriaV2;
import org.egov.infra.mdms.model.MdmsRequest;
import org.egov.infra.mdms.producer.Producer;
import org.egov.infra.mdms.repository.MdmsDataRepository;
import org.egov.infra.mdms.repository.querybuilder.MdmsDataQueryBuilder;
import org.egov.infra.mdms.repository.querybuilder.MdmsDataQueryBuilderV2;
import org.egov.infra.mdms.repository.rowmapper.MdmsDataRowMapper;
import org.egov.infra.mdms.repository.rowmapper.MdmsDataRowMapperV2;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.egov.infra.mdms.errors.ErrorCodes.INVALID_TENANT_ID_ERR_CODE;

@Repository
@Slf4j
public class MdmsDataRepositoryImpl implements MdmsDataRepository {

    private final Producer producer;
    private final JdbcTemplate jdbcTemplate;
    private final ApplicationConfig applicationConfig;
    private final MdmsDataQueryBuilder mdmsDataQueryBuilder;
    private final MdmsDataQueryBuilderV2 mdmsDataQueryBuilderV2;
    private final MdmsDataRowMapperV2 mdmsDataRowMapperV2;
    private final MdmsDataRowMapper mdmsDataRowMapper;
    private final MultiStateInstanceUtil multiStateInstanceUtil;

    @Autowired
    public MdmsDataRepositoryImpl(Producer producer, JdbcTemplate jdbcTemplate,
                                  ApplicationConfig applicationConfig, MdmsDataQueryBuilder mdmsDataQueryBuilder,
                                  MdmsDataRowMapperV2 mdmsDataRowMapperV2,
                                  MdmsDataQueryBuilderV2 mdmsDataQueryBuilderV2,
                                  MdmsDataRowMapper mdmsDataRowMapper, MultiStateInstanceUtil multiStateInstanceUtil) {
        this.producer = producer;
        this.jdbcTemplate = jdbcTemplate;
        this.applicationConfig = applicationConfig;
        this.mdmsDataQueryBuilder = mdmsDataQueryBuilder;
        this.mdmsDataRowMapper = mdmsDataRowMapper;
        this.mdmsDataRowMapperV2 = mdmsDataRowMapperV2;
        this.mdmsDataQueryBuilderV2 = mdmsDataQueryBuilderV2;
        this.multiStateInstanceUtil = multiStateInstanceUtil;
    }

    /**
     * @param mdmsRequest
     */
    @Override
    public void create(MdmsRequest mdmsRequest) {
        producer.push(mdmsRequest.getMdms().getTenantId(), applicationConfig.getSaveMdmsDataTopicName(), mdmsRequest);
    }

    /**
     * @param mdmsRequest
     */
    @Override
    public void update(MdmsRequest mdmsRequest) {
        producer.push(mdmsRequest.getMdms().getTenantId(), applicationConfig.getUpdateMdmsDataTopicName(), mdmsRequest);
    }

    /**
     * @param mdmsCriteriaV2
     * @return
     */
    @Override
    public List<Mdms> searchV2(MdmsCriteriaV2 mdmsCriteriaV2) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = mdmsDataQueryBuilderV2.getMdmsDataSearchQuery(mdmsCriteriaV2, preparedStmtList);
        try {
            query = multiStateInstanceUtil.replaceSchemaPlaceholder(query, mdmsCriteriaV2.getTenantId());
        } catch (InvalidTenantIdException e) {
            throw new CustomException(INVALID_TENANT_ID_ERR_CODE, e.getMessage());
        }
        log.info("Mdms Data search query: {}", query);
        return jdbcTemplate.query(query, preparedStmtList.toArray(), mdmsDataRowMapperV2);
    }

    /**
     * @param mdmsCriteria
     * @return
     */
    @Override
    public Map<String, Map<String, JSONArray>> search(MdmsCriteria mdmsCriteria) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = mdmsDataQueryBuilder.getMdmsDataSearchQuery(mdmsCriteria, preparedStmtList);
        try {
            query = multiStateInstanceUtil.replaceSchemaPlaceholder(query, mdmsCriteria.getTenantId());
        } catch (InvalidTenantIdException e) {
            throw new CustomException(INVALID_TENANT_ID_ERR_CODE, e.getMessage());
        }
        log.info("Mdms Data search query: {}", query);
        return jdbcTemplate.query(query, preparedStmtList.toArray(), mdmsDataRowMapper);
    }
}
