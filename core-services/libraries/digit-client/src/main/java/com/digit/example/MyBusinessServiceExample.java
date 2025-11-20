package com.digit.example;

import com.digit.services.account.AccountClient;
import com.digit.services.account.model.Tenant;
import com.digit.services.boundary.BoundaryClient;
import com.digit.services.boundary.model.Boundary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Example business service showing how to use Digit clients after configuration.
 * No configuration needed - just autowire and use!
 */
@Service
public class MyBusinessServiceExample {

    @Autowired
    private BoundaryClient boundaryClient;  // No config needed!

    @Autowired
    private AccountClient accountClient;    // No config needed!

    /**
     * Example method showing direct usage of clients.
     */
    public void doSomething() {
        // Direct usage - no configuration needed
        
        // Use boundary client
        List<String> codes = List.of("BOUNDARY001", "BOUNDARY002");
        List<Boundary> boundaries = boundaryClient.searchBoundariesByCodes(codes);
        
        // Use account client  
        Tenant tenant = accountClient.searchTenantByCode("TENANT001");
        
        // Business logic here...
    }

    /**
     * Example method for creating resources.
     */
    public void createResources() {
        // Create tenant
        Tenant newTenant = new Tenant();
        newTenant.setCode("NEW001");
        newTenant.setName("New Tenant");
        newTenant.setEmailId("new@example.com");
        
        Tenant created = accountClient.createTenant(newTenant);
        
        // Create boundaries
        // ... boundary creation logic
    }
}
