package org.egov.sunbirdrc.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class CredentialController {

    @Autowired
    private CredentialService credentialService;
    @PostMapping("/_get")
    public ResponseEntity<?> getCredential(@RequestBody String entityPayload) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(entityPayload);
        String entityId = jsonNode.get("entityId").asText();
        // Print the entityId value
        String credentialId=credentialService.getCredential(entityId);
        System.out.println("Entity ID: " + entityId);
        return new ResponseEntity<>(credentialId, HttpStatus.OK);
    }
}
