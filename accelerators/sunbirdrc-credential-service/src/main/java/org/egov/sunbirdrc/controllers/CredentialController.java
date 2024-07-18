package org.egov.sunbirdrc.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.sunbirdrc.models.CredentialIdResponse;
import org.egov.sunbirdrc.models.QrCodeRequest;
import org.egov.sunbirdrc.service.CredentialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;


@RequestMapping(value = "/credential")
@Controller
@Slf4j
public class CredentialController {

    @Autowired
    private CredentialService credentialService;

    @Autowired
    private ObjectMapper objectMapper;
    @PostMapping("/_get")
    public ResponseEntity<CredentialIdResponse> getCredential(@RequestBody QrCodeRequest qrCodeRequest) throws JsonProcessingException {

        CredentialIdResponse credentialId=credentialService.getCredential(qrCodeRequest);
        return new ResponseEntity<CredentialIdResponse>(credentialId, HttpStatus.OK);
    }
}
