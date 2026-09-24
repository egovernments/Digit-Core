package org.egov.user.web.controller;

import org.egov.common.contract.response.Error;
import org.egov.common.contract.response.ErrorResponse;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.user.config.UserServiceConstants;
import org.egov.user.domain.model.TokenWrapper;
import org.egov.user.domain.service.UserSessionService;
import org.egov.user.persistence.repository.UserSessionLogoutEventRepository;
import org.egov.user.web.contract.auth.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LogoutController {

    private final TokenStore tokenStore;
    private final UserSessionService userSessionService;
    private final UserSessionLogoutEventRepository userSessionLogoutEventRepository;

    public LogoutController(TokenStore tokenStore, UserSessionService userSessionService,
                             UserSessionLogoutEventRepository userSessionLogoutEventRepository) {
        this.tokenStore = tokenStore;
        this.userSessionService = userSessionService;
        this.userSessionLogoutEventRepository = userSessionLogoutEventRepository;
    }

    /**
     * End-point to logout the session. Idempotent — a device that logs out while offline
     * clears its local session immediately and queues this call with a client-generated
     * {@code clientEventId}, resent on reconnect (possibly more than once, on retry). Repeated
     * calls are safe to replay because the underlying state change is itself a no-op once
     * applied (see UserSessionService#logout, matched strictly on sessionId and guarded by
     * "still ACTIVE"), and token removal is idempotent. When {@code clientEventId} is supplied,
     * a duplicate is recognized via {@link UserSessionLogoutEventRepository} only so the event
     * isn't recorded twice; it is still processed, so a client that reuses an id for a later
     * login can't be left with a removed token and a still-ACTIVE session.
     */
    @PostMapping("/_logout")
    public ResponseEntity<?> deleteToken(@RequestBody TokenWrapper tokenWrapper) throws Exception {
        String accessToken = tokenWrapper.getAccessToken();
        OAuth2AccessToken redisToken = tokenStore.readAccessToken(accessToken);
        if (redisToken == null) {
            // Already gone — either this token was never valid, or a prior attempt (possibly
            // this exact retry) already completed the logout. Either way the goal of a logout
            // call ("this token is not authenticated") already holds, so a retry must see
            // success here — an offline-logout sync queue that got "Logout failed" for an
            // already-completed logout would keep retrying it indefinitely.
            return buildLogoutSuccessResponse();
        }

        String clientEventId = tokenWrapper.getClientEventId();
        User user = extractUser(redisToken);
        boolean duplicateEvent = user != null && clientEventId != null
                && userSessionLogoutEventRepository.isAlreadyProcessed(clientEventId, user.getUuid(), user.getTenantId());

        // Runs even for a duplicate event: if the first delivery already terminated the session
        // this matches nothing (no-op), but if the client reused the id for a later login, this
        // terminates that login's session so it isn't left ACTIVE behind a removed token.
        if (user != null) {
            userSessionService.logout(user.getSessionId(), user.getTenantId(), user.getUuid());
        }
        tokenStore.removeAccessToken(redisToken);
        if (user != null && clientEventId != null && !duplicateEvent) {
            userSessionLogoutEventRepository.recordProcessed(clientEventId, user.getSessionId(), user.getTenantId(),
                    user.getUuid(), System.currentTimeMillis());
        }
        return buildLogoutSuccessResponse();
    }

    private User extractUser(OAuth2AccessToken redisToken) {
        if (redisToken.getAdditionalInformation() == null) {
            return null;
        }
        Object userRequestObj = redisToken.getAdditionalInformation().get(UserServiceConstants.USER_REQUEST_KEY);
        return userRequestObj instanceof User ? (User) userRequestObj : null;
    }

    private ResponseEntity<ResponseInfo> buildLogoutSuccessResponse() {
        ResponseInfo responseInfo = new ResponseInfo("", "", System.currentTimeMillis(), "", "", "Logout successfully");
        return new ResponseEntity<>(responseInfo, HttpStatus.OK);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleError(Exception ex) {
        ex.printStackTrace();
        return buildLogoutFailedResponse();
    }

    private ResponseEntity<ErrorResponse> buildLogoutFailedResponse() {
        ErrorResponse response = new ErrorResponse();
        ResponseInfo responseInfo = new ResponseInfo("", "", System.currentTimeMillis(), "", "", "Logout failed");
        response.setResponseInfo(responseInfo);
        Error error = new Error();
        error.setCode(400);
        error.setDescription("Logout failed");
        response.setError(error);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}