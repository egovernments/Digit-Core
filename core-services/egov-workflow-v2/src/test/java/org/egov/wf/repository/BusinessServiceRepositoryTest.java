package org.egov.wf.repository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.egov.wf.config.WorkflowConfig;
import org.egov.wf.repository.querybuilder.BusinessServiceQueryBuilder;
import org.egov.wf.repository.rowmapper.BusinessServiceRowMapper;
import org.egov.wf.service.MDMSService;
import org.egov.wf.util.WorkflowUtil;
import org.egov.wf.web.models.BusinessServiceSearchCriteria;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import ch.qos.logback.core.util.COWArrayList;

@ContextConfiguration(classes = {BusinessServiceRepository.class})
@ExtendWith(SpringExtension.class)
class BusinessServiceRepositoryTest {
    @MockBean
    private BusinessServiceQueryBuilder businessServiceQueryBuilder;

    @MockBean
    private BusinessServiceRowMapper businessServiceRowMapper;

    @MockBean
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private MDMSService mDMSService;

    @MockBean
    private WorkflowConfig workflowConfig;
    
    @MockBean
    private WorkflowUtil workflowUtil;


    @Test
    void testGetBusinessServicesWithnull() throws DataAccessException {

        when(this.mDMSService.getStateLevelMapping()).thenReturn(new HashMap<>());
        when(this.jdbcTemplate.query((String) any(), (Object[]) any(),
                (org.springframework.jdbc.core.ResultSetExtractor<Object>) any())).thenReturn(null);
        when(this.businessServiceQueryBuilder.getBusinessServices((BusinessServiceSearchCriteria) any(),
                (List<Object>) any())).thenReturn("Business Services");

        ArrayList<String> stringList = new ArrayList<>();
        stringList.add("foo");
        ArrayList<String> stateUuids = new ArrayList<>();

           }


    @Test
    void testGetBusinessServiceswithstring() throws DataAccessException {

        when(this.mDMSService.getStateLevelMapping()).thenReturn(new HashMap<>());
        when(this.jdbcTemplate.query((String) any(), (Object[]) any(),
                (org.springframework.jdbc.core.ResultSetExtractor<Object>) any())).thenReturn("Query");
        when(this.businessServiceQueryBuilder.getBusinessServices((BusinessServiceSearchCriteria) any(),
                (List<Object>) any())).thenReturn("Business Services");

        ArrayList<String> stringList = new ArrayList<>();
        stringList.add("foo");
        ArrayList<String> stateUuids = new ArrayList<>();

    }

    @Test
    void testGetRoleTenantAndStatusMappingwithnull() throws DataAccessException {


        when(this.mDMSService.getStateLevelMapping()).thenReturn(new HashMap<>());
        when(this.jdbcTemplate.query((String) any(), (Object[]) any(),
                (org.springframework.jdbc.core.ResultSetExtractor<Object>) any())).thenReturn(null);
        when(this.businessServiceQueryBuilder
                .getBusinessServices((org.egov.wf.web.models.BusinessServiceSearchCriteria) any(), (List<Object>) any()))
                .thenReturn("Business Services");

    }


    @Test
    void testGetRoleTenantAndStatusMapping() throws DataAccessException {

        when(this.mDMSService.getStateLevelMapping()).thenReturn(new HashMap<>());
        when(this.jdbcTemplate.query((String) any(), (Object[]) any(),
                (org.springframework.jdbc.core.ResultSetExtractor<Object>) any()))
                .thenReturn((COWArrayList<Object>) mock(COWArrayList.class));
        when(this.businessServiceQueryBuilder
                .getBusinessServices((org.egov.wf.web.models.BusinessServiceSearchCriteria) any(), (List<Object>) any()))
                .thenReturn("Business Services");

    }
}

