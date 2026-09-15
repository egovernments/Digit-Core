package org.egov.user.web.controller;

import org.egov.common.contract.response.ResponseInfo;
import org.egov.user.security.oauth2.custom.jwt.IDPJwtValidator;
import org.egov.user.web.contract.SsoCacheClearRequest;
import org.egov.user.web.contract.factory.ResponseInfoFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * Internal admin endpoints for managing SSO decoder caches.
 *
 * <p>These endpoints are intended for operational use only (e.g. during key
 * rotation or incident response) and must be protected at the API gateway
 * or via dedicated security configuration. They SHOULD NOT be exposed to
 * untrusted callers.</p>
 */
@RestController
@RequestMapping("/sso")
public class SsoCacheAdminController {

    private final IDPJwtValidator idpJwtValidator;
    private final ResponseInfoFactory responseInfoFactory;

    public SsoCacheAdminController(IDPJwtValidator idpJwtValidator, ResponseInfoFactory responseInfoFactory) {
        this.idpJwtValidator = idpJwtValidator;
        this.responseInfoFactory = responseInfoFactory;
    }

    @PostMapping("/decoders/_clear")
    public ResponseInfo clearDecodersForTenantAndProvider(@RequestBody @Valid SsoCacheClearRequest request) {
        idpJwtValidator.clearDecoderCacheFor(request.getTenantId(), request.getProviderId());
        return responseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(), true);
    }

}