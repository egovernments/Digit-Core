package org.egov.service;

import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TaxHeadMasterService {

    private final BillingServiceClient billingServiceClient;

    @Autowired
    public TaxHeadMasterService(BillingServiceClient billingServiceClient) {
        this.billingServiceClient = billingServiceClient;
    }

    /**
     * Fetches the advance amount taxHead for the given businessService from Billing Service
     * @param businessService The businessService for which taxhead is to be fetched
     * @param tenantId The tenant ID
     * @return The code of the TaxHead
     */
    public String getAdvanceTaxHead(String businessService, String tenantId){
        return billingServiceClient.getAdvanceTaxHead(businessService, tenantId);
    }

    /**
     * Creates a map of taxHeadCode to priority for taxHeads of given businessService using Billing Service
     * @param businessService The business service code
     * @param tenantId The tenant ID
     * @return Map of tax head code to order
     */
    public Map<String,Integer> getCodeToOrderMap(String businessService, String tenantId){
        return billingServiceClient.getCodeToOrderMap(businessService, tenantId);
    }

    /**
     * Fetches the isAdvanceAllowed flag from the Billing Service for the given businessService
     * @param businessService BusinessService for which advance flag has to be returned
     * @param tenantId The tenant ID
     * @return boolean flag indicating whether advance payment is allowed for the given businessService
     */
    public Boolean isAdvanceAllowed(String businessService, String tenantId){
        return billingServiceClient.isAdvanceAllowed(businessService, tenantId);
    }

}
