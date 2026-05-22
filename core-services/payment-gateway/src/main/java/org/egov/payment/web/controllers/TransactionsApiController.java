package org.egov.payment.web.controllers;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.egov.payment.models.Transaction;
import org.egov.payment.service.GatewayService;
import org.egov.payment.service.TransactionService;
import org.egov.payment.web.models.TransactionCreateResponse;
import org.egov.payment.web.models.TransactionCriteria;
import org.egov.payment.web.models.TransactionRequest;
import org.egov.payment.web.models.TransactionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Endpoints for all payment-related operations.
 */
@Slf4j
@Controller
public class TransactionsApiController {

    private final TransactionService transactionService;
    private final GatewayService gatewayService;

    @Autowired
    public TransactionsApiController(TransactionService transactionService, GatewayService gatewayService) {
        this.transactionService = transactionService;
        this.gatewayService = gatewayService;
    }

    /**
     * Initiates a new payment transaction; returns redirect URI on success.
     */
    @PostMapping(value = "/transaction/v3/_create")
    public ResponseEntity<TransactionCreateResponse> transactionsV1CreatePost(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @RequestHeader("X-Client-ID") String clientId,
            @Valid @RequestBody TransactionRequest transactionRequest) {
        transactionRequest.getTransaction().setTenantId(tenantId);
        Transaction transaction = transactionService.initiateTransaction(transactionRequest, tenantId, clientId);
        TransactionCreateResponse response = new TransactionCreateResponse(transaction);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Returns the current status of transactions matching the criteria.
     */
    @GetMapping(value = "/transaction/v3/_search")
    public ResponseEntity<TransactionResponse> transactionsV1SearchPost(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @Valid @ModelAttribute TransactionCriteria transactionCriteria) {
        transactionCriteria.setTenantId(tenantId);
        transactionCriteria.setOffset(0);
        transactionCriteria.setLimit(5);
        List<Transaction> transactions = transactionService.getTransactions(transactionCriteria);
        TransactionResponse response = new TransactionResponse(transactions);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Updates the status of a transaction from the gateway callback.
     */
    @PutMapping(value = "/transaction/v3/_update")
    public ResponseEntity<TransactionResponse> transactionsV1UpdatePost(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @RequestHeader("X-Client-ID") String clientId,
            @RequestParam Map<String, String> params) {
        List<Transaction> transactions = transactionService.updateTransaction(params, tenantId, clientId);
        TransactionResponse response = new TransactionResponse(transactions);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Returns the set of active payment gateways.
     */
    @GetMapping(value = "/gateway/v3/_search")
    public ResponseEntity<Set<String>> transactionsV1AvailableGatewaysPost() {
        Set<String> gateways = gatewayService.getActiveGateways();
        log.debug("Available gateways: {}", gateways);
        return new ResponseEntity<>(gateways, HttpStatus.OK);
    }
}
