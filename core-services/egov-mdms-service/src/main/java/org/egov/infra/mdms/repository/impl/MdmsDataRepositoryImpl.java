package org.egov.infra.mdms.repository.impl;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.egov.infra.mdms.config.ApplicationConfig;
import org.egov.infra.mdms.constants.SchemaDefinitionConstant;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class MdmsDataRepositoryImpl implements MdmsDataRepository {

    private Producer producer;
    private JdbcTemplate jdbcTemplate;
    private ApplicationConfig applicationConfig;
    private MdmsDataQueryBuilder mdmsDataQueryBuilder;
    private MdmsDataQueryBuilderV2 mdmsDataQueryBuilderV2;
    private MdmsDataRowMapperV2 mdmsDataRowMapperV2;
    private MdmsDataRowMapper mdmsDataRowMapper;

    public static final String TenantsSchema = "tenant.tenants";
    public static final String rateSchema = "ws-services-calculation.WCBillingSlab";

    @Autowired
    public MdmsDataRepositoryImpl(Producer producer, JdbcTemplate jdbcTemplate,
                                  ApplicationConfig applicationConfig, MdmsDataQueryBuilder mdmsDataQueryBuilder,
                                  MdmsDataRowMapperV2 mdmsDataRowMapperV2,
                                  MdmsDataQueryBuilderV2 mdmsDataQueryBuilderV2,
                                  MdmsDataRowMapper mdmsDataRowMapper) {
        this.producer = producer;
        this.jdbcTemplate = jdbcTemplate;
        this.applicationConfig = applicationConfig;
        this.mdmsDataQueryBuilder = mdmsDataQueryBuilder;
        this.mdmsDataRowMapper = mdmsDataRowMapper;
        this.mdmsDataRowMapperV2 = mdmsDataRowMapperV2;
        this.mdmsDataQueryBuilderV2 = mdmsDataQueryBuilderV2;
    }

    /**
     * @param mdmsRequest
     */
    @Override
    public void create(MdmsRequest mdmsRequest) {
        producer.push(applicationConfig.getSaveMdmsDataTopicName(), mdmsRequest);
        if(mdmsRequest.getMdms().getSchemaCode().equalsIgnoreCase(TenantsSchema)){
            log.info("data to reindex:"+mdmsRequest.getMdms());
            producer.push(applicationConfig.getCreateTenantIndex(),mdmsRequest.getMdms());
        }
        if(mdmsRequest.getMdms().getSchemaCode().equalsIgnoreCase(rateSchema)){
            log.info("data to reindex:"+mdmsRequest.getMdms());
            producer.push(applicationConfig.getCreateRateIndex(),mdmsRequest.getMdms());
        }
    }

    /**
     * @param mdmsRequest
     */
    @Override
    public void update(MdmsRequest mdmsRequest) {
        producer.push(applicationConfig.getUpdateMdmsDataTopicName(), mdmsRequest);
        if(mdmsRequest.getMdms().getSchemaCode().equalsIgnoreCase(TenantsSchema)){
            log.info("data to reindex from update:"+mdmsRequest.getMdms());
            producer.push(applicationConfig.getUpdateTenantIndex(),mdmsRequest.getMdms());
        }
        if(mdmsRequest.getMdms().getSchemaCode().equalsIgnoreCase(rateSchema)) {
            log.info("data to reindex:" + mdmsRequest.getMdms());
            producer.push(applicationConfig.getUpdateRateIndex(), mdmsRequest.getMdms());
        }
    }

    /**
     * @param mdmsCriteriaV2
     * @return
     */
    @Override
    public List<Mdms> searchV2(MdmsCriteriaV2 mdmsCriteriaV2) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = mdmsDataQueryBuilderV2.getMdmsDataSearchQuery(mdmsCriteriaV2, preparedStmtList);
        log.info(query);
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
        log.info(query);
        return jdbcTemplate.query(query, preparedStmtList.toArray(), mdmsDataRowMapper);
    }
}
