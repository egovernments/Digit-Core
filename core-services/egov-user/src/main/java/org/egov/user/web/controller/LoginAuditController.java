package org.egov.user.web.controller;

import org.egov.common.contract.response.ResponseInfo;
import org.egov.user.domain.model.LoginAudit;
import org.egov.user.persistence.repository.LoginAuditRepository;
import org.egov.user.web.contract.LoginAuditResponse;
import org.egov.user.web.contract.LoginAuditSearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RequestMapping("loginaudit")
@RestController
public class LoginAuditController {

    @Autowired
    private LoginAuditRepository loginAuditRepository;

    @PostMapping("/_search")
    public LoginAuditResponse get(@RequestBody @Valid LoginAuditSearchRequest request, @RequestHeader HttpHeaders headers) {

        List<LoginAudit> loginAudits = loginAuditRepository.getLoginAudits(request);
        ResponseInfo responseInfo = ResponseInfo.builder().status(String.valueOf(HttpStatus.OK.value())).build();
        LoginAuditResponse response = LoginAuditResponse.builder().responseInfo(responseInfo).loginAudits(loginAudits).build();
        return response;
    }


}
