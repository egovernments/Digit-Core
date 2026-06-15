package org.egov.user.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.user.domain.model.*;
import org.egov.user.domain.service.TokenService;
import org.egov.user.domain.service.UserService;
import org.egov.user.web.contract.*;
import org.egov.user.web.contract.auth.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.egov.tracer.http.HttpUtils.isInterServiceCall;
import static org.springframework.util.CollectionUtils.isEmpty;

@RestController
@Slf4j
public class UserController {

    private UserService userService;
    private TokenService tokenService;

    @Value("${mobile.number.validation.workaround.enabled}")
    private String mobileValidationWorkaroundEnabled;

    @Value("${otp.validation.register.mandatory}")
    private boolean IsValidationMandatory;

    @Value("${citizen.registration.withlogin.enabled}")
    private boolean isRegWithLoginEnabled;

    @Autowired
    public UserController(UserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    @PostMapping("/citizen/_create")
    public Object createCitizen(@RequestBody @Valid CreateUserRequest createUserRequest) {
        log.info("Received Citizen Registration Request");
        User user = createUserRequest.toDomain(true);
        user.setOtpValidationMandatory(IsValidationMandatory);
        if (isRegWithLoginEnabled) {
            Object object = userService.registerWithLogin(user, createUserRequest.getRequestInfo());
            return new ResponseEntity<>(object, HttpStatus.OK);
        }
        User createdUser = userService.createCitizen(user, createUserRequest.getRequestInfo());
        return createResponse(createdUser);
    }

    @PostMapping("/users/_createnovalidate")
    public UserDetailResponse createUserWithoutValidation(@RequestBody @Valid CreateUserRequest createUserRequest,
            @RequestHeader HttpHeaders headers) {
        User user = createUserRequest.toDomain(true);
        user.setMobileValidationMandatory(isMobileValidationRequired(headers));
        user.setOtpValidationMandatory(false);
        final User newUser = userService.createUser(user, createUserRequest.getRequestInfo());
        return createResponse(newUser);
    }

    @PostMapping("/_search")
    public UserSearchResponse get(@RequestBody @Valid UserSearchRequest request, @RequestHeader HttpHeaders headers) {
        log.info("Received User search Request");
        if (request.getActive() == null) {
            request.setActive(true);
        }
        return searchUsers(request, headers);
    }

    @PostMapping("/v1/_search")
    public UserSearchResponse getV1(@RequestBody UserSearchRequest request, @RequestHeader HttpHeaders headers) {
        return searchUsers(request, headers);
    }

    @PostMapping("/_details")
    public CustomUserDetails getUser(@RequestParam(value = "access_token") String accessToken) {
        final UserDetail userDetail = tokenService.getUser(accessToken);
        return new CustomUserDetails(userDetail);
    }

    @PostMapping("/users/_updatenovalidate")
    public UpdateResponse updateUserWithoutValidation(@RequestBody final @Valid CreateUserRequest createUserRequest,
            @RequestHeader HttpHeaders headers) {
        User user = createUserRequest.toDomain(false);
        user.setMobileValidationMandatory(isMobileValidationRequired(headers));
        final User updatedUser = userService.updateWithoutOtpValidation(user, createUserRequest.getRequestInfo());
        return createResponseforUpdate(updatedUser);
    }

    @PostMapping("/profile/_update")
    public UpdateResponse patch(@RequestBody final @Valid CreateUserRequest createUserRequest) {
        log.info("Received Profile Update Request  " + createUserRequest);
        User user = createUserRequest.toDomain(false);
        final User updatedUser = userService.partialUpdate(user, createUserRequest.getRequestInfo());
        return createResponseforUpdate(updatedUser);
    }

    private UserDetailResponse createResponse(User newUser) {
        UserRequest userRequest = new UserRequest(newUser);
        ResponseInfo responseInfo = ResponseInfo.builder().status(String.valueOf(HttpStatus.OK.value())).build();
        return new UserDetailResponse(responseInfo, Collections.singletonList(userRequest));
    }

    private UpdateResponse createResponseforUpdate(User newUser) {
        UpdateRequest updateRequest = new UpdateRequest(newUser);
        ResponseInfo responseInfo = ResponseInfo.builder().status(String.valueOf(HttpStatus.OK.value())).build();
        return new UpdateResponse(responseInfo, Collections.singletonList(updateRequest));
    }

    private UserSearchResponse searchUsers(UserSearchRequest request, HttpHeaders headers) {
        UserSearchCriteria searchCriteria = request.toDomain();
        List<User> userModels = userService.searchUsers(searchCriteria, isInterServiceCall(headers), request.getRequestInfo());
        List<UserSearchResponseContent> userContracts = userModels.stream()
                .map(UserSearchResponseContent::new)
                .collect(Collectors.toList());
        ResponseInfo responseInfo = ResponseInfo.builder().status(String.valueOf(HttpStatus.OK.value())).build();
        return new UserSearchResponse(responseInfo, userContracts);
    }

    private boolean isMobileValidationRequired(HttpHeaders headers) {
        boolean fromGateway = !isInterServiceCall(headers);
        if (mobileValidationWorkaroundEnabled != null && Boolean.valueOf(mobileValidationWorkaroundEnabled)
                && !fromGateway) {
            return false;
        }
        return true;
    }
}
