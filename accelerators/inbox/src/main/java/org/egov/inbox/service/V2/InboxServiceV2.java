package org.egov.inbox.service.V2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.wnameless.json.flattener.JsonFlattener;
import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.egov.hash.HashService;
import org.egov.inbox.config.InboxConfiguration;
import org.egov.inbox.repository.ServiceRequestRepository;
import org.egov.inbox.repository.builder.V2.InboxQueryBuilder;
import org.egov.inbox.service.V2.validator.ValidatorDefaultImplementation;
import org.egov.inbox.service.WorkflowService;
import org.egov.inbox.util.MDMSUtil;
import org.egov.inbox.web.model.Inbox;
import org.egov.inbox.web.model.InboxRequest;
import org.egov.inbox.web.model.InboxResponse;
import org.egov.inbox.web.model.V2.*;
import org.egov.inbox.web.model.workflow.BusinessService;
import org.egov.inbox.web.model.workflow.ProcessInstance;
import org.egov.inbox.web.model.workflow.ProcessInstanceSearchCriteria;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;

import static org.egov.inbox.util.InboxConstants.*;

@Service
@Slf4j
public class InboxServiceV2 {

    @Autowired
    private InboxConfiguration config;

    @Autowired
    private InboxQueryBuilder queryBuilder;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private ValidatorDefaultImplementation validator;

    @Autowired
    private MDMSUtil mdmsUtil;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private HashService hashService;

    /**
     *
     * @param inboxRequest
     * @return
     */
    public InboxResponse getInboxResponse(InboxRequest inboxRequest) {

        validator.validateSearchCriteria(inboxRequest);
        InboxQueryConfiguration inboxQueryConfiguration = mdmsUtil.getConfigFromMDMS(
                inboxRequest.getInbox().getTenantId(),
                inboxRequest.getInbox().getProcessSearchCriteria().getModuleName());
        hashParamsWhereverRequiredBasedOnConfiguration(inboxRequest.getInbox().getModuleSearchCriteria(),
                inboxQueryConfiguration);
        List<Inbox> items = getInboxItems(inboxRequest, inboxQueryConfiguration.getIndex());
        enrichProcessInstanceInInboxItems(items);
        Integer totalCount = getTotalApplicationCount(inboxRequest, inboxQueryConfiguration.getIndex());
        List<HashMap<String, Object>> statusCountMap = getStatusCountMap(inboxRequest,
                inboxQueryConfiguration.getIndex());
        Integer nearingSlaCount = CollectionUtils
                .isEmpty(inboxRequest.getInbox().getProcessSearchCriteria().getStatus()) ? 0
                        : getApplicationsNearingSlaCount(inboxRequest, inboxQueryConfiguration.getIndex());
        InboxResponse inboxResponse = InboxResponse.builder().items(items).totalCount(totalCount)
                .statusMap(statusCountMap).nearingSlaCount(nearingSlaCount).build();

        return inboxResponse;
    }

    private void hashParamsWhereverRequiredBasedOnConfiguration(Map<String, Object> moduleSearchCriteria,
            InboxQueryConfiguration inboxQueryConfiguration) {

        inboxQueryConfiguration.getAllowedSearchCriteria().forEach(searchParam -> {
            if (!ObjectUtils.isEmpty(searchParam.getIsHashingRequired()) && searchParam.getIsHashingRequired()) {
                if (moduleSearchCriteria.containsKey(searchParam.getName())) {
                    if (moduleSearchCriteria.get(searchParam.getName()) instanceof List) {
                        List<Object> hashedParams = new ArrayList<>();
                        ((List<?>) moduleSearchCriteria.get(searchParam.getName())).forEach(object -> {
                            hashedParams.add(hashService.getHashValue(object));
                        });
                        moduleSearchCriteria.put(searchParam.getName(), hashedParams);
                    } else {
                        Object hashedValue = hashService.getHashValue(moduleSearchCriteria.get(searchParam.getName()));
                        moduleSearchCriteria.put(searchParam.getName(), hashedValue);
                    }
                }
            }
        });
    }

    private void enrichProcessInstanceInInboxItems(List<Inbox> items) {
        /*
         * As part of the new inbox, having currentProcessInstance as part of the index
         * is mandated. This has been
         * done to avoid having redundant network calls which could hog the performance.
         */
        items.forEach(item -> {
            if (item.getBusinessObject().containsKey(CURRENT_PROCESS_INSTANCE_CONSTANT)) {
                // Set process instance object in the native process instance field declared in
                // the model inbox class.
                ProcessInstance processInstance = mapper.convertValue(
                        item.getBusinessObject().get(CURRENT_PROCESS_INSTANCE_CONSTANT), ProcessInstance.class);
                item.setProcessInstance(processInstance);

                // Remove current process instance from business object in order to avoid having
                // redundant data in response.
                item.getBusinessObject().remove(CURRENT_PROCESS_INSTANCE_CONSTANT);
            }
        });
    }

    private List<Inbox> getInboxItems(InboxRequest inboxRequest, String indexName) {
        List<BusinessService> businessServices = workflowService.getBusinessServices(inboxRequest);
        enrichActionableStatusesFromRole(inboxRequest, businessServices);

        Map<String, Object> finalQueryBody = queryBuilder.getESQuery(inboxRequest, Boolean.TRUE);
        try {
            String q = mapper.writeValueAsString(finalQueryBody);
            log.info("Query: " + q);
        } catch (Exception e) {
            e.printStackTrace();
        }
        StringBuilder uri = getURI(indexName, SEARCH_PATH);
        Object result = serviceRequestRepository.fetchResult(uri, finalQueryBody);
        List<Inbox> inboxItemsList = parseInboxItemsFromSearchResponse(result, businessServices);
        // log.info(result.toString());
        return inboxItemsList;
    }

    /************* ✨ Codeium Command ⭐ *************/
    /**
     * This method takes in the inbox request and list of business services
     * It fetches the actionable statuses for the given role and tenantId
     * It then updates the inbox request with the actionable statuses
     */
    /****** 203bf6c4-77e2-494a-bbba-dc1e36258155 *******/
    private void enrichActionableStatusesFromRole(InboxRequest inboxRequest, List<BusinessService> businessServices) {
        ProcessInstanceSearchCriteria processCriteria = inboxRequest.getInbox().getProcessSearchCriteria();

        HashMap<String, String> StatusIdNameMap = workflowService.getActionableStatusesForRole(
                inboxRequest.getRequestInfo(), businessServices,
                inboxRequest.getInbox().getProcessSearchCriteria());
        log.info(StatusIdNameMap.toString());
        List<String> actionableStatus = new ArrayList<>();
        if (StatusIdNameMap.values().size() > 0 && processCriteria.getStatus() != null
                && !processCriteria.getStatus().isEmpty()) {
            if (!CollectionUtils.isEmpty(processCriteria.getStatus())) {
                processCriteria.getStatus().forEach(status -> {
                    if (StatusIdNameMap.values().contains(status)) {
                        actionableStatus.add(status);
                    }
                });
                inboxRequest.getInbox().getProcessSearchCriteria().setStatus(actionableStatus);
            } else {
                inboxRequest.getInbox().getProcessSearchCriteria().setStatus(new ArrayList<>(StatusIdNameMap.keySet()));
            }
        } else {
            inboxRequest.getInbox().getProcessSearchCriteria().setStatus(new ArrayList<>());
        }
    }

    public Integer getTotalApplicationCount(InboxRequest inboxRequest, String indexName) {

        Map<String, Object> finalQueryBody = queryBuilder.getESQuery(inboxRequest, Boolean.FALSE);
        StringBuilder uri = getURI(indexName, COUNT_PATH);
        Map<String, Object> response = (Map<String, Object>) serviceRequestRepository.fetchResult(uri, finalQueryBody);
        Integer totalCount = 0;
        if (response.containsKey(COUNT_CONSTANT)) {
            totalCount = (Integer) response.get(COUNT_CONSTANT);
        } else {
            throw new CustomException("INBOX_COUNT_ERR", "Error occurred while executing ES count query");
        }
        return totalCount;
    }

    public List<HashMap<String, Object>> getStatusCountMap(InboxRequest inboxRequest, String indexName) {
        List<BusinessService> businessServices = workflowService.getBusinessServices(inboxRequest);

        HashMap<String, String> StatusIdNameMap = workflowService.getActionableStatusesForRole(
                inboxRequest.getRequestInfo(), businessServices,
                inboxRequest.getInbox().getProcessSearchCriteria());
        log.info(StatusIdNameMap.toString());
        List<String> actionableStatus = new ArrayList<>();
        // add keys
        StatusIdNameMap.keySet().forEach(actionableStatus::add);
        Map<String, Object> finalQueryBody = queryBuilder.getStatusCountQuery(inboxRequest);
        log.info("Query for status count: " + finalQueryBody.toString());
        StringBuilder uri = getURI(indexName, SEARCH_PATH);
        Map<String, Object> response = (Map<String, Object>) serviceRequestRepository.fetchResult(uri, finalQueryBody);
        Set<String> actionableStatusHSet = new HashSet<>(actionableStatus);
        HashMap<String, Object> statusCountMap = parseStatusCountMapFromAggregationResponse(response,
                actionableStatusHSet);
        List<HashMap<String, Object>> transformedStatusMap = transformStatusMap(inboxRequest, statusCountMap);
        return transformedStatusMap;
    }

    private Long getApplicationServiceSla(Map<String, Long> businessServiceSlaMap, Map<String, Long> stateUuidSlaMap,
            Object data) {

        Long currentDate = System.currentTimeMillis(); // current time
        Map<String, Object> auditDetails = (Map<String, Object>) ((Map<String, Object>) data).get(AUDIT_DETAILS_KEY);
        if (auditDetails == null) {
            // Check if 'data' is indeed a Map
            if (data instanceof Map) {
                // Cast 'data' to Map
                Map<String, Object> dataMap = (Map<String, Object>) data;

                // Iterate through the first level of the map
                for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
                    // Check if the value is a map and if it contains auditDetails at the second
                    // level
                    if (entry.getValue() instanceof Map) {
                        Map<String, Object> secondLevelMap = (Map<String, Object>) entry.getValue();
                        if (secondLevelMap.containsKey("auditDetails")) {
                            auditDetails = (Map<String, Object>) secondLevelMap.get("auditDetails");
                            break; // Found the first occurrence of auditDetails, no need to continue
                        }
                    }
                }
            }
        }
        String stateUuid = "";
        try {
            stateUuid = JsonPath.read(data, STATE_UUID_PATH);
        } catch (Exception e) {
            log.warn(STATE_UUID_PATH + " not found in data: " + data);
            return null;
        }
        if (stateUuidSlaMap.containsKey(stateUuid)) {
            if (!ObjectUtils.isEmpty(auditDetails.get(LAST_MODIFIED_TIME_KEY))) {
                Long lastModifiedTime = ((Number) auditDetails.get(LAST_MODIFIED_TIME_KEY)).longValue();

                return Long.valueOf(Math.round((stateUuidSlaMap.get(stateUuid) - (currentDate
                        - lastModifiedTime))
                        / ((double) (24 * 60 * 60 * 1000))));
            }
        } else {
            if (!ObjectUtils.isEmpty(auditDetails.get(CREATED_TIME_KEY))) {
                Long createdTime = ((Number) auditDetails.get(CREATED_TIME_KEY)).longValue();
                String businessService = JsonPath.read(data, BUSINESS_SERVICE_PATH);
                Long businessServiceSLA = businessServiceSlaMap.get(businessService);

                return Long.valueOf(Math
                        .round((businessServiceSLA - (currentDate - createdTime)) / ((double) (24 *
                                60 * 60 * 1000))));
            }
        }
        return null;
    }

    private List<HashMap<String, Object>> transformStatusMap(InboxRequest request,
            HashMap<String, Object> statusCountMap) {

        if (CollectionUtils.isEmpty(statusCountMap))
            return null;

        List<BusinessService> businessServices = workflowService.getBusinessServices(request);

        Map<String, String> statusIdToBusinessServiceMap = workflowService
                .getStatusIdToBusinessServiceMap(businessServices);
        Map<String, String> statusIdToApplicationStatusMap = workflowService
                .getApplicationStatusIdToStatusMap(businessServices);
        Map<String, String> statusIdToStateMap = workflowService.getStatusIdToStateMap(businessServices);

        List<HashMap<String, Object>> statusCountMapTransformed = new ArrayList<>();

        for (Map.Entry<String, Object> entry : statusCountMap.entrySet()) {
            String statusId = entry.getKey();
            Integer count = (Integer) entry.getValue();
            if (statusIdToStateMap.get(statusId) != null) {
                HashMap<String, Object> map = new HashMap<>();
                map.put(COUNT_CONSTANT, count);
                map.put(APPLICATION_STATUS_KEY, statusIdToApplicationStatusMap.get(statusId));
                map.put(BUSINESSSERVICE_KEY, statusIdToBusinessServiceMap.get(statusId));
                map.put(STATUSID_KEY, statusId);
                map.put(STATE_KEY, statusIdToStateMap.get(statusId));
                statusCountMapTransformed.add(map);
            }
        }
        return statusCountMapTransformed;
    }

    private HashMap<String, Object> parseStatusCountMapFromAggregationResponse(Map<String, Object> response,
            Set<String> actionableStatuses) {
        List<HashMap<String, Object>> statusCountResponse = new ArrayList<>();
        if (!CollectionUtils.isEmpty((Map<String, Object>) response.get(AGGREGATIONS_KEY))) {
            List<Map<String, Object>> statusCountBuckets = JsonPath.read(response,
                    STATUS_COUNT_AGGREGATIONS_BUCKETS_PATH);
            HashMap<String, Object> statusCountMap = new HashMap<>();
            actionableStatuses.forEach(status -> {
                statusCountMap.put(status, 0);
            });
            statusCountBuckets.forEach(bucket -> {
                statusCountMap.put((String) bucket.get(KEY), bucket.get(DOC_COUNT_KEY));
            });
            statusCountResponse.add(statusCountMap);
        }
        if (CollectionUtils.isEmpty(statusCountResponse))
            return null;

        return statusCountResponse.get(0);
    }

    private List<Inbox> parseInboxItemsFromSearchResponse(Object result, List<BusinessService> businessServices) {
        Map<String, Object> hits = (Map<String, Object>) ((Map<String, Object>) result).get(HITS);
        List<Map<String, Object>> nestedHits = (List<Map<String, Object>>) hits.get(HITS);
        if (CollectionUtils.isEmpty(nestedHits)) {
            return new ArrayList<>();
        }

        Map<String, Long> businessServiceSlaMap = new HashMap<>();
        Map<String, Long> stateUuidVsSlaMap = new HashMap<>();

        businessServices.forEach(businessService -> {
            businessServiceSlaMap.put(businessService.getBusinessService(), businessService.getBusinessServiceSla());
            businessService.getStates().forEach(state -> {
                if (!ObjectUtils.isEmpty(state.getSla()))
                    stateUuidVsSlaMap.put(state.getUuid(), state.getSla());
            });
        });

        List<Inbox> inboxItemList = new ArrayList<>();
        nestedHits.forEach(hit -> {
            Inbox inbox = new Inbox();
            Map<String, Object> businessObject = (Map<String, Object>) hit.get(SOURCE_KEY);
            inbox.setBusinessObject((Map<String, Object>) businessObject.get(DATA_KEY));
            Long serviceSla = getApplicationServiceSla(businessServiceSlaMap, stateUuidVsSlaMap,
                    inbox.getBusinessObject());
            inbox.getBusinessObject().put(SERVICESLA_KEY, serviceSla);
            inboxItemList.add(inbox);
        });
        return inboxItemList;
    }

    public Integer getApplicationsNearingSlaCount(InboxRequest inboxRequest, String indexName) {
        List<BusinessService> businessServicesObjs = workflowService.getBusinessServices(inboxRequest);
        Map<String, Long> businessServiceSlaMap = new HashMap<>();
        Map<String, HashSet<String>> businessServiceVsStateUuids = new HashMap<>();
        businessServicesObjs.forEach(businessService -> {
            List<String> listOfUuids = new ArrayList<>();
            businessService.getStates().forEach(state -> {
                listOfUuids.add(state.getUuid());
            });
            businessServiceVsStateUuids.put(businessService.getBusinessService(), new HashSet<>(listOfUuids));
            businessServiceSlaMap.put(businessService.getBusinessService(), businessService.getBusinessServiceSla());
        });

        List<String> uuidsInSearchCriteria = inboxRequest.getInbox().getProcessSearchCriteria().getStatus();

        Map<String, List<String>> businessServiceVsUuidsBasedOnSearchCriteria = new HashMap<>();

        // If status uuids are being passed in process search criteria, segregating them
        // based on their business service
        if (!CollectionUtils.isEmpty(uuidsInSearchCriteria)) {
            uuidsInSearchCriteria.forEach(uuid -> {
                businessServiceVsStateUuids.keySet().forEach(businessService -> {
                    HashSet<String> setOfUuids = businessServiceVsStateUuids.get(businessService);
                    if (setOfUuids.contains(uuid)) {
                        if (businessServiceVsUuidsBasedOnSearchCriteria.containsKey(businessService)) {
                            businessServiceVsUuidsBasedOnSearchCriteria.get(businessService).add(uuid);
                        } else {
                            businessServiceVsUuidsBasedOnSearchCriteria.put(businessService,
                                    new ArrayList<>(Collections.singletonList(uuid)));
                        }
                    }
                });

            });
        } else {
            businessServiceVsStateUuids.keySet().forEach(businessService -> {
                HashSet<String> setOfUuids = businessServiceVsStateUuids.get(businessService);
                businessServiceVsUuidsBasedOnSearchCriteria.put(businessService, new ArrayList<>(setOfUuids));
            });
        }

        List<String> businessServices = new ArrayList<>(businessServiceVsUuidsBasedOnSearchCriteria.keySet());
        Integer totalCount = 0;
        // Fetch slot percentage only once here !!!!!!!!!!

        for (int i = 0; i < businessServices.size(); i++) {
            String businessService = businessServices.get(i);
            Long businessServiceSla = businessServiceSlaMap.get(businessService);
            inboxRequest.getInbox().getProcessSearchCriteria()
                    .setStatus(businessServiceVsUuidsBasedOnSearchCriteria.get(businessService));
            Map<String, Object> finalQueryBody = queryBuilder.getNearingSlaCountQuery(inboxRequest, businessServiceSla);
            StringBuilder uri = getURI(indexName, COUNT_PATH);
            Map<String, Object> response = (Map<String, Object>) serviceRequestRepository.fetchResult(uri,
                    finalQueryBody);
            Integer currentCount = 0;
            if (response.containsKey(COUNT_CONSTANT)) {
                currentCount = (Integer) response.get(COUNT_CONSTANT);
            } else {
                throw new CustomException("INBOX_COUNT_ERR", "Error occurred while executing ES count query");
            }
            totalCount += currentCount;
        }

        return totalCount;

    }

    private StringBuilder getURI(String indexName, String endpoint) {
        StringBuilder uri = new StringBuilder(config.getIndexServiceHost());
        uri.append(indexName);
        uri.append(endpoint);
        return uri;
    }

    public SearchResponse getSpecificFieldsFromESIndex(SearchRequest searchRequest) {
        String tenantId = searchRequest.getIndexSearchCriteria().getTenantId();
        String moduleName = searchRequest.getIndexSearchCriteria().getModuleName();
        Map<String, Object> moduleSearchCriteria = searchRequest.getIndexSearchCriteria().getModuleSearchCriteria();

        validator.validateSearchCriteria(tenantId, moduleName, moduleSearchCriteria);
        InboxQueryConfiguration inboxQueryConfiguration = mdmsUtil.getConfigFromMDMS(tenantId, moduleName);
        hashParamsWhereverRequiredBasedOnConfiguration(moduleSearchCriteria, inboxQueryConfiguration);
        List<Data> data = getDataFromSimpleSearch(searchRequest, inboxQueryConfiguration.getIndex());
        SearchResponse searchResponse = SearchResponse.builder().data(data).build();
        return searchResponse;
    }

    private List<Data> getDataFromSimpleSearch(SearchRequest searchRequest, String index) {
        Map<String, Object> finalQueryBody = queryBuilder.getESQueryForSimpleSearch(searchRequest, Boolean.TRUE);
        try {
            String q = mapper.writeValueAsString(finalQueryBody);
            log.info("Query: " + q);
        } catch (Exception e) {
            e.printStackTrace();
        }
        StringBuilder uri = getURI(index, SEARCH_PATH);
        Object result = serviceRequestRepository.fetchResult(uri, finalQueryBody);
        List<Data> dataList = parseSearchResponseForSimpleSearch(result);
        return dataList;
    }

    private List<Data> parseSearchResponseForSimpleSearch(Object result) {
        Map<String, Object> hits = (Map<String, Object>) ((Map<String, Object>) result).get(HITS);
        List<Map<String, Object>> nestedHits = (List<Map<String, Object>>) hits.get(HITS);
        if (CollectionUtils.isEmpty(nestedHits)) {
            return new ArrayList<>();
        }

        List<Data> dataList = new ArrayList<>();
        nestedHits.forEach(hit -> {
            Data data = new Data();
            Map<String, Object> sourceObject = (Map<String, Object>) hit.get(SOURCE_KEY);
            Map<String, Object> dataObject = (Map<String, Object>) sourceObject.get(DATA_KEY);
            List<Field> fields = getFieldsFromDataObject(dataObject);
            data.setFields(fields);
            dataList.add(data);
        });

        return dataList;
    }

    private List<Field> getFieldsFromDataObject(Map<String, Object> dataObject) {
        List<Field> listOfFields = new ArrayList<>();
        try {
            Map<String, Object> flattenedDataObject = JsonFlattener.flattenAsMap(mapper.writeValueAsString(dataObject));
            flattenedDataObject.keySet().forEach(key -> {
                Field field = new Field();
                field.setKey(key);
                field.setValue(flattenedDataObject.get(key));
                listOfFields.add(field);
            });
        } catch (JsonProcessingException ex) {
            throw new CustomException("EG_INBOX_GET_FIELDS_ERR", "Error while processing JSON.");
        }
        return listOfFields;
    }
}
