package org.egov.pgr.util;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.pgr.config.PGRConfiguration;
import org.egov.pgr.repository.ServiceRequestRepository;
import org.egov.pgr.web.models.RequestInfoWrapper;
import org.egov.pgr.web.models.User;
import org.egov.pgr.web.models.user.UserDetailResponse;
import org.egov.pgr.web.models.user.UserSearchRequest;
import org.egov.pgr.web.models.workflow.BusinessServiceResponse;
import org.egov.pgr.web.models.workflow.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.egov.pgr.util.PGRConstants.*;

@Slf4j
@Component
public class MigrationUtils {


    private UserUtils userUtils;

    private PGRConfiguration config;

    private ObjectMapper mapper;

    private ServiceRequestRepository repository;

    private MDMSUtils mdmsUtils;

    @Autowired
    public MigrationUtils(UserUtils userUtils, PGRConfiguration config, ObjectMapper mapper, ServiceRequestRepository repository, MDMSUtils mdmsUtils) {
        this.userUtils = userUtils;
        this.config = config;
        this.mapper = mapper;
        this.repository = repository;
        this.mdmsUtils = mdmsUtils;
    }





    public Map<Long, String> getIdtoUUIDMap(List<String> ids) {

        /**
         * calls the user search API based on the given list of user uuids
         * @param uuids
         * @return
         */

        ids.removeAll(Collections.singleton(null));

        UserSearchRequest userSearchRequest = new UserSearchRequest();

        if (!CollectionUtils.isEmpty(ids))
            userSearchRequest.setId(ids);

        StringBuilder uri = new StringBuilder(config.getUserHost()).append(config.getUserSearchEndpoint());
        UserDetailResponse userDetailResponse = userUtils.userCall(userSearchRequest, uri);
        List<User> users = userDetailResponse.getUser();

        if (CollectionUtils.isEmpty(users))
            throw new RuntimeException("PGR_ERROR");


        //Map<Long, String> idToUuidMap = users.stream().collect(Collectors.toMap(User::getId, User::getUuid));


        return new HashMap<>();

    }




    public Map<String,String> getStatusToUUIDMap(String tenantId) {
        StringBuilder url = getSearchURLWithParams(tenantId, PGR_BUSINESSSERVICE);
        RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(new RequestInfo()).build();
        Object result = repository.fetchResult(url, requestInfoWrapper);
        BusinessServiceResponse response = null;
        try {
            response = mapper.convertValue(result, BusinessServiceResponse.class);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("PGR_ERROR");
        }

        if (CollectionUtils.isEmpty(response.getBusinessServices()))
            throw new RuntimeException("PGR_ERROR");


        Map<String,String> statusToUUIDMap = response.getBusinessServices().get(0).getStates().stream()
                .collect(Collectors.toMap(State::getState,State::getUuid));

        return statusToUUIDMap;
    }





    private StringBuilder getSearchURLWithParams(String tenantId, String businessService) {

        StringBuilder url = new StringBuilder(config.getWfHost());
        url.append(config.getWfBusinessServiceSearchPath());
        url.append("?tenantId=");
        url.append(tenantId);
        url.append("&businessServices=");
        url.append(businessService);
        return url;
    }


}
