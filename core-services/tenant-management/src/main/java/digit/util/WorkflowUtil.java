package digit.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import digit.config.Configuration;
import static digit.config.ServiceConstants.*;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.common.contract.workflow.*;
import org.egov.common.contract.models.*;
import digit.repository.ServiceRequestRepository;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class WorkflowUtil {

    @Autowired
    private ServiceRequestRepository repository;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private Configuration configs;



    /**
    * Searches the BussinessService corresponding to the businessServiceCode
    * Returns applicable BussinessService for the given parameters
    * @param requestInfo
    * @param tenantId
    * @param businessServiceCode
    * @return
    */
    public BusinessService getBusinessService(RequestInfo requestInfo, String tenantId, String businessServiceCode) {

        StringBuilder url = getSearchURLWithParams(tenantId, businessServiceCode);
        RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(requestInfo).build();
        Object result = repository.fetchResult(url, requestInfoWrapper);
        BusinessServiceResponse response = null;
        try {
            response = mapper.convertValue(result, BusinessServiceResponse.class);
        } catch (IllegalArgumentException e) {
            throw new CustomException(PARSING_ERROR, FAILED_TO_PARSE_BUSINESS_SERVICE_SEARCH);
        }

        if (CollectionUtils.isEmpty(response.getBusinessServices()))
            throw new CustomException(BUSINESS_SERVICE_NOT_FOUND, THE_BUSINESS_SERVICE + businessServiceCode + NOT_FOUND);

        return response.getBusinessServices().get(0);
    }

    /**
    * Calls the workflow service with the given action and updates the status
    * Returns the updated status of the application
    * @param requestInfo
    * @param tenantId
    * @param businessId
    * @param businessServiceCode
    * @param workflow
    * @param wfModuleName
    * @return
    */
    public String updateWorkflowStatus(RequestInfo requestInfo, String tenantId,
        String businessId, String businessServiceCode, Workflow workflow, String wfModuleName) {
        ProcessInstance processInstance = getProcessInstanceForWorkflow(requestInfo, tenantId, businessId,
        businessServiceCode, workflow, wfModuleName);
        ProcessInstanceRequest workflowRequest = new ProcessInstanceRequest(requestInfo, Collections.singletonList(processInstance));
        State state = callWorkFlow(workflowRequest);

        return state.getApplicationStatus();
    }

    /**
    * Creates url for search based on given tenantId and businessServices
    * @param tenantId
    * @param businessService
    * @return
    */
    private StringBuilder getSearchURLWithParams(String tenantId, String businessService) {
        StringBuilder url = new StringBuilder(configs.getWfHost());
        url.append(configs.getWfBusinessServiceSearchPath());
        url.append(TENANTID);
        url.append(tenantId);
        url.append(BUSINESS_SERVICES);
        url.append(businessService);
        return url;
    }

    /**
    * Enriches ProcessInstance Object for Workflow
    * @param requestInfo
    * @param tenantId
    * @param businessId
    * @param businessServiceCode
    * @param workflow
    * @param wfModuleName
    * @return
    */
    private ProcessInstance getProcessInstanceForWorkflow(RequestInfo requestInfo, String tenantId,
        String businessId, String businessServiceCode, Workflow workflow, String wfModuleName) {

        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setBusinessId(businessId);
        processInstance.setAction(workflow.getAction());
        processInstance.setModuleName(wfModuleName);
        processInstance.setTenantId(tenantId);
        processInstance.setBusinessService(getBusinessService(requestInfo, tenantId, businessServiceCode).getBusinessService());
        processInstance.setComment(workflow.getComments());

        if(!CollectionUtils.isEmpty(workflow.getAssignes())) {
            List<User> users = new ArrayList<>();

            workflow.getAssignes().forEach(uuid -> {
                User user = new User();
                user.setUuid(uuid);
                users.add(user);
            });

            processInstance.setAssignes(users);
        }

        return processInstance;
    }

    /**
    * Gets the workflow corresponding to the processInstance
    * @param processInstances
    * @return
    */
    public Map<String, Workflow> getWorkflow(List<ProcessInstance> processInstances) {

        Map<String, Workflow> businessIdToWorkflow = new HashMap<>();

        processInstances.forEach(processInstance -> {
            List<String> userIds = null;

            if(!CollectionUtils.isEmpty(processInstance.getAssignes())){
                userIds = processInstance.getAssignes().stream().map(User::getUuid).collect(Collectors.toList());
            }

            Workflow workflow = Workflow.builder()
                .action(processInstance.getAction())
                .assignes(userIds)
                .comments(processInstance.getComment())
                .build();

            businessIdToWorkflow.put(processInstance.getBusinessId(), workflow);
        });

        return businessIdToWorkflow;
    }

    /**
    * Method to take the ProcessInstanceRequest as parameter and set resultant status
    * @param workflowReq
    * @return
    */
    private State callWorkFlow(ProcessInstanceRequest workflowReq) {
        ProcessInstanceResponse response = null;
        StringBuilder url = new StringBuilder(configs.getWfHost().concat(configs.getWfTransitionPath()));
        Object optional = repository.fetchResult(url, workflowReq);
        response = mapper.convertValue(optional, ProcessInstanceResponse.class);
        return response.getProcessInstances().get(0).getState();
    }
}