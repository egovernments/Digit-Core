package org.egov.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.config.ApportionConfig;
import org.egov.repository.ServiceRequestRepository;
import org.egov.tracer.model.CustomException;
import org.egov.web.models.BusinessServiceResponse;
import org.egov.web.models.TaxHeadResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class BillingServiceClient {

    private final ApportionConfig config;
    private final RestTemplate restTemplate;

    @Autowired
    public BillingServiceClient(ApportionConfig config, RestTemplate restTemplate) {
        this.config = config;
        this.restTemplate = restTemplate;
    }

    /**
     * Fetches business service details by code
     * @param businessServiceCode The business service code (e.g., "PT")
     * @param tenantId The tenant ID
     * @return BusinessServiceResponse
     */
    public BusinessServiceResponse getBusinessService(String businessServiceCode, String tenantId) {
        try {
            String url = config.getBillingHost() + config.getBusinessServiceEndpoint() + "/" + businessServiceCode;
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Tenant-ID", tenantId);
            headers.set("Content-Type", "application/json");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            log.info("Calling business service API: {} for code: {} and tenant: {}", url, businessServiceCode, tenantId);
            
            ResponseEntity<BusinessServiceResponse> response = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                entity, 
                BusinessServiceResponse.class
            );
            
            if (response.getBody() == null) {
                throw new CustomException("BUSINESS_SERVICE_NOT_FOUND", 
                    "Business service not found for code: " + businessServiceCode);
            }
            
            log.info("Successfully fetched business service: {}", response.getBody().getCode());
            return response.getBody();
            
        } catch (Exception e) {
            log.error("Error fetching business service for code: {} and tenant: {}", businessServiceCode, tenantId, e);
            throw new CustomException("BUSINESS_SERVICE_FETCH_ERROR", 
                "Failed to fetch business service: " + e.getMessage());
        }
    }

    /**
     * Fetches tax head details by code
     * @param taxHeadCode The tax head code (e.g., "PT_BASE_TAX")
     * @param tenantId The tenant ID
     * @return TaxHeadResponse
     */
    public TaxHeadResponse getTaxHead(String taxHeadCode, String tenantId) {
        try {
            String url = config.getBillingHost() + config.getTaxHeadEndpoint() + "/" + taxHeadCode;
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Tenant-ID", tenantId);
            headers.set("Content-Type", "application/json");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            log.info("Calling tax head API: {} for code: {} and tenant: {}", url, taxHeadCode, tenantId);
            
            ResponseEntity<TaxHeadResponse> response = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                entity, 
                TaxHeadResponse.class
            );
            
            if (response.getBody() == null) {
                throw new CustomException("TAX_HEAD_NOT_FOUND", 
                    "Tax head not found for code: " + taxHeadCode);
            }
            
            log.info("Successfully fetched tax head: {}", response.getBody().getCode());
            return response.getBody();
            
        } catch (Exception e) {
            log.error("Error fetching tax head for code: {} and tenant: {}", taxHeadCode, tenantId, e);
            throw new CustomException("TAX_HEAD_FETCH_ERROR", 
                "Failed to fetch tax head: " + e.getMessage());
        }
    }

    /**
     * Checks if advance payment is allowed for the business service
     * @param businessServiceCode The business service code
     * @param tenantId The tenant ID
     * @return Boolean indicating if advance is allowed
     */
    public Boolean isAdvanceAllowed(String businessServiceCode, String tenantId) {
        // Hardcoded to false as requested
        log.info("isAdvanceAllowed hardcoded to false for businessService: {}", businessServiceCode);
        return false;
    }

    /**
     * Gets advance tax head code for a business service
     * @param businessServiceCode The business service code
     * @param tenantId The tenant ID
     * @return Advance tax head code
     */
    public String getAdvanceTaxHead(String businessServiceCode, String tenantId) {
        // TODO: Need proper API to get advance tax head by category='ADVANCE_COLLECTION'
        // For now, using convention-based approach - THIS NEEDS TO BE FIXED
        String advanceTaxHeadCode = businessServiceCode + "_ADVANCE";
        
        try {
            // Verify the advance tax head exists
            getTaxHead(advanceTaxHeadCode, tenantId);
            log.warn("Using convention-based advance tax head: {} - API should provide this", advanceTaxHeadCode);
            return advanceTaxHeadCode;
        } catch (CustomException e) {
            log.warn("Advance tax head {} not found, falling back to generic advance", advanceTaxHeadCode);
            // Fallback to generic advance tax head
            return "ADVANCE";
        }
    }

    /**
     * Gets tax head order/priority by code
     * @param taxHeadCode The tax head code
     * @param tenantId The tenant ID
     * @return Order/priority of the tax head
     */
    public Integer getTaxHeadOrder(String taxHeadCode, String tenantId) {
        TaxHeadResponse taxHead = getTaxHead(taxHeadCode, tenantId);
        return taxHead.getOrder();
    }

    /**
     * Gets multiple tax heads for a business service
     * @param businessServiceCode The business service code
     * @param tenantId The tenant ID
     * @return Map of tax head code to order
     */
    public Map<String, Integer> getCodeToOrderMap(String businessServiceCode, String tenantId) {
        try {
            String url = config.getBillingHost() + config.getTaxHeadEndpoint() 
                + "?businessServiceCode=" + businessServiceCode 
                + "&isActive=true&limit=25&offset=0";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Tenant-ID", tenantId);
            headers.set("Content-Type", "application/json");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            log.info("Calling bulk tax heads API: {} for businessService: {} and tenant: {}", url, businessServiceCode, tenantId);
            
            ResponseEntity<TaxHeadResponse[]> response = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                entity, 
                TaxHeadResponse[].class
            );
            
            if (response.getBody() == null || response.getBody().length == 0) {
                log.warn("No tax heads found for businessService: {}", businessServiceCode);
                return new HashMap<>();
            }
            
            Map<String, Integer> codeToOrderMap = new HashMap<>();
            for (TaxHeadResponse taxHead : response.getBody()) {
                codeToOrderMap.put(taxHead.getCode(), taxHead.getOrder());
            }
            
            log.info("Successfully fetched {} tax heads for businessService: {}", codeToOrderMap.size(), businessServiceCode);
            return codeToOrderMap;
            
        } catch (Exception e) {
            log.error("Error fetching tax heads for businessService: {} and tenant: {}", businessServiceCode, tenantId, e);
            throw new CustomException("TAX_HEADS_FETCH_ERROR", 
                "Failed to fetch tax heads: " + e.getMessage());
        }
    }
}