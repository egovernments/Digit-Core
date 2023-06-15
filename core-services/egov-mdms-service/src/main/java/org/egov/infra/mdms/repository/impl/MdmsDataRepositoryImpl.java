package org.egov.infra.mdms.repository.impl;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.egov.infra.mdms.config.ApplicationConfig;
import org.egov.infra.mdms.model.Mdms;
import org.egov.infra.mdms.model.MdmsRequest;
import org.egov.infra.mdms.producer.Producer;
import org.egov.infra.mdms.repository.MdmsDataRepository;
import org.egov.infra.mdms.repository.querybuilder.MdmsDataQueryBuilder;
import org.egov.infra.mdms.repository.querybuilder.SchemaDefinitionQueryBuilder;
import org.egov.infra.mdms.repository.rowmapper.MdmsDataRowMapper;
import org.egov.mdms.model.MdmsCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
@Slf4j
public class MdmsDataRepositoryImpl implements MdmsDataRepository {

    private Producer producer;
    private JdbcTemplate jdbcTemplate;
    private ApplicationConfig applicationConfig;
    private MdmsDataQueryBuilder mdmsDataQueryBuilder;
    private MdmsDataRowMapper mdmsDataRowMapper;

    @Autowired
    public MdmsDataRepositoryImpl(Producer producer, JdbcTemplate jdbcTemplate,
                                          ApplicationConfig applicationConfig, MdmsDataQueryBuilder mdmsDataQueryBuilder,
                                  MdmsDataRowMapper mdmsDataRowMapper){
        this.producer = producer;
        this.jdbcTemplate = jdbcTemplate;
        this.applicationConfig = applicationConfig;
        this.mdmsDataQueryBuilder = mdmsDataQueryBuilder;
        this.mdmsDataRowMapper = mdmsDataRowMapper;
    }




    /**
     * @param mdmsRequest
     */
    @Override
    public void create(MdmsRequest mdmsRequest) {
        producer.push(applicationConfig.getSaveMdmsDataTopicName(), mdmsRequest);
    }

    /**
     * @param mdmsRequest
     */
    @Override
    public void update(MdmsRequest mdmsRequest) {
    }

    /**
     * @param schemaCodes
     * @return
     */
    @Override
    public Map<String, JSONArray> search(Set<String> schemaCodes) {
        log.info("Search from database");
        List<Object> preparedStmtList = new ArrayList<>();
        String query = mdmsDataQueryBuilder.getMdmsDataSearchQuery(schemaCodes, preparedStmtList);
        log.info(query);
        return jdbcTemplate.query(query, preparedStmtList.toArray(), mdmsDataRowMapper);
    }
}
