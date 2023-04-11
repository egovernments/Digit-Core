package org.egov.inbox.service.V2.validator;


import org.egov.inbox.util.MDMSUtil;
import org.egov.inbox.web.model.InboxRequest;
import org.egov.inbox.web.model.V2.InboxQueryConfiguration;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.Map;

import static org.egov.inbox.util.InboxConstants.SORT_BY_CONSTANT;
import static org.egov.inbox.util.InboxConstants.SORT_ORDER_CONSTANT;

@Component
public class ValidatorDefaultImplementation implements SearchCriteriaValidatorInterface {


    @Autowired
    private MDMSUtil mdmsUtil;

    @Override
    public void validateSearchCriteria(InboxRequest inboxRequest) {
        InboxQueryConfiguration config = mdmsUtil.getConfigFromMDMS(inboxRequest.getInbox().getTenantId(), inboxRequest.getInbox().getProcessSearchCriteria().getModuleName());
        
        Map<String, Boolean> isMandatoryMap = new HashMap<>();
        
        config.getAllowedSearchCriteria().forEach(
                searchParam -> {
                    isMandatoryMap.put(searchParam.getName(), ObjectUtils.isEmpty(searchParam.getIsMandatory()) ? Boolean.FALSE : searchParam.getIsMandatory());
                }
        );

        HashMap<String,Object> moduleSearchCriteria = inboxRequest.getInbox().getModuleSearchCriteria();

        Map<String, String> errorMap  = new HashMap<>();
        for(Map.Entry<String, Object> entry: moduleSearchCriteria.entrySet()){

            String key = entry.getKey();
            Object value = entry.getValue();

            if(!(key.equals(SORT_ORDER_CONSTANT) || key.equals(SORT_BY_CONSTANT))) {

                if (isMandatoryMap.get(key)) {
                    if (ObjectUtils.isEmpty(value)) {
                        errorMap.put("INVALID_SEARCH_CRITERIA", "Field cannot be null or empty: " + key);
                    }
                }
            }
        }

        if(!CollectionUtils.isEmpty(errorMap))
            throw new CustomException(errorMap);

    }

    public void validateSearchCriteria(String tenantId, String moduleName, Map<String, Object> moduleSearchCriteria) {
        InboxQueryConfiguration config = mdmsUtil.getConfigFromMDMS(tenantId, moduleName);

        Map<String, Boolean> isMandatoryMap = new HashMap<>();

        config.getAllowedSearchCriteria().forEach(
                searchParam -> {
                    isMandatoryMap.put(searchParam.getName(), ObjectUtils.isEmpty(searchParam.getIsMandatory()) ? Boolean.FALSE : searchParam.getIsMandatory());
                }
        );

        Map<String, String> errorMap  = new HashMap<>();
        for(Map.Entry<String, Object> entry: moduleSearchCriteria.entrySet()){

            String key = entry.getKey();
            Object value = entry.getValue();

            if(!(key.equals(SORT_ORDER_CONSTANT) || key.equals(SORT_BY_CONSTANT))) {

                if (isMandatoryMap.get(key)) {
                    if (ObjectUtils.isEmpty(value)) {
                        errorMap.put("INVALID_SEARCH_CRITERIA", "Field cannot be null or empty: " + key);
                    }
                }
            }
        }

        if(!CollectionUtils.isEmpty(errorMap))
            throw new CustomException(errorMap);

    }





}
