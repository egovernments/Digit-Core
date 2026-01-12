package org.egov.service;

import static org.egov.util.ApportionConstants.DEFAULT;

import java.math.BigDecimal;
import java.util.*;

import org.egov.config.ApportionConfig;
import org.egov.producer.Producer;
import org.egov.web.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;


@Service
public class ApportionService {

    private final List<Apportion> apportions;
    private Map<String, Apportion> APPORTION_MAP = new HashMap<>();

    private Producer producer;
    private ApportionConfig config;

    @Autowired
    public ApportionService(List<Apportion> apportions,  Producer producer,
                            ApportionConfig config) {
        this.apportions = Collections.unmodifiableList(apportions);
        this.producer = producer;
        this.config = config;
        initialize();
    }

    private void initialize() {
        if (Objects.isNull(apportions))
            throw new IllegalStateException("No Apportion found, spring initialization failed.");

        if (APPORTION_MAP.isEmpty() && !apportions.isEmpty()) {
            apportions.forEach(apportion -> {
                APPORTION_MAP.put(apportion.getBusinessService(), apportion);
            });
        }
        APPORTION_MAP = Collections.unmodifiableMap(APPORTION_MAP);
    }


    /**
     * Apportions the paid amount for the given list of bills
     *
     * @param request The apportion request
     * @param tenantId The tenant ID from header
     * @param clientId The client ID from header
     * @return Apportioned Bills
     */
    public List<Bill> apportionBills(ApportionRequest request, String tenantId, String clientId) {
        List<Bill> bills = request.getBills();
        Apportion apportion;

        // No longer using Kafka producer - removed persister calls

        for (Bill bill : bills) {

            // Create a map of businessService to list of billDetails belonging to that businessService
            //   Map<String, List<BillDetail>> businessServiceToBillDetails = util.groupByBusinessService(billInfo.getBillDetails());

            bill.getBillDetails().sort(Comparator.comparing(BillDetail::getFromPeriod));


            String businessKey = bill.getBusinessService();
            BigDecimal amountPaid = bill.getAmountPaid();

            List<BillDetail> billDetails = bill.getBillDetails();

            if (CollectionUtils.isEmpty(billDetails))
                continue;

            // Get the appropriate implementation of Apportion
            if (isApportionPresent(businessKey))
                apportion = getApportion(businessKey);
            else
                apportion = getApportion(DEFAULT);

            /*
             * Apportion the paid amount among the given list of billDetail
             */
            apportion.apportionPaidAmount(bill, tenantId, clientId);
        }

        // No longer using Kafka producer - removed persister calls
        return bills;
    }


    /**
     * Retrives the apportion for the given businessService
     *
     * @param businessService The businessService of the billDetails
     * @return Apportion object for the given businessService
     */
    private Apportion getApportion(String businessService) {
        return APPORTION_MAP.get(businessService);
    }


    /**
     * Checks if the apportion is present for the given businessService
     *
     * @param businessService The businessService of the billDetails
     * @return True if the apportion is present else false
     */
    private Boolean isApportionPresent(String businessService) {
        return APPORTION_MAP.containsKey(businessService);
    }


}