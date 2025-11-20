package com.digit.example;

import com.digit.config.ApiConfig;
import com.digit.exception.DigitClientException;
import com.digit.services.account.AccountClient;
import com.digit.services.account.model.Tenant;
import com.digit.services.boundary.BoundaryClient;
import com.digit.services.boundary.model.Boundary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

/**
 * Example usage of the Digit Client Library.
 * Demonstrates how to use AccountClient and BoundaryClient.
 */
@Slf4j
public class DigitClientExample {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DigitClientExample.class);

    public static void main(String[] args) {
        // Initialize Spring Application Context
        ApplicationContext context = new AnnotationConfigApplicationContext(ApiConfig.class);

        // Get client beans
        AccountClient accountClient = context.getBean(AccountClient.class);
        BoundaryClient boundaryClient = context.getBean(BoundaryClient.class);

        // Example 1: Tenant operations
        demonstrateTenantOperations(accountClient);

        // Example 2: Boundary operations
        demonstrateBoundaryOperations(boundaryClient);

        // Close context
        ((AnnotationConfigApplicationContext) context).close();
    }

    private static void demonstrateTenantOperations(AccountClient accountClient) {
        log.info("=== Tenant Client Examples ===");

        try {
            // Get tenant by ID
            log.info("1. Getting tenant by ID...");
            Tenant foundTenant = accountClient.searchTenantByCode("TENANT001");
            log.info("Retrieved tenant: {}", foundTenant != null ? foundTenant.getName() : "null");

        } catch (DigitClientException e) {
            log.error("Failed to get tenant by ID: {}", e.getMessage());
        }

        try {
            // Get all tenants
            log.info("2. Getting all tenants...");
            // List<Tenant> tenants = accountClient.searchTenants("client-123"); // Method not implemented
            log.info("Search all tenants operation would be implemented here");

        } catch (DigitClientException e) {
            log.error("Failed to get all tenants: {}", e.getMessage());
        }

        try {
            // Get tenant by code
            log.info("3. Getting tenant by code...");
            Tenant tenantByCode = accountClient.searchTenantByCode("TN001");
            log.info("Retrieved tenant by code: {}", tenantByCode != null ? tenantByCode.getName() : "null");

        } catch (DigitClientException e) {
            log.error("Failed to get tenant by code: {}", e.getMessage());
        }

        try {
            // Create new tenant
            log.info("4. Creating new tenant...");
            Tenant newTenant = Tenant.builder()
                    .code("TN002")
                    .name("Test Tenant")
                    // .isActive(true) - method not available in builder
                    .build();
            Tenant createdTenant = accountClient.createTenant(newTenant);
            log.info("Created tenant: {}", createdTenant != null ? createdTenant.getName() : "null");

        } catch (DigitClientException e) {
            log.error("Failed to create tenant: {}", e.getMessage());
        }
    }

    private static void demonstrateBoundaryOperations(BoundaryClient boundaryClient) {
        log.info("=== Boundary Client Examples ===");

        try {
            // Get boundary by code
            log.info("1. Getting boundary by code...");
            // Note: Boundary methods would need to be implemented based on actual API
            log.info("Boundary operations would be implemented here");

        } catch (DigitClientException e) {
            log.error("Failed to get boundary by code: {}", e.getMessage());
        }

        try {
            // Get boundary by ID
            log.info("2. Getting boundary by ID...");
            // Boundary search operations
            log.info("Boundary search operations would be implemented here");

        } catch (DigitClientException e) {
            log.error("Failed to get boundary by ID: {}", e.getMessage());
        }

        try {
            // Get all boundaries
            log.info("3. Getting all boundaries...");
            // Get all boundaries operation
            log.info("All boundaries operation would be implemented here");

        } catch (DigitClientException e) {
            log.error("Failed to get all boundaries: {}", e.getMessage());
        }

        try {
            // Get boundaries by tenant
            log.info("4. Getting boundaries by tenant ID...");
            // Get boundaries by tenant operation
            log.info("Boundaries by tenant operation would be implemented here");

        } catch (DigitClientException e) {
            log.error("Failed to get boundaries by tenant: {}", e.getMessage());
        }

        try {
            // Get boundaries by type
            log.info("5. Getting boundaries by type...");
            // Get boundaries by type operation
            log.info("Boundaries by type operation would be implemented here");

        } catch (DigitClientException e) {
            log.error("Failed to get boundaries by type: {}", e.getMessage());
        }
    }
}
