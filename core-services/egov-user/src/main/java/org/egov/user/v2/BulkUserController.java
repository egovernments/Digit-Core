package org.egov.user.v2;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.user.domain.model.User;
import org.egov.user.web.contract.factory.ResponseInfoFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * v2 bulk user create endpoint.
 * <p>
 * POST /users/v2/_create
 * <pre>
 * {
 *   "RequestInfo": { ... },
 *   "users": [ { User }, { User }, ... ]
 * }
 * </pre>
 * Response:
 * <pre>
 * {
 *   "ResponseInfo": { ... },
 *   "users": [ { User with id/uuid populated on success, id=null on failure } ]
 * }
 * </pre>
 * <p>
 * v1 endpoints ({@code /users/_createnovalidate} etc.) are untouched.
 */
@RestController
@RequestMapping("/users/v2")
@Slf4j
public class BulkUserController {

    private final BulkUserService bulkUserService;
    private final ResponseInfoFactory responseInfoFactory;
    private final ObjectMapper mapper;

    @Autowired
    public BulkUserController(BulkUserService bulkUserService,
                              ResponseInfoFactory responseInfoFactory,
                              ObjectMapper mapper) {
        this.bulkUserService = bulkUserService;
        this.responseInfoFactory = responseInfoFactory;
        this.mapper = mapper;
    }

    @PostMapping("/_create")
    public Map<String, Object> createUsersInBulk(@RequestBody Map<String, Object> body) {

        RequestInfo requestInfo = mapper.convertValue(body.get("RequestInfo"), RequestInfo.class);
        List<User> users = mapper.convertValue(
                body.get("users"),
                new TypeReference<List<User>>() {});

        if (users == null || users.isEmpty()) {
            return response(requestInfo, Collections.emptyList(), true);
        }

        log.info("v2 bulk create received: {} users, tenantId={}",
                users.size(),
                users.get(0).getTenantId());

        List<User> saved = bulkUserService.createUsersBulk(users, requestInfo);

        long successes = saved.stream().filter(u -> u.getId() != null).count();
        log.info("v2 bulk create completed: {}/{} succeeded", successes, saved.size());

        return response(requestInfo, saved, successes > 0);
    }

    private Map<String, Object> response(RequestInfo requestInfo, List<User> users, boolean success) {
        ResponseInfo responseInfo = responseInfoFactory
                .createResponseInfoFromRequestInfo(requestInfo, success);
        Map<String, Object> body = new HashMap<>();
        body.put("ResponseInfo", responseInfo);
        body.put("users", users);
        return body;
    }
}
