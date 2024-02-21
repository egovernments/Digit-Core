package org.egov.sunbirdrc.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.sunbirdrc.models.DidSchemaId;
import org.egov.sunbirdrc.service.CredentialService;
import org.egov.sunbirdrc.service.FetchDidSchemaIdService;
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
    private CredentialService credentialService;

    @RequestMapping(value="/_get", method = RequestMethod.POST)
    public ResponseEntity<String> getCredentialId(@RequestBody String payloadAsString) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(payloadAsString);
        JsonNode idNode = rootNode.path("Data").path("tradelicense");
        String uuid= idNode.path("id").asText(null);
        return credentialService.getCredentialId(uuid);
    }
}
