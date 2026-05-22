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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Daily reconciliation of pending transactions older than the early reconcile interval * 2.
 */
@Component
@Slf4j
public class DailyReconciliationJob implements Job {

    @Autowired
    private AppProperties appProperties;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private TransactionRepository transactionRepository;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        List<Transaction> pendingTxns = transactionRepository.fetchTransactionsByTimeRange(
                TransactionCriteria.builder()
                        .txnStatus(Transaction.TxnStatusEnum.PENDING).build(),
                0L,
                System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(appProperties.getEarlyReconcileJobRunInterval() * 2));

        log.info("DailyReconciliation: Attempting to reconcile {} pending transactions", pendingTxns.size());

        for (Transaction txn : pendingTxns) {
            log.info(transactionService.updateTransaction(
                    Collections.singletonMap(PgConstants.PG_TXN_IN_LABEL, txn.getTxnId()),
                    txn.getTenantId(),
                    appProperties.getEgovPgReconciliationSystemUserUuid()
            ).toString());
        }
    }
}
