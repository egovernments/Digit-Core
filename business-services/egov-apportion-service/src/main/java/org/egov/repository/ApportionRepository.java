package org.egov.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.web.models.*;
import org.egov.web.models.enums.DemandApportionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Slf4j
public class ApportionRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public ApportionRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Saves bill request to database
     * @param request The apportion request
     * @param tenantId The tenant ID from header
     * @param clientId The client ID from header
     * @param currentTime The consistent timestamp for this request
     */
    public void saveBillRequest(ApportionRequest request, String tenantId, String clientId, Long currentTime) {
        try {
            for (Bill bill : request.getBills()) {
                String billDetailsJson = objectMapper.writeValueAsString(bill.getBillDetails());
                
                String sql = """
                    INSERT INTO eg_appr_bills_request 
                    (billDetails, tenantId, payerName, payerAddress, payerEmail, paidBy, 
                     billId, isActive, isCancelled, mobileNumber, createdBy, createdTime)
                    VALUES (?::jsonb, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """;
                
                jdbcTemplate.update(sql,
                    billDetailsJson,
                    tenantId,
                    bill.getPayerName(),
                    bill.getPayerAddress(),
                    bill.getPayerEmail(),
                    bill.getPaidBy(),
                    bill.getId(),
                    true, // isActive
                    bill.getIsCancelled(),
                    bill.getMobileNumber(),
                    clientId,
                    currentTime
                );
            }
            log.info("Successfully saved bill request for tenantId: {}", tenantId);
        } catch (Exception e) {
            log.error("Error saving bill request: ", e);
            throw new RuntimeException("Failed to save bill request", e);
        }
    }

    /**
     * Saves bill response to database
     * @param bills The apportioned bills
     * @param tenantId The tenant ID from header
     * @param clientId The client ID from header
     * @param currentTime The consistent timestamp for this request
     */
    public void saveBillResponse(List<Bill> bills, String tenantId, String clientId, Long currentTime) {
        try {
            for (Bill bill : bills) {
                String billDetailsJson = objectMapper.writeValueAsString(bill.getBillDetails());
                
                String sql = """
                    INSERT INTO eg_appr_bills_response 
                    (billDetails, tenantId, payerName, payerAddress, payerEmail, paidBy, 
                     billId, isActive, isCancelled, mobileNumber, createdBy, createdTime)
                    VALUES (?::jsonb, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """;
                
                jdbcTemplate.update(sql,
                    billDetailsJson,
                    tenantId,
                    bill.getPayerName(),
                    bill.getPayerAddress(),
                    bill.getPayerEmail(),
                    bill.getPaidBy(),
                    bill.getId(),
                    true, // isActive
                    bill.getIsCancelled(),
                    bill.getMobileNumber(),
                    clientId,
                    currentTime
                );
            }
            log.info("Successfully saved bill response for tenantId: {}", tenantId);
        } catch (Exception e) {
            log.error("Error saving bill response: ", e);
            throw new RuntimeException("Failed to save bill response", e);
        }
    }

    /**
     * Saves demand request to database
     * @param request The demand apportion request
     * @param tenantId The tenant ID from header
     * @param clientId The client ID from header
     * @param currentTime The consistent timestamp for this request
     */
    public void saveDemandRequest(DemandApportionRequest request, String tenantId, String clientId, Long currentTime) {
        try {
            for (Demand demand : request.getDemands()) {
                String demandDetailsJson = objectMapper.writeValueAsString(demand.getDemandDetails());
                
                String sql = """
                    INSERT INTO eg_appr_demand_request 
                    (demandId, tenantId, consumerCode, taxperiodfrom, taxperiodto, 
                     status, demandDetails, createdBy, createdTime)
                    VALUES (?, ?, ?, ?, ?, ?, ?::jsonb, ?, ?)
                    """;
                
                jdbcTemplate.update(sql,
                    demand.getId(),
                    tenantId,
                    demand.getConsumerCode(),
                    demand.getTaxPeriodFrom(),
                    demand.getTaxPeriodTo(),
                    demand.getStatus() != null ? demand.getStatus().toString() : null,
                    demandDetailsJson,
                    clientId,
                    currentTime
                );
            }
            log.info("Successfully saved demand request for tenantId: {}", tenantId);
        } catch (Exception e) {
            log.error("Error saving demand request: ", e);
            throw new RuntimeException("Failed to save demand request", e);
        }
    }

    /**
     * Saves demand response to database
     * @param demands The apportioned demands
     * @param tenantId The tenant ID from header
     * @param clientId The client ID from header
     * @param currentTime The consistent timestamp for this request
     */
    public void saveDemandResponse(List<Demand> demands, String tenantId, String clientId, Long currentTime) {
        try {
            for (Demand demand : demands) {
                String demandDetailsJson = objectMapper.writeValueAsString(demand.getDemandDetails());
                
                String sql = """
                    INSERT INTO eg_appr_demand_response 
                    (demandId, tenantId, consumerCode, taxperiodfrom, taxperiodto, 
                     status, demandDetails, createdBy, createdTime)
                    VALUES (?, ?, ?, ?, ?, ?, ?::jsonb, ?, ?)
                    """;
                
                jdbcTemplate.update(sql,
                    demand.getId(),
                    tenantId,
                    demand.getConsumerCode(),
                    demand.getTaxPeriodFrom(),
                    demand.getTaxPeriodTo(),
                    demand.getStatus() != null ? demand.getStatus().toString() : null,
                    demandDetailsJson,
                    clientId,
                    currentTime
                );
            }
            log.info("Successfully saved demand response for tenantId: {}", tenantId);
        } catch (Exception e) {
            log.error("Error saving demand response: ", e);
            throw new RuntimeException("Failed to save demand response", e);
        }
    }
}