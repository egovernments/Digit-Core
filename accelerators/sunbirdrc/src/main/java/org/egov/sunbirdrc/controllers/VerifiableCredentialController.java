package org.egov.sunbirdrc.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.coyote.Response;
import org.egov.sunbirdrc.models.DidSchemaId;
import org.egov.sunbirdrc.models.VcCredentialId;
import org.egov.sunbirdrc.service.CredentialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;

@Controller
@RequestMapping("/sunbirdrc/vcid/v1")
public class VerifiableCredentialController {


    @Autowired
    private VcCredentialId vcCredentialId;

    @RequestMapping(value="/_get", method = RequestMethod.POST)
    public ResponseEntity<VcCredentialId> getCredentialId(@RequestBody String payloadAsString) throws JsonProcessingException {
        return new ResponseEntity<>(vcCredentialId,HttpStatus.OK);
    }
}
