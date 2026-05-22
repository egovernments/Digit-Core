package org.egov.pg.web.controllers;


import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.egov.pg.models.Transaction;
import org.egov.pg.service.GatewayService;
import org.egov.pg.service.TransactionService;
import org.egov.pg.web.models.TransactionCreateResponse;
import org.egov.pg.web.models.TransactionCriteria;
import org.egov.pg.web.models.TransactionRequest;
import org.egov.pg.web.models.TransactionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Endpoints to deal with all payment related operations
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
	 * Initiates a new payment transaction, on successful validation, a redirect is issued to the payment gateway.
	 *
	 * @param transactionRequest Request containing all information necessary for initiating payment
	 * @return Transaction that has been created
	 */
	@PostMapping(value = "/transaction/v3/_create")
	public ResponseEntity<TransactionCreateResponse> transactionsV1CreatePost(@RequestHeader("X-Tenant-ID") String tenantId, @RequestHeader("X-User-ID") String userId, @RequestHeader(value = "X-Request-ID", required = false) String requestId, @Valid @RequestBody TransactionRequest transactionRequest) {
		transactionRequest.getTransaction().setTenantId(tenantId);

		Transaction transaction = transactionService.initiateTransaction(transactionRequest, tenantId, userId, requestId);
		TransactionCreateResponse response = new TransactionCreateResponse(transaction);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * Returns the current status of a transaction in our systems;
	 * This does not guarantee live payment gateway status.
	 *
	 * @param transactionCriteria Search Conditions that should be matched
	 * @return List of transactions matching the search criteria
	 */
	@GetMapping(value = "/transaction/v3/_search")
	public ResponseEntity<TransactionResponse> transactionsV1SearchPost(@RequestHeader("X-Tenant-ID") String tenantId, @Valid @ModelAttribute TransactionCriteria transactionCriteria) {
		transactionCriteria.setTenantId(tenantId);
		transactionCriteria.setOffset(0);
		transactionCriteria.setLimit(5);
		List<Transaction> transactions = transactionService.getTransactions(transactionCriteria);
		TransactionResponse response = new TransactionResponse(transactions);

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * Updates the status of the transaction from the gateway
	 *
	 * @param params Parameters posted by the gateway
	 * @return The current transaction status of the transaction
	 */
	@PutMapping(value = "/transaction/v3/_update")
	public ResponseEntity<TransactionResponse> transactionsV1UpdatePost(@RequestHeader("X-Tenant-ID") String tenantId, @RequestHeader("X-User-ID") String userId, @RequestHeader(value = "X-Request-ID", required = false) String requestId, @RequestParam Map<String, String> params) {
		List<Transaction> transactions = transactionService.updateTransaction(params, tenantId, userId, requestId);
		TransactionResponse response = new TransactionResponse(transactions);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * Active payment gateways that can be used for payments
	 *
	 * @return list of active gateways that can be used for payments
	 */
	@GetMapping(value = "/gateway/v3/_search")
	public ResponseEntity<Set<String>> transactionsV1AvailableGatewaysPost() {

		Set<String> gateways = gatewayService.getActiveGateways();
		log.debug("Available gateways : " + gateways);
		return new ResponseEntity<>(gateways, HttpStatus.OK);
	}
}
