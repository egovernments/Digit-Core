package digit.web.controllers;


import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.databind.ObjectMapper;

import digit.service.MasterDataMigrationService;
import digit.web.models.MasterDataMigrationRequest;

@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2023-06-20T09:54:35.237+05:30[Asia/Calcutta]")
@Controller
@RequestMapping(value = "/data")
public class DataApiController {

    private final ObjectMapper objectMapper;

    private final MasterDataMigrationService masterDataMigrationService;

    @Autowired
    public DataApiController(ObjectMapper objectMapper, MasterDataMigrationService masterDataMigrationService) {
        this.objectMapper = objectMapper;
        this.masterDataMigrationService = masterDataMigrationService;
    }

    @RequestMapping(value = "/v1/_migrate", method = RequestMethod.POST)
    public ResponseEntity<Object> migrateMasterData(@Valid @RequestBody MasterDataMigrationRequest body) {
        masterDataMigrationService.migrateMasterData(body);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

}
