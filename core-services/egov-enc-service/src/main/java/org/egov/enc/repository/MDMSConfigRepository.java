package org.egov.enc.repository;

import org.egov.enc.models.MDMSConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@ConditionalOnProperty( value = "egov.mdms.provider", havingValue = "org.egov.enc.masterdata.provider.DBMasterDataProvider")
public class MDMSConfigRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String selectAllConfigurationsQuery = "select * from eg_enc_mdms_config";


    public List<MDMSConfig> fetchMDMSConfig() throws DataAccessException {
        return jdbcTemplate.query(selectAllConfigurationsQuery, new BeanPropertyRowMapper<>(MDMSConfig.class));
    }
}
