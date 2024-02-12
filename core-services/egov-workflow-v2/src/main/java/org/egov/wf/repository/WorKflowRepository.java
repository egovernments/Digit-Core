package org.egov.wf.repository;


import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.wf.repository.querybuilder.WorkflowQueryBuilder;
import org.egov.wf.repository.rowmapper.WorkflowRowMapper;
import org.egov.wf.util.WorkflowUtil;
import org.egov.wf.web.models.ProcessInstance;
import org.egov.wf.web.models.ProcessInstanceSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Repository
@Slf4j
public class WorKflowRepository {

    private WorkflowQueryBuilder queryBuilder;

    private JdbcTemplate jdbcTemplate;

    private WorkflowRowMapper rowMapper;

    private WorkflowUtil util;


    @Autowired
    public WorKflowRepository(WorkflowQueryBuilder queryBuilder, JdbcTemplate jdbcTemplate, WorkflowRowMapper rowMapper, WorkflowUtil util) {
        this.queryBuilder = queryBuilder;
        this.jdbcTemplate = jdbcTemplate;
        this.rowMapper = rowMapper;
        this.util = util;
    }


    /**
     * Executes the search criteria on the db
     * @param criteria The object containing the params to search on
     * @return The parsed response from the search query
     */
    public List<ProcessInstance> getProcessInstances(ProcessInstanceSearchCriteria criteria){
        List<Object> preparedStmtList = new ArrayList<>();

        List<String> ids = getProcessInstanceIds(criteria);

        if(CollectionUtils.isEmpty(ids))
            return new LinkedList<>();

        String query = queryBuilder.getProcessInstanceSearchQueryById(ids, preparedStmtList);
        query = util.replaceSchemaPlaceholder(query, criteria.getTenantId());
        log.debug("query for status search: "+query+" params: "+preparedStmtList);

        return jdbcTemplate.query(query, rowMapper, preparedStmtList.toArray());
    }



    /**
     *
     * @param criteria
     * @return
     */
    public List<ProcessInstance> getProcessInstancesForUserInbox(ProcessInstanceSearchCriteria criteria){
        List<Object> preparedStmtList = new ArrayList<>();

        if(CollectionUtils.isEmpty(criteria.getStatus()) && CollectionUtils.isEmpty(criteria.getTenantSpecifiStatus()))
            return new LinkedList<>();

        List<String> ids = getInboxSearchIds(criteria);

        if(CollectionUtils.isEmpty(ids))
            return new LinkedList<>();

        String query = queryBuilder.getProcessInstanceSearchQueryById(ids, preparedStmtList);
        query = util.replaceSchemaPlaceholder(query, criteria.getTenantId());
        log.debug("query for status search: "+query+" params: "+preparedStmtList);
        return jdbcTemplate.query(query, rowMapper, preparedStmtList.toArray());
    }

    public Integer getProcessInstancesForUserInboxCount(ProcessInstanceSearchCriteria criteria) {
        List<Object> preparedStmtList = new ArrayList<>();
        criteria.setIsAssignedToMeCount(true);
        String query = queryBuilder.getInboxIdCount(criteria, (ArrayList<Object>) preparedStmtList);
        query = util.replaceSchemaPlaceholder(query, criteria.getTenantId());
        Integer count =  jdbcTemplate.queryForObject(query, Integer.class, preparedStmtList.toArray());
        return count;
    }

    /**
     * Returns the count based on the search criteria
     * @param criteria
     * @return
     */
    public Integer getInboxCount(ProcessInstanceSearchCriteria criteria) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getInboxCount(criteria, preparedStmtList,Boolean.FALSE);
        query = util.replaceSchemaPlaceholder(query, criteria.getTenantId());
        Integer count =  jdbcTemplate.queryForObject(query, preparedStmtList.toArray(), Integer.class);
        return count;
    }

    public Integer getProcessInstancesCount(ProcessInstanceSearchCriteria criteria){
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getProcessInstanceCount(criteria, preparedStmtList,Boolean.FALSE);
        query = util.replaceSchemaPlaceholder(query, criteria.getTenantId());
        return jdbcTemplate.queryForObject(query, Integer.class, preparedStmtList.toArray());
    }

    /**
     * Returns the count based on the search criteria
     * @param criteria
     * @return
     */
    public List getInboxStatusCount(ProcessInstanceSearchCriteria criteria) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getInboxCount(criteria, preparedStmtList,Boolean.TRUE);
        query = util.replaceSchemaPlaceholder(query, criteria.getTenantId());
        log.info(query);
        return jdbcTemplate.queryForList(query, preparedStmtList.toArray());
    }

    public List getProcessInstancesStatusCount(ProcessInstanceSearchCriteria criteria){
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getProcessInstanceCount(criteria, preparedStmtList,Boolean.TRUE);
        query = util.replaceSchemaPlaceholder(query, criteria.getTenantId());
        return  jdbcTemplate.queryForList(query, preparedStmtList.toArray());
    }



    private List<String> getInboxSearchIds(ProcessInstanceSearchCriteria criteria) {
        List<Object> preparedStmtList = new ArrayList<>();
        criteria.setIsAssignedToMeCount(false);
        String query = queryBuilder.getInboxIdQuery(criteria,preparedStmtList,true);
        query = util.replaceSchemaPlaceholder(query, criteria.getTenantId());
        return jdbcTemplate.query(query, new SingleColumnRowMapper<>(String.class), preparedStmtList.toArray());
    }

    private List<String> getProcessInstanceIds(ProcessInstanceSearchCriteria criteria) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getProcessInstanceIds(criteria,preparedStmtList);
        query = util.replaceSchemaPlaceholder(query, criteria.getTenantId());
        log.info(query);
        log.info(preparedStmtList.toString());
        return jdbcTemplate.query(query, new SingleColumnRowMapper<>(String.class), preparedStmtList.toArray());
    }


    public List<String> fetchEscalatedApplicationsBusinessIdsFromDb(RequestInfo requestInfo,ProcessInstanceSearchCriteria criteria) {
        ArrayList<Object> preparedStmtList = new ArrayList<>();

        // 1st step is to fetch businessIds based on the assignee and the module.
        /*

        String query = queryBuilder.getInboxApplicationsBusinessIdsQuery(criteria, preparedStmtList);
        List<String> inboxApplicationsBusinessIds = jdbcTemplate.query(query, preparedStmtList.toArray(), new SingleColumnRowMapper<>(String.class));
        log.info(inboxApplicationsBusinessIds.toString());
        preparedStmtList.clear();

        // (DONE) 2nd step is to fetch businessIds of inbox applications which have been autoEscalated at least once in their wf
        // (DONE) For this step, fetch AUTO_ESCALATION_EMPLOYEES uuids based on role codes by doing a call to user service
        // (PENDING) Also, add the call to mdms service for filtering out states which need to be excluded

        criteria.setBusinessIds(inboxApplicationsBusinessIds);
         */

        String query = queryBuilder.getAutoEscalatedApplicationsBusinessIdsQuery(criteria, preparedStmtList);
        query = util.replaceSchemaPlaceholder(query, criteria.getTenantId());
        List<String> escalatedApplicationsBusinessIds = jdbcTemplate.query(query, new SingleColumnRowMapper<>(String.class), preparedStmtList.toArray());
        preparedStmtList.clear();
        log.info(escalatedApplicationsBusinessIds.toString());
        // 3rd step is to do a simple search on these business ids(DONE IN WORKFLOW SERVICE)

        return escalatedApplicationsBusinessIds;
    }

    public Integer getEscalatedApplicationsCount(RequestInfo requestInfo,ProcessInstanceSearchCriteria criteria) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getEscalatedApplicationsCount(requestInfo,criteria, (ArrayList<Object>) preparedStmtList);
        query = util.replaceSchemaPlaceholder(query, criteria.getTenantId());
        Integer count =  jdbcTemplate.queryForObject(query, Integer.class, preparedStmtList.toArray());
        return count;
    }
}