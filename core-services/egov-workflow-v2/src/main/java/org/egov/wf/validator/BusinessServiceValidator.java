package org.egov.wf.validator;

import org.egov.common.contract.request.Role;
import org.egov.common.contract.request.User;
import org.egov.tracer.model.CustomException;
import org.egov.wf.repository.BusinessServiceRepository;
import org.egov.wf.web.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Component
public class BusinessServiceValidator {

    private BusinessServiceRepository repository;

    @Autowired
    public BusinessServiceValidator(BusinessServiceRepository repository) {
        this.repository = repository;
    }

    /**
     * Validates the create request
     * @param request BusinessService create request
     */
    public void validateCreateRequest(BusinessServiceRequest request){
        validateIfExists(request,true);
        validateForDuplicates(request);
        validateUserInfo(request);
        validateUserInfoFields(request);
        validateUserInfoRoles(request);
    }


    /**
     * Validates the update request
     * @param request The update request for the BusinessService
     */
    public void validateUpdate(BusinessServiceRequest request){
        validateIfExists(request,false);
        validateForDuplicates(request);
    }


    /**
     * Validates if any object is duplicated
     * @param request The create or update request
     */
    private void validateForDuplicates(BusinessServiceRequest request){
        List<BusinessService> businessServices = new LinkedList<>();
        Map<String,String> errorMap = new HashMap<>();

        request.getBusinessServices().forEach(businessService -> {
            List<State> states = new LinkedList<>();
            if(businessServices.contains(businessService))
                errorMap.put("INVALID BUSINESSSERVICE","BusinessService: "+businessService.getBusinessService()+" is duplicate");
            else businessServices.add(businessService);
            businessService.getStates().forEach(state -> {
                List<Action> actions = new LinkedList<>();
                if(states.contains(state))
                    errorMap.put("INVALID STATE","State: "+state.getState()+" is duplicate");
                else states.add(state);
                if(!CollectionUtils.isEmpty(state.getActions()))
                    state.getActions().forEach(action -> {
                        if(actions.contains(action))
                            errorMap.put("INVALID ACTION","Action: "+action.getAction()+" is duplicate");
                        else actions.add(action);
                    });
            });
        });

        if(!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }



    /**
     * Validates the request for create and update if it exists in db or not
     * @param request The create or update Request
     * @param isCreate Boolean to specify if the request is create or update
     */
    private void validateIfExists(BusinessServiceRequest request,Boolean isCreate){

        List<BusinessService> businessServicesFromDB = getBusinessServices(request);
        Map<String,String> errorMap = new HashMap<>();

        if(isCreate && !CollectionUtils.isEmpty(businessServicesFromDB)){
            businessServicesFromDB.forEach(businessService -> {
                errorMap.put("INVALID BUSINESSSERVICE","The businessService: "+businessService.getBusinessService()+
                " for tenantId: "+businessService.getTenantId()+" already exists");
            });
        }
        else if(!isCreate){
            if(businessServicesFromDB.size()!=request.getBusinessServices().size())
                businessServicesFromDB.forEach(businessService -> {
                    errorMap.put("INVALID BUSINESSSERVICE","The businessService: "+businessService.getBusinessService()+
                            " for tenantId: "+businessService.getTenantId()+" does not exists");
                });
            validateIds(request.getBusinessServices(),businessServicesFromDB);
        }
        if(!errorMap.isEmpty())
        {
            throw new CustomException(errorMap);
        }
    }



    /**
     * Fetches the BusinessServices in the request object from db
     * @param request The BusinessServiceRequest for create and update
     * @return List of BusinessServices found in db
     */
    private List<BusinessService> getBusinessServices(BusinessServiceRequest request){
        String tenantId = request.getBusinessServices().get(0).getTenantId();
        List<String> businessServiceCodes = new LinkedList<>();
        List<String> stateUuids = new LinkedList<>();
        List<String> actionUuids = new LinkedList<>();

        BusinessServiceSearchCriteria criteria = new BusinessServiceSearchCriteria();
        request.getBusinessServices().forEach(businessService -> {
            businessServiceCodes.add(businessService.getBusinessService());
            businessService.getStates().forEach(state -> {
                stateUuids.add(state.getUuid());
                if(!CollectionUtils.isEmpty(state.getActions())){
                    state.getActions().forEach(action -> {
                        actionUuids.add(action.getUuid());
                    });
                }
            });
        });
        criteria.setTenantId(tenantId);
        criteria.setBusinessServices(businessServiceCodes);

        List<BusinessService> businessServices = repository.getBusinessServices(criteria);
        return businessServices;
    }


    /**
     * Validates if all uuids are in db
     * @param businessServicesFromRequest The BusinessServiceRequest for update
     * @param businessServicesFromDB The search response from db
     */
    private void validateIds(List<BusinessService> businessServicesFromRequest,List<BusinessService> businessServicesFromDB){
        List<String> uuidsFromRequest = getIds(businessServicesFromRequest);
        List<String> uuiidsFromDB = getIds(businessServicesFromDB);
        Boolean ifExists = listEqualsIgnoreOrder(uuidsFromRequest,uuiidsFromDB);
        Map<String,String> errorMap = new HashMap<>();
        if(!ifExists){
            uuidsFromRequest.forEach(uuid -> {
                if(!uuiidsFromDB.contains(uuid))
                    errorMap.put("INVALID UUID","The uuid: "+uuid+" does not exists");
            });
        }
        if(!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }


    /**
     * Extracts all the uuids from the list of businessServices
     * @param businessServices The list of BusinessServices whose ids are to be extracted
     * @return The list of all uuids in the BusinessServicces
     */
    private List<String> getIds(List<BusinessService> businessServices){
        List<String> ids = new LinkedList<>();
        businessServices.forEach(businessService -> {
            ids.add(businessService.getUuid());
            businessService.getStates().forEach(state -> {
                ids.add(state.getUuid());
                if(!CollectionUtils.isEmpty(state.getActions())){
                    state.getActions().forEach(action -> {
                        ids.add(action.getUuid());
                    });
                }
            });
        });
        return ids;
    }


    /**
     * Compares if two list contains same elements
     * @param list1
     * @param list2
     * @param <T>
     * @return Boolean true if both list contains the same elements irrespective of order
     */
    private static <T> boolean listEqualsIgnoreOrder(List<T> list1, List<T> list2) {
        return new HashSet<>(list1).equals(new HashSet<>(list2));
    }

    public void validateUserInfo(BusinessServiceRequest request) {
        User requestinfo = request.getRequestInfo().getUserInfo();
        if (requestinfo == null) {
            throw new CustomException("EG_Workflow_APP_ERR", "userinfo is mandatory for creating WorkflowService");
        }
    }
    public void validateUserInfoRoles(BusinessServiceRequest request) {
        List<Role> roles= request.getRequestInfo().getUserInfo().getRoles();

        if (roles == null) {
            throw new CustomException("EG_Workflow_APP_ERR", "userinfo Roles is mandatory for creating WorkflowService");
        }
    }
    public void validateUserInfoFields(BusinessServiceRequest request) {
        String Uuid = request.getRequestInfo().getUserInfo().getUuid();
        String type = request.getRequestInfo().getUserInfo().getType();
        String username = request.getRequestInfo().getUserInfo().getUserName();
        String emailid = request.getRequestInfo().getUserInfo().getEmailId();

        if (Uuid == null) {
            throw new CustomException("EG_Workflow_APP_ERR", "userinfo UUID is mandatory for creating WorkflowService");
        } else if (username == null) {
            throw new CustomException("EG_Workflow_APP_ERR", "userinfo username is mandatory for creating WorkflowService");
        } else if (emailid == null) {
            throw new CustomException("EG_Workflow_APP_ERR", "userinfo email is mandatory for creating WorkflowService");
        } else if (type == null) {
            throw new CustomException("EG_Workflow_APP_ERR", "userinfo type is mandatory for creating WorkflowService");
        }

    }




}
