package org.egov.payment.service.jobs;

import lombok.extern.slf4j.Slf4j;
import org.egov.payment.config.AppProperties;
import org.egov.payment.constants.PgConstants;
import org.egov.payment.models.Transaction;
import org.egov.payment.repository.TransactionRepository;
import org.egov.payment.service.TransactionService;
import org.egov.payment.web.models.TransactionCriteria;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Updates all transactions in pending state created in the last configured interval.
 */
@Component
@Slf4j
public class EarlyReconciliationJob implements Job {

    @Autowired
    private AppProperties appProperties;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private TransactionRepository transactionRepository;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        int startTime = appProperties.getEarlyReconcileJobRunInterval() * 2;
        int endTime = startTime - appProperties.getEarlyReconcileJobRunInterval();

        List<Transaction> pendingTxns = transactionRepository.fetchTransactionsByTimeRange(
                TransactionCriteria.builder()
                        .txnStatus(Transaction.TxnStatusEnum.PENDING).build(),
                System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(startTime),
                System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(endTime));

        log.info("EarlyReconciliation: Attempting to reconcile {} pending transactions", pendingTxns.size());

        for (Transaction txn : pendingTxns) {
            log.info(transactionService.updateTransaction(
                    Collections.singletonMap(PgConstants.PG_TXN_IN_LABEL, txn.getTxnId()),
                    txn.getTenantId(),
                    appProperties.getEgovPgReconciliationSystemUserUuid()
            ).toString());
        }
    }
}
