package org.egov.payment.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.payment.config.AppProperties;
import org.egov.payment.constants.PgConstants;
import org.egov.payment.messaging.producer.Producer;
import org.egov.payment.models.Transaction;
import org.egov.payment.models.TransactionDump;
import org.egov.payment.repository.TransactionRepository;
import org.egov.payment.validator.TransactionValidator;
import org.egov.payment.web.models.TransactionCriteria;
import org.egov.payment.web.models.TransactionRequest;
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
 * Handles all transaction related requests.
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
     * Initiates a transaction by generating a gateway redirect URI.
     */
    public Transaction initiateTransaction(TransactionRequest transactionRequest, String tenantId, String clientId) {
        Transaction txn = transactionRequest.getTransaction();

        validator.validateCreateTxn(txn);
        paymentsService.validatePayment(txn, tenantId, clientId);

        enrichmentService.enrichCreateTransaction(txn, tenantId, clientId);

        Transaction transaction = transactionRequest.getTransaction();

        TransactionDump dump = TransactionDump.builder()
                .txnId(transaction.getTxnId())
                .auditDetails(transaction.getAuditDetails())
                .build();

        if (skipGateway(transaction)) {
            transaction.setTxnStatus(Transaction.TxnStatusEnum.SUCCESS);
            paymentsService.registerPayment(txn, tenantId, clientId);
        } else {
            URI uri = gatewayService.initiateTxn(transaction);
            transaction.setRedirectUrl(uri.toString());
            dump.setTxnRequest(uri.toString());
        }

        transactionRepository.saveTransaction(txn);
        transactionRepository.saveTransactionDump(dump);

        if (Boolean.TRUE.equals(appProperties.getMessageBrokerEnabled())) {
            producer.push(appProperties.getCreateTxnTopic(), transaction);
            log.info("Published create transaction event for txnId: {}", transaction.getTxnId());
        }

        return transaction;
    }

    /**
     * Fetches a list of transactions matching the criteria.
     */
    public List<Transaction> getTransactions(TransactionCriteria transactionCriteria) {
        log.info(transactionCriteria.toString());
        try {
            return transactionRepository.fetchTransactions(transactionCriteria);
        } catch (DataAccessException e) {
            log.error("Unable to fetch data from the database for criteria: {}", transactionCriteria, e);
            throw new CustomException("FETCH_TXNS_FAILED", "Unable to fetch transactions from store");
        }
    }

    /**
     * Updates the status of a transaction from the gateway callback.
     */
    public List<Transaction> updateTransaction(Map<String, String> requestParams, String tenantId, String clientId) {
        Transaction currentTxnStatus = validator.validateUpdateTxn(requestParams);
        if (tenantId == null) {
            tenantId = currentTxnStatus.getTenantId();
        }

        log.debug(currentTxnStatus.toString());
        log.debug(requestParams.toString());

        Transaction newTxn;

        if (skipGateway(currentTxnStatus)) {
            newTxn = currentTxnStatus;
        } else {
            newTxn = gatewayService.getLiveStatus(currentTxnStatus, requestParams);
            enrichmentService.enrichUpdateTransaction(currentTxnStatus, newTxn, clientId);
        }

        if (shouldGenerateReceipt(currentTxnStatus, newTxn)) {
            paymentsService.registerPayment(newTxn, tenantId, clientId);
        }

        TransactionDump dump = TransactionDump.builder()
                .txnId(currentTxnStatus.getTxnId())
                .txnResponse(newTxn.getResponseJson())
                .auditDetails(newTxn.getAuditDetails())
                .build();

        transactionRepository.updateTransaction(newTxn);
        transactionRepository.updateTransactionDump(dump);

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
                log.error("Transaction Amount mismatch, expected {} got {}", prevStatus.getTxnAmount(), newStatus.getTxnAmount());
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
