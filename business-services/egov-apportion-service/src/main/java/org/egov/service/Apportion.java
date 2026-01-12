package org.egov.service;

import org.egov.web.models.Bill;
import org.egov.web.models.BillDetail;

import java.math.BigDecimal;
import java.util.*;

public interface Apportion {

    /**
     * Should return the code of the BusinessService for which the interface is implemented
     * @return Code of the BusinessService
     */
    String getBusinessService();

    /**
     * Distibutes the paid amount among the Bill account details
     * @param bill The bill to be apportioned
     * @param tenantId The tenant ID for billing service calls
     * @param clientId The client ID for audit details
     * @return Apportioned BillDetails
     */
    List<BillDetail> apportionPaidAmount(Bill bill, String tenantId, String clientId);

}
