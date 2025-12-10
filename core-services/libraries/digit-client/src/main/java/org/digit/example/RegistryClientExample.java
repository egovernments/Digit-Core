package org.digit.example;

import org.digit.factory.DigitClientFactory;
import org.digit.services.registry.RegistryClient;
import org.digit.services.registry.model.RegistryData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Example demonstrating how to use the RegistryClient.
 * Shows both createRegistryData and searchRegistryData operations.
 */
public class RegistryClientExample {

    public static void main(String[] args) {
        // Create a RegistryClient with default base URL (http://localhost:8085)
        RegistryClient registryClient = DigitClientFactory.createRegistryClient();
        
        // Or create with custom base URL
        // RegistryClient registryClient = DigitClientFactory.createRegistryClient("http://your-registry-service:8085");

        try {
            // Example 1: Create registry data
            ObjectMapper objectMapper = new ObjectMapper();
            
            Map<String, Object> licenseData = new HashMap<>();
            licenseData.put("licenseNumber", "DL-001");
            licenseData.put("holderName", "Jane Citizen");
            licenseData.put("issueDate", "2024-01-10");
            licenseData.put("expiryDate", "2029-01-09");
            licenseData.put("status", "ACTIVE");
            
            JsonNode dataNode = objectMapper.valueToTree(licenseData);
            
            RegistryData registryData = RegistryData.builder()
                    .schemaCode("tradelicense")
                    .schemaVersion(1)
                    .version(1)
                    .data(dataNode)
                    .isActive(true)
                    .effectiveFrom(LocalDateTime.now())
                    .build();

            System.out.println("Creating registry data...");
            RegistryData createdData = registryClient.createRegistryData(registryData);
            
            if (createdData != null) {
                System.out.println("Successfully created registry data:");
                System.out.println("Registry ID: " + createdData.getRegistryId());
                System.out.println("Schema Code: " + createdData.getSchemaCode());
                System.out.println("Is Active: " + createdData.getIsActive());
                
                // Example 2: Search registry data
                String registryId = createdData.getRegistryId();
                if (registryId != null) {
                    System.out.println("\nSearching for registry data...");
                    RegistryData foundData = registryClient.searchRegistryData(createdData.getSchemaCode(), registryId);
                    
                    if (foundData != null) {
                        System.out.println("Successfully found registry data:");
                        System.out.println("Registry ID: " + foundData.getRegistryId());
                        System.out.println("Schema Code: " + foundData.getSchemaCode());
                        System.out.println("Data: " + foundData.getData());
                    } else {
                        System.out.println("Registry data not found");
                    }
                }
            } else {
                System.out.println("Failed to create registry data");
            }

        } catch (Exception e) {
            System.err.println("Error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}