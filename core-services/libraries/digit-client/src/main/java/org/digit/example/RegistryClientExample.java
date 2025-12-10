package org.digit.example;

import org.digit.factory.DigitClientFactory;
import org.digit.services.registry.RegistryClient;
import org.digit.services.registry.model.RegistryData;
import org.digit.services.registry.model.RegistryDataResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
                    .version(1)
                    .data(dataNode)
                    .build();

            System.out.println("Creating registry data...");
            RegistryDataResponse createResponse = registryClient.createRegistryData("tradelicense", registryData);
            
            if (createResponse != null && createResponse.getSuccess()) {
                System.out.println("Successfully created registry data:");
                System.out.println("Success: " + createResponse.getSuccess());
                System.out.println("Data: " + createResponse.getData());
                System.out.println("Message: " + createResponse.getMessage());
                
                // Example 2: Search registry data
                System.out.println("\nSearching for registry data...");
                RegistryDataResponse searchResponse = registryClient.searchRegistryData("tradelicense", "some-registry-id");
                
                if (searchResponse != null && searchResponse.getSuccess()) {
                    System.out.println("Successfully found registry data:");
                    System.out.println("Success: " + searchResponse.getSuccess());
                    System.out.println("Data: " + searchResponse.getData());
                    System.out.println("Message: " + searchResponse.getMessage());
                } else {
                    System.out.println("Registry data not found or error occurred:");
                    if (searchResponse != null) {
                        System.out.println("Error: " + searchResponse.getError());
                    }
                }
            } else {
                System.out.println("Failed to create registry data:");
                if (createResponse != null) {
                    System.out.println("Error: " + createResponse.getError());
                }
            }

        } catch (Exception e) {
            System.err.println("Error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}