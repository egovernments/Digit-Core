package org.egov.user.web.controller;

import org.egov.common.contract.response.Error;
import org.egov.common.contract.response.ErrorResponse;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.user.config.UserServiceConstants;
import org.egov.user.domain.model.TokenWrapper;
import org.egov.user.domain.service.UserSessionService;
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

    private TokenStore tokenStore;
    private UserSessionService userSessionService;

    public LogoutController(TokenStore tokenStore, UserSessionService userSessionService) {
        this.tokenStore = tokenStore;
        this.userSessionService = userSessionService;
    }

    /**
     * End-point to logout the session.
     *
     * @param
     * @return
     * @throws Exception
     */
    @PostMapping("/_logout")
    public ResponseEntity<?> deleteToken(@RequestBody TokenWrapper tokenWrapper) throws Exception {
        String accessToken = tokenWrapper.getAccessToken();
        OAuth2AccessToken redisToken = tokenStore.readAccessToken(accessToken);
        if (redisToken == null) {
            return buildLogoutFailedResponse();
        }
        markSessionLoggedOut(redisToken);
        tokenStore.removeAccessToken(redisToken);
        ResponseInfo responseInfo = new ResponseInfo("", "", System.currentTimeMillis(), "", "", "Logout successfully");
        return new ResponseEntity<>(responseInfo, HttpStatus.OK);
    }

    private void markSessionLoggedOut(OAuth2AccessToken redisToken) {
        if (redisToken == null || redisToken.getAdditionalInformation() == null) {
            return;
        }
        Object userRequestObj = redisToken.getAdditionalInformation().get(UserServiceConstants.USER_REQUEST_KEY);
        if (userRequestObj instanceof org.egov.user.web.contract.auth.User) {
            org.egov.user.web.contract.auth.User user = (org.egov.user.web.contract.auth.User) userRequestObj;
            userSessionService.logout(user.getSessionId(), user.getTenantId());
        }
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