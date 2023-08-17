package org.egov.wf.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.egov.wf.config.WorkflowConfig;
import org.egov.wf.repository.querybuilder.BusinessServiceQueryBuilder;
import org.egov.wf.repository.rowmapper.BusinessServiceRowMapper;
import org.egov.wf.service.MDMSService;
import org.egov.wf.util.WorkflowUtil;
import org.egov.wf.web.models.Action;
import org.egov.wf.web.models.BusinessService;
import org.egov.wf.web.models.BusinessServiceSearchCriteria;
import org.egov.wf.web.models.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class BusinessServiceRepository {


    private BusinessServiceQueryBuilder queryBuilder;

    private JdbcTemplate jdbcTemplate;

    private BusinessServiceRowMapper rowMapper;

    private WorkflowConfig config;

    private MDMSService mdmsService;
    
    @Autowired
    private  WorkflowUtil util;


    @Autowired
    public BusinessServiceRepository(BusinessServiceQueryBuilder queryBuilder, JdbcTemplate jdbcTemplate,
                                     BusinessServiceRowMapper rowMapper, WorkflowConfig config, MDMSService mdmsService) {
        this.queryBuilder = queryBuilder;
        this.jdbcTemplate = jdbcTemplate;
        this.rowMapper = rowMapper;
        this.config = config;
        this.mdmsService = mdmsService;
    }






    public List<BusinessService> getBusinessServices(BusinessServiceSearchCriteria criteria){
        String query;
        String tenantId = criteria.getTenantId();
        criteria.setTenantId(null);
        List<Object> preparedStmtList = new ArrayList<>();
        query = queryBuilder.getBusinessServices(criteria, preparedStmtList);
        query = util.replaceSchemaPlaceholder(query, tenantId);
        List<BusinessService> searchResults = jdbcTemplate.query(query, preparedStmtList.toArray(), rowMapper);

        if(CollectionUtils.isEmpty(searchResults))
            return new LinkedList<>();

        List<String> tenantHierarchy = getTenantHierarchy(tenantId);
        List<BusinessService> businessServices = new LinkedList<>();

        List<String> businessServicesCodesFound = new LinkedList<>();

        for(String t : tenantHierarchy){

            for (BusinessService businessService : searchResults){

                if(businessService.getTenantId().equalsIgnoreCase(t) && !businessServicesCodesFound.contains(businessService.getBusinessService())){
                    businessServicesCodesFound.add(businessService.getBusinessService());
                    businessServices.add(businessService);
                }

            }
        }

        return businessServices;
    }


    /**
     * Creates map of roles vs tenantId vs List of status uuids from all the available businessServices
     * @return
     */
    @Cacheable(value = "roleTenantAndStatusesMapping", key = "#tenantIdForState")
    public Map<String,Map<String,List<String>>> getRoleTenantAndStatusMapping(String tenantIdForState){


        Map<String, Map<String,List<String>>> roleTenantAndStatusMapping = new HashMap();

        List<BusinessService> businessServices = getAllBusinessService(tenantIdForState);

        for(BusinessService businessService : businessServices){

            String tenantId = businessService.getTenantId();

            for(State state : businessService.getStates()){

                String uuid = state.getUuid();

                if(!CollectionUtils.isEmpty(state.getActions())){

                    for(Action action : state.getActions()){

                        List<String> roles = action.getRoles();

                        if(!CollectionUtils.isEmpty(roles)){
                            for(String role : roles){

                                Map<String, List<String>> tenantToStatusMap;

                                if (roleTenantAndStatusMapping.containsKey(role))
                                    tenantToStatusMap = roleTenantAndStatusMapping.get(role);
                                else tenantToStatusMap = new HashMap();

                                List<String> statuses;

                                if(tenantToStatusMap.containsKey(tenantId))
                                    statuses = tenantToStatusMap.get(tenantId);
                                else statuses = new LinkedList<>();

                                statuses.add(uuid);

                                tenantToStatusMap.put(tenantId, statuses);
                                roleTenantAndStatusMapping.put(role, tenantToStatusMap);
                            }
                        }
                    }

                }

            }

        }

        return roleTenantAndStatusMapping;

    }

    /**
     * Returns all the available businessServices
     * @return
     */
    private List<BusinessService> getAllBusinessService(String tenantIdForState){

        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getBusinessServices(new BusinessServiceSearchCriteria(), preparedStmtList);
        query =  util.replaceSchemaPlaceholder(query, tenantIdForState);
        List<BusinessService> businessServices = jdbcTemplate.query(query, preparedStmtList.toArray(), rowMapper);

        return businessServices;
    }


    /**
     * Will filter out configurations which are not in sync with MDMS master data
     * @param businessServices
     * @return
     */
    private List<BusinessService> filterBusinessServices(List<BusinessService> businessServices){

        Map<String, Boolean> stateLevelMapping = mdmsService.getStateLevelMapping();
        List<BusinessService> filteredBusinessService = new LinkedList<>();

        for(BusinessService businessService : businessServices){

            String code = businessService.getBusinessService();
            String tenantId = businessService.getTenantId();
            Boolean isStatelevel = stateLevelMapping.get(code);

            if(isStatelevel == null){
                isStatelevel = true;
               // throw new CustomException("INVALID_MDMS_CONFIG","The master data is missing for businessService: "+code);
            }

            if(isStatelevel){
                if(tenantId.equalsIgnoreCase(config.getStateLevelTenantId())){
                    filteredBusinessService.add(businessService);
                }
            }
            else {
                if(!tenantId.equalsIgnoreCase(config.getStateLevelTenantId())){
                    filteredBusinessService.add(businessService);
                }
            }
        }

        return filteredBusinessService;
    }


    private List<String> getTenantHierarchy(String tenantId){

        String NAMESPACE_SEPARATOR = ".";
        Integer tenantDepth = StringUtils.countMatches(tenantId, NAMESPACE_SEPARATOR);
        ArrayList<String> tenantHierarchy = new ArrayList<>();
        tenantHierarchy.add(tenantId);
        for (int index = tenantDepth; index >= 1; index--) {
            String tenant = tenantId.substring(0,
                    StringUtils.ordinalIndexOf(tenantId, NAMESPACE_SEPARATOR, index));
            tenantHierarchy.add(tenant);
        }

        return tenantHierarchy;

    }





}
