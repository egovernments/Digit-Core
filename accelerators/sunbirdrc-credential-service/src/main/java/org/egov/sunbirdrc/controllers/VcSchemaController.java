package org.egov.sunbirdrc.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.egov.sunbirdrc.models.MdmsData;
import org.egov.sunbirdrc.models.MdmsSchema;
import org.egov.sunbirdrc.service.AddVcSchemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping(value = "/vc/schema")
@Controller
public class VcSchemaController {

    @Autowired
    private AddVcSchemaService addVcSchemaService;

    @PostMapping("/_create")
    public ResponseEntity<String> addVcSchema(@RequestBody MdmsSchema mdmsSchema) throws JsonProcessingException {
        System.out.println("mdms data is"+ mdmsSchema);
        String mdmsSchemaResponse = addVcSchemaService.addVcSchema(mdmsSchema);
        return new ResponseEntity<>(mdmsSchemaResponse, HttpStatus.OK);
    }
}
