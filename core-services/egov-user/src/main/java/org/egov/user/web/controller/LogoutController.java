package org.egov.user.web.controller;

import org.egov.common.contract.response.Error;
import org.egov.common.contract.response.ErrorResponse;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.user.domain.model.TokenWrapper;
import org.egov.user.security.oauth2.custom.CustomRedisTokenStore;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LogoutController {

    private CustomRedisTokenStore tokenStore;

    public LogoutController(CustomRedisTokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    /**
     * End-point to logout the session.
     *
     * @param
     * @return
     * @throws Exception
     */
    @PostMapping("/_logout")
    public ResponseInfo deleteToken(@RequestBody TokenWrapper tokenWrapper) throws Exception {
        String accessToken = tokenWrapper.getAccessToken();
        tokenStore.removeAccessToken(accessToken);
        return new ResponseInfo("", "", System.currentTimeMillis(), "", "", "Logout successfully");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleError(Exception ex) {
        ex.printStackTrace();
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
