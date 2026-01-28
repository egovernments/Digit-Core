package org.egov.service;

import static org.egov.util.ApportionConstants.DEFAULT;

import java.math.BigDecimal;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.egov.config.ApportionConfig;
import org.egov.producer.Producer;
import org.egov.repository.ApportionRepository;
import org.egov.web.models.*;
import org.egov.web.models.enums.DemandApportionRequest;
import org.egov.web.models.enums.Purpose;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
public class ApportionServiceV2 {

    private final List<ApportionV2> apportions;
    private Map<String, ApportionV2> APPORTION_MAP = new HashMap<>();

    private Producer producer;
    private ApportionConfig config;
    private TranslationService translationService;
    private ApportionRepository apportionRepository;


    @Autowired
    public ApportionServiceV2(List<ApportionV2> apportions, Producer producer,
                              ApportionConfig config, TranslationService translationService,
                              ApportionRepository apportionRepository) {
        this.apportions = Collections.unmodifiableList(apportions);
        this.producer = producer;
        this.config = config;
        this.translationService = translationService;
        this.apportionRepository = apportionRepository;
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
        ApportionV2 apportion;
        
        // Generate consistent timestamp for the entire request
        Long currentTime = System.currentTimeMillis();

        // Save the request to database
        apportionRepository.saveBillRequest(request, tenantId, clientId, currentTime);

        for (Bill bill : bills) {
            // Enrich tenantId and audit details from header into bill and nested objects
            enrichTenantId(bill, tenantId);
            enrichAuditDetails(bill, clientId, currentTime);
            bill.getBillDetails().sort(Comparator.comparing(BillDetail::getFromPeriod));

            String businessKey = bill.getBusinessService();

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
            ApportionRequestV2 apportionRequestV2 = translationService.translate(bill, tenantId, clientId);
            
            List<TaxDetail> taxDetails = apportion.apportionPaidAmount(apportionRequestV2, clientId);
            updateAdjustedAmountInBills(bill,taxDetails);
            addAdvanceIfExistForBill(billDetails,taxDetails);
        }

        // Save the response to database
        apportionRepository.saveBillResponse(bills, tenantId, clientId, currentTime);
        return bills;
    }


    /**
     * Retrives the apportion for the given businessService
     *
     * @param businessService The businessService of the billDetails
     * @return Apportion object for the given businessService
     */
    private ApportionV2 getApportion(String businessService) {
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



    /**
     * Apportions the paid amount for the given list of demands
     *
     * @param request The apportion request
     * @param tenantId The tenant ID from header
     * @param clientId The client ID from header
     * @return Apportioned Bills
     */
    public List<Demand> apportionDemands(DemandApportionRequest request, String tenantId, String clientId) {
        List<Demand> demands = request.getDemands();
        ApportionV2 apportion;
        
        // Generate consistent timestamp for the entire request
        Long currentTime = System.currentTimeMillis();

        // Save the request to database
        apportionRepository.saveDemandRequest(request, tenantId, clientId, currentTime);
        
        // Enrich tenantId and audit details from header into demands and nested objects
        enrichTenantIdInDemands(demands, tenantId);
        enrichAuditDetailsInDemands(demands, clientId, currentTime);

        demands.sort(Comparator.comparing(Demand::getTaxPeriodFrom));

        ApportionRequestV2 apportionRequestV2 = translationService.translate(demands, tenantId, clientId);

        /*
        * Need to validate that all demands that come for apportioning
        * has same businessService and consumerCode
        * */
        String businessKey = demands.get(0).getBusinessService();

        if (isApportionPresent(businessKey))
            apportion = getApportion(businessKey);
        else
            apportion = getApportion(DEFAULT);

        List<TaxDetail> taxDetails = apportion.apportionPaidAmount(apportionRequestV2, clientId);
        updateAdjustedAmountInDemands(demands,taxDetails);
        addAdvanceIfExistForDemand(demands,taxDetails);

        // Save the response to database
        apportionRepository.saveDemandResponse(demands, tenantId, clientId, currentTime);
        return demands;
    }


    /**
     * Updates adjusted amount in demand from mao returned after apportion
     * @param demands
     * @param taxDetails
     */
    private void updateAdjustedAmountInDemands(List<Demand> demands,List<TaxDetail> taxDetails){

        Map<String,BigDecimal> idToAdjustedAmount = new HashMap<>();
        taxDetails.forEach(taxDetail -> {
            taxDetail.getBuckets().forEach(bucket -> {
                idToAdjustedAmount.put(bucket.getEntityId(),bucket.getAdjustedAmount());
            });
        });

        demands.forEach(demand -> {
            demand.getDemandDetails().forEach(demandDetail -> {
                demandDetail.setCollectionAmount(idToAdjustedAmount.get(demandDetail.getId()));
            });
        });

    }

    /**
     * Updates adjusted amount in bill from map returned after apportion
     * Uses taxHeadCode as the primary matching key since it's always present
     * @param bill
     * @param taxDetails
     */
    private void updateAdjustedAmountInBills(Bill bill,List<TaxDetail> taxDetails){

        Map<String,BigDecimal> idToAmountPaid = new HashMap<>();

        taxDetails.forEach(taxDetail -> {
            idToAmountPaid.put(taxDetail.getEntityId(),taxDetail.getAmountPaid());
        });

        bill.getBillDetails().forEach(billDetail -> {
            billDetail.setAmountPaid(idToAmountPaid.get(billDetail.getId()));
            
            // Find the corresponding TaxDetail for this BillDetail
            TaxDetail matchingTaxDetail = taxDetails.stream()
                    .filter(td -> (td.getEntityId() == null && billDetail.getId() == null) || 
                                  (td.getEntityId() != null && td.getEntityId().equals(billDetail.getId())))
                    .findFirst()
                    .orElse(null);
            
            if (matchingTaxDetail != null) {
                // Create a map by taxHeadCode for matching
                Map<String, Bucket> taxHeadToBucket = new HashMap<>();
                matchingTaxDetail.getBuckets().forEach(bucket -> 
                    taxHeadToBucket.put(bucket.getTaxHeadCode(), bucket)
                );
                
                billDetail.getBillAccountDetails().forEach(billAccountDetail -> {
                    // Match by taxHeadCode which is always present
                    Bucket matchedBucket = taxHeadToBucket.get(billAccountDetail.getTaxHeadCode());
                    
                    if (matchedBucket != null) {
                        billAccountDetail.setAdjustedAmount(matchedBucket.getAdjustedAmount());
                        if(billAccountDetail.getTaxHeadCode().contains("ADVANCE")){
                            billAccountDetail.setAmount(matchedBucket.getAmount());
                        }
                    }
                });
            }
        });
    }


    private void addAdvanceIfExistForDemand(List<Demand> demands,List<TaxDetail> taxDetails){

        Bucket advanceBucket = null;
        TaxDetail taxDetail = taxDetails.get(taxDetails.size()-1);

        for(Bucket bucket : taxDetail.getBuckets()){
            if(bucket.getEntityId()==null && bucket.getPurpose().equals(Purpose.ADVANCE_AMOUNT)){
                advanceBucket = bucket;
                break;
            }
        }

        if(advanceBucket != null){
            DemandDetail demandDetailForAdvance = new DemandDetail();
            demandDetailForAdvance.setTaxAmount(advanceBucket.getAmount());
            demandDetailForAdvance.setTaxHeadMasterCode(advanceBucket.getTaxHeadCode());
            demands.get(demands.size()-1).getDemandDetails().add(demandDetailForAdvance);
        }

    }


    private void addAdvanceIfExistForBill(List<BillDetail> billDetails,List<TaxDetail> taxDetails){

        Bucket advanceBucket = null;
        TaxDetail taxDetail = taxDetails.get(taxDetails.size()-1);

        for(Bucket bucket : taxDetail.getBuckets()){
            if(bucket.getEntityId()==null && bucket.getTaxHeadCode().contains("ADVANCE")){
                advanceBucket = bucket;
                break;
            }
        }

        if(advanceBucket != null){
            BillAccountDetail billAccountDetailForAdvance = new BillAccountDetail();
            billAccountDetailForAdvance.setAmount(advanceBucket.getAmount());
            billAccountDetailForAdvance.setPurpose(Purpose.ADVANCE_AMOUNT);
            billAccountDetailForAdvance.setTaxHeadCode(advanceBucket.getTaxHeadCode());
            billDetails.get(billDetails.size()-1).getBillAccountDetails().add(billAccountDetailForAdvance);
        }

    }


    /**
     * Enriches tenantId from header into demands and all nested objects
     * @param demands The demands to enrich
     * @param tenantId The tenant ID from header
     */
    private void enrichTenantIdInDemands(List<Demand> demands, String tenantId) {
        demands.forEach(demand -> {
            // Set tenantId in demand
            demand.setTenantId(tenantId);
            
            // Set tenantId in demand details
            if (demand.getDemandDetails() != null) {
                demand.getDemandDetails().forEach(demandDetail -> {
                    demandDetail.setTenantId(tenantId);
                });
            }
        });
    }

    /**
     * Enriches audit details for demands and all nested objects
     * @param demands The demands to enrich
     * @param clientId The client ID from header
     * @param currentTime The consistent timestamp for this request
     */
    private void enrichAuditDetailsInDemands(List<Demand> demands, String clientId, Long currentTime) {
        demands.forEach(demand -> {
            // Set audit details in demand if not present
            if (demand.getAuditDetails() == null) {
                demand.setAuditDetails(AuditDetails.builder()
                        .createdBy(clientId)
                        .lastModifiedBy(clientId)
                        .createdTime(currentTime)
                        .lastModifiedTime(currentTime)
                        .build());
            } else {
                demand.getAuditDetails().setLastModifiedBy(clientId);
                demand.getAuditDetails().setLastModifiedTime(currentTime);
            }
            
            // Set audit details in demand details
            if (demand.getDemandDetails() != null) {
                demand.getDemandDetails().forEach(demandDetail -> {
                    // Set audit details in demand detail if not present
                    if (demandDetail.getAuditDetails() == null) {
                        demandDetail.setAuditDetails(AuditDetails.builder()
                                .createdBy(clientId)
                                .lastModifiedBy(clientId)
                                .createdTime(currentTime)
                                .lastModifiedTime(currentTime)
                                .build());
                    } else {
                        demandDetail.getAuditDetails().setLastModifiedBy(clientId);
                        demandDetail.getAuditDetails().setLastModifiedTime(currentTime);
                    }
                });
            }
        });
    }

    /**
     * Enriches tenantId from header into bill and all nested objects
     * @param bill The bill to enrich
     * @param tenantId The tenant ID from header
     */
    private void enrichTenantId(Bill bill, String tenantId) {
        // Set tenantId in bill
        bill.setTenantId(tenantId);
        
        // Set tenantId in bill details
        if (bill.getBillDetails() != null) {
            bill.getBillDetails().forEach(billDetail -> {
                billDetail.setTenantId(tenantId);
                
                // Set tenantId in bill account details
                if (billDetail.getBillAccountDetails() != null) {
                    billDetail.getBillAccountDetails().forEach(billAccountDetail -> {
                        billAccountDetail.setTenantId(tenantId);
                    });
                }
            });
        }
    }

    /**
     * Enriches audit details for bill and all nested objects
     * @param bill The bill to enrich
     * @param clientId The client ID from header
     * @param currentTime The consistent timestamp for this request
     */
    private void enrichAuditDetails(Bill bill, String clientId, Long currentTime) {
        // Set audit details in bill if not present
        if (bill.getAuditDetails() == null) {
            bill.setAuditDetails(AuditDetails.builder()
                    .createdBy(clientId)
                    .lastModifiedBy(clientId)
                    .createdTime(currentTime)
                    .lastModifiedTime(currentTime)
                    .build());
        } else {
            // Update last modified details
            bill.getAuditDetails().setLastModifiedBy(clientId);
            bill.getAuditDetails().setLastModifiedTime(currentTime);
        }
        
        // Set audit details in bill details
        if (bill.getBillDetails() != null) {
            bill.getBillDetails().forEach(billDetail -> {
                // Set audit details in bill detail if not present
                if (billDetail.getAuditDetails() == null) {
                    billDetail.setAuditDetails(AuditDetails.builder()
                            .createdBy(clientId)
                            .lastModifiedBy(clientId)
                            .createdTime(currentTime)
                            .lastModifiedTime(currentTime)
                            .build());
                } else {
                    billDetail.getAuditDetails().setLastModifiedBy(clientId);
                    billDetail.getAuditDetails().setLastModifiedTime(currentTime);
                }
                
                // Set audit details in bill account details
                if (billDetail.getBillAccountDetails() != null) {
                    billDetail.getBillAccountDetails().forEach(billAccountDetail -> {
                        // Set audit details in bill account detail if not present
                        if (billAccountDetail.getAuditDetails() == null) {
                            billAccountDetail.setAuditDetails(AuditDetails.builder()
                                    .createdBy(clientId)
                                    .lastModifiedBy(clientId)
                                    .createdTime(currentTime)
                                    .lastModifiedTime(currentTime)
                                    .build());
                        } else {
                            billAccountDetail.getAuditDetails().setLastModifiedBy(clientId);
                            billAccountDetail.getAuditDetails().setLastModifiedTime(currentTime);
                        }
                    });
                }
            });
        }
    }



}
