package digit.web.controllers;


import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import digit.service.SchemaDefinitionMigrationService;
import digit.web.models.SchemaMigrationRequest;

@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2023-06-20T09:54:35.237+05:30[Asia/Calcutta]")
@RestController
@RequestMapping(value = "/schema")
public class SchemaApiController {

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;

    private final SchemaDefinitionMigrationService schemaDefinitionMigrationService;

    @Autowired
    public SchemaApiController(ObjectMapper objectMapper, HttpServletRequest request, SchemaDefinitionMigrationService schemaDefinitionMigrationService) {
        this.objectMapper = objectMapper;
        this.request = request;
        this.schemaDefinitionMigrationService = schemaDefinitionMigrationService;
    }

    @PostMapping(value = "/v1/_generate")
    public ResponseEntity<Void> generateSchemaDefinition() {
        schemaDefinitionMigrationService.generateSchemaDefinition();
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @PostMapping(value = "/v1/_migrate")
    public ResponseEntity<Void> migrateSchemaDefinition(@RequestBody SchemaMigrationRequest schemaMigrationRequest) {
        schemaDefinitionMigrationService.beginMigration(schemaMigrationRequest);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

}
