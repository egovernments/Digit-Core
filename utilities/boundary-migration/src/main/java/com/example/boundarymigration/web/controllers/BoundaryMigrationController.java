package com.example.boundarymigration.web.controllers;

import com.example.boundarymigration.service.BoundaryMigrate;
import com.example.boundarymigration.web.models.legacy.BoundaryMigrateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/boundary")
public class BoundaryMigrationController {

    @Autowired
    private BoundaryMigrate boundaryMigrate;

    /**
     * Migrate boundary data from old format to new format
     * @param requestBody
     * @return
     */
    @RequestMapping(value = "/_migrate", method = RequestMethod.POST)
    public void boundaryMigrate(@RequestBody BoundaryMigrateRequest requestBody) {
        boundaryMigrate.migrate(requestBody);
    }
}
