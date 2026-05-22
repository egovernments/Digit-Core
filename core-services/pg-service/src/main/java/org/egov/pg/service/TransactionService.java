package org.egov.pg.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.pg.config.AppProperties;
import org.egov.pg.constants.PgConstants;
import org.egov.pg.messaging.producer.Producer;
import org.egov.pg.models.Transaction;
import org.egov.pg.models.TransactionDump;
import org.egov.pg.repository.TransactionRepository;
import org.egov.pg.validator.TransactionValidator;
import org.egov.pg.web.models.TransactionCriteria;
import org.egov.pg.web.models.TransactionRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.springframework.util.StringUtils.isEmpty;

/**
 * Handles all transaction related requests
 */
@Service
@Slf4j
public class TransactionService {

	private final TransactionValidator validator;
	private final GatewayService gatewayService;
	private final Producer producer;
	private final EnrichmentService enrichmentService;
	private final AppProperties appProperties;
	private final TransactionRepository transactionRepository;
	private final PaymentsService paymentsService;


	@Autowired
	public TransactionService(TransactionValidator validator, GatewayService gatewayService, Producer producer,
							  TransactionRepository transactionRepository, PaymentsService paymentsService,
							  EnrichmentService enrichmentService, AppProperties appProperties) {
		this.validator = validator;
		this.gatewayService = gatewayService;
		this.producer = producer;
		this.transactionRepository = transactionRepository;
		this.paymentsService = paymentsService;
		this.enrichmentService = enrichmentService;
		this.appProperties = appProperties;
	}

	/**
	 * Initiates a transaction by generating a gateway redirect URI for the request
	 * <p>
	 * 1. Validates transaction object
	 * 2. Enriches the request by assigning a TxnID and a default status of PENDING
	 * 3. If yes, calls the gateway's implementation to generate a redirect URI
	 * 4. Persists the transaction and a transaction dump with the RAW requests asynchronously
	 * 5. Returns the redirect URI
	 *
	 * @param transactionRequest Valid transaction request for which transaction needs to be initiated
	 * @return Redirect URI to the gateway for the particular transaction
	 */
	public Transaction initiateTransaction(TransactionRequest transactionRequest, String tenantId, String userId, String requestId) {
		Transaction txn = transactionRequest.getTransaction();

		// Validate Transaction
		validator.validateCreateTxn(txn);
		paymentsService.validatePayment(txn, tenantId, userId);

		// Enrich transaction by generating txnId, audit details, default status
		enrichmentService.enrichCreateTransaction(txn, tenantId, userId);

		Transaction transaction = transactionRequest.getTransaction();

		TransactionDump dump = TransactionDump.builder()
				.txnId(transaction.getTxnId())
				.auditDetails(transaction.getAuditDetails())
				.build();

		if (skipGateway(transaction)) {
			transaction.setTxnStatus(Transaction.TxnStatusEnum.SUCCESS);
			paymentsService.registerPayment(txn, tenantId, userId);
		} else {
			URI uri = gatewayService.initiateTxn(transaction);
			transaction.setRedirectUrl(uri.toString());

			dump.setTxnRequest(uri.toString());
		}

		// Persist transaction and transaction dump objects
		transactionRepository.saveTransaction(txn, requestId);
		transactionRepository.saveTransactionDump(dump, requestId);

		// Publish transaction create event to messaging queue
		if (Boolean.TRUE.equals(appProperties.getMessageBrokerEnabled())) {
			producer.push(appProperties.getUpdateTxnTopic(), transaction);
			log.info("Published create transaction event for txnId: {}", transaction.getTxnId());
		}

		return transaction;
	}


	/**
	 * Fetches a list of transactions matching the current criteria
	 * <p>
	 * Currently has a hard limit of 5 records, configurable
	 *
	 * @param transactionCriteria Search Conditions that should be matched
	 * @return List of transactions matching the conditions.
	 */
	public List<Transaction> getTransactions(TransactionCriteria transactionCriteria) {
		log.info(transactionCriteria.toString());
		try {
			return transactionRepository.fetchTransactions(transactionCriteria);
		} catch (DataAccessException e) {
			log.error("Unable to fetch data from the database for criteria: " + transactionCriteria.toString(), e);
			throw new CustomException("FETCH_TXNS_FAILED", "Unable to fetch transactions from store");
		}
	}

	/**
	 * Updates the status of the transaction from the gateway
	 * <p>
	 * 1. Fetch TXN ID from the request params, if not found, exit!
	 * 2. Fetch current transaction status from DB, if not found, exit!
	 * 3. Fetch the current transaction status from the payment gateway
	 * 4. Verify the amount returned from the gateway matches our records
	 * 5. If successful, generate receipt
	 * 6. Persist the updated transaction status and raw gateway transaction response
	 *
	 * @param requestParams Response parameters posted by the gateway
	 * @param tenantId
	 * @param clientId
	 * @return Updated transaction
	 */
	public List<Transaction> updateTransaction(Map<String, String> requestParams, String tenantId, String userId, String requestId) {

		Transaction currentTxnStatus = validator.validateUpdateTxn(requestParams);
		if (tenantId == null) {
			tenantId = currentTxnStatus.getTenantId();
		}

		log.debug(currentTxnStatus.toString());
		log.debug(requestParams.toString());

		Transaction newTxn = null;

		if (skipGateway(currentTxnStatus)) {
			newTxn = currentTxnStatus;

		} else {
			newTxn = gatewayService.getLiveStatus(currentTxnStatus, requestParams);

			// Enrich the new transaction status before persisting
			enrichmentService.enrichUpdateTransaction(currentTxnStatus, newTxn, userId);
		}

		// Check if transaction is successful, amount matches etc
		if (shouldGenerateReceipt(currentTxnStatus, newTxn)) {
			paymentsService.registerPayment(newTxn, tenantId, userId);
		}

		TransactionDump dump = TransactionDump.builder()
				.txnId(currentTxnStatus.getTxnId())
				.txnResponse(newTxn.getResponseJson())
				.auditDetails(newTxn.getAuditDetails())
				.build();

		transactionRepository.updateTransaction(newTxn, requestId);
		transactionRepository.updateTransactionDump(dump, requestId);

		// Publish transaction update event to messaging queue
		if (Boolean.TRUE.equals(appProperties.getMessageBrokerEnabled())) {
			producer.push(appProperties.getUpdateTxnTopic(), newTxn);
			log.info("Published update transaction event for txnId: {}", newTxn.getTxnId());
		}

		return Collections.singletonList(newTxn);
	}

	public boolean skipGateway(Transaction transaction) {
		return new BigDecimal(transaction.getTxnAmount()).compareTo(BigDecimal.ZERO) == 0;
	}

	public boolean shouldGenerateReceipt(Transaction prevStatus, Transaction newStatus) {
		if (prevStatus.getTxnStatus().equals(Transaction.TxnStatusEnum.SUCCESS) && !isEmpty(prevStatus.getReceipt())) {
			return false;
		}

		if (newStatus.getTxnStatus().equals(Transaction.TxnStatusEnum.SUCCESS)) {
			if (new BigDecimal(prevStatus.getTxnAmount()).compareTo(new BigDecimal(newStatus.getTxnAmount())) == 0) {
				newStatus.setTxnStatus(Transaction.TxnStatusEnum.SUCCESS);
				newStatus.setTxnStatusMsg(PgConstants.TXN_SUCCESS);
				return true;
			} else {
				log.error("Transaction Amount mismatch, expected {} got {}", prevStatus.getTxnAmount(), newStatus
						.getTxnAmount());
				newStatus.setTxnStatus(Transaction.TxnStatusEnum.FAILURE);
				newStatus.setTxnStatusMsg(PgConstants.TXN_FAILURE_AMT_MISMATCH);
				return false;
			}
		} else {
			newStatus.setTxnStatus(Transaction.TxnStatusEnum.FAILURE);
			newStatus.setTxnStatusMsg(PgConstants.TXN_FAILURE_GATEWAY);
			return false;
		}
	}
}
