package org.egov.pg.service;

import org.egov.pg.config.AppProperties;
import org.egov.pg.constants.PgConstants;
import org.egov.pg.messaging.producer.Producer;
import org.egov.pg.models.TaxAndPayment;
import org.egov.pg.models.Transaction;
import org.egov.pg.models.TransactionDump;
import org.egov.pg.models.User;
import org.egov.pg.repository.TransactionRepository;
import org.egov.pg.validator.TransactionValidator;
import org.egov.pg.web.models.TransactionCriteria;
import org.egov.pg.web.models.TransactionRequest;
import org.egov.tracer.model.AuditDetails;
import org.egov.tracer.model.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionValidator validator;
    @Mock
    private GatewayService gatewayService;
    @Mock
    private Producer producer;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private PaymentsService paymentsService;
    @Mock
    private EnrichmentService enrichmentService;
    @Mock
    private AppProperties appProperties;

    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(
                validator, gatewayService, producer, transactionRepository, paymentsService, enrichmentService, appProperties);
    }

    @Test
    void initiateTransaction_gatewayFlow_persistsDumpAndPublishesEvent() throws Exception {
        Transaction txn = baseTransaction("100.00", Transaction.TxnStatusEnum.PENDING);
        txn.setTxnId("TXN-1");
        txn.setAuditDetails(new AuditDetails("u", "u", 1L, 1L));
        TransactionRequest request = TransactionRequest.builder().transaction(txn).build();
        when(gatewayService.initiateTxn(txn)).thenReturn(new URI("https://gateway.test/redirect"));
        when(appProperties.getMessageBrokerEnabled()).thenReturn(true);
        when(appProperties.getUpdateTxnTopic()).thenReturn("pg-update-topic");

        Transaction result = transactionService.initiateTransaction(request, "pb.amritsar", "client-a", null);

        assertThat(result.getRedirectUrl()).isEqualTo("https://gateway.test/redirect");
        verify(validator).validateCreateTxn(txn);
        verify(paymentsService).validatePayment(txn, "pb.amritsar", "client-a");
        verify(enrichmentService).enrichCreateTransaction(txn, "pb.amritsar", "client-a");
        verify(transactionRepository).saveTransaction(eq(txn), any());

        ArgumentCaptor<TransactionDump> dumpCaptor = ArgumentCaptor.forClass(TransactionDump.class);
        verify(transactionRepository).saveTransactionDump(dumpCaptor.capture(), any());
        assertThat(dumpCaptor.getValue().getTxnRequest()).isEqualTo("https://gateway.test/redirect");

        verify(producer).push("pg-update-topic", txn);
        verify(paymentsService, never()).registerPayment(any(), any(), any());
    }

    @Test
    void initiateTransaction_zeroAmount_skipsGatewayAndRegistersPayment() {
        Transaction txn = baseTransaction("0", Transaction.TxnStatusEnum.PENDING);
        txn.setTxnId("TXN-2");
        txn.setAuditDetails(new AuditDetails("u", "u", 1L, 1L));
        TransactionRequest request = TransactionRequest.builder().transaction(txn).build();
        when(appProperties.getMessageBrokerEnabled()).thenReturn(false);

        Transaction result = transactionService.initiateTransaction(request, "pb", "client", null);

        assertThat(result.getTxnStatus()).isEqualTo(Transaction.TxnStatusEnum.SUCCESS);
        verify(paymentsService).registerPayment(txn, "pb", "client");
        verify(gatewayService, never()).initiateTxn(any());
        verify(producer, never()).push(any(), any());
    }

    @Test
    void getTransactions_repositoryThrows_throwsCustomException() {
        TransactionCriteria criteria = TransactionCriteria.builder().tenantId("pb").txnId("TXN-3").build();
        when(transactionRepository.fetchTransactions(criteria)).thenThrow(new DataAccessResourceFailureException("db down"));

        assertThatThrownBy(() -> transactionService.getTransactions(criteria))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException ce = (CustomException) ex;
                    assertThat(ce.getCode()).isEqualTo("FETCH_TXNS_FAILED");
                    assertThat(ce.getMessage()).contains("Unable to fetch transactions from store");
                });
    }

    @Test
    void updateTransaction_skipGatewayAndNullTenant_registersPaymentWithExistingTenant() {
        Transaction existing = baseTransaction("0", Transaction.TxnStatusEnum.SUCCESS);
        existing.setTxnId("TXN-4");
        existing.setTenantId("pb.tenant");
        existing.setAuditDetails(new AuditDetails("u", "u", 1L, 1L));

        when(validator.validateUpdateTxn(any())).thenReturn(existing);
        when(appProperties.getMessageBrokerEnabled()).thenReturn(false);

        List<Transaction> updated = transactionService.updateTransaction(Map.of("transactionId", "TXN-4"), null, "client-b", null);

        assertThat(updated).hasSize(1);
        assertThat(updated.get(0)).isSameAs(existing);
        verify(paymentsService).registerPayment(existing, "pb.tenant", "client-b");
        verify(gatewayService, never()).getLiveStatus(any(), any());
        verify(enrichmentService, never()).enrichUpdateTransaction(any(), any(), any());
        verify(transactionRepository).updateTransaction(eq(existing), any());
        verify(transactionRepository).updateTransactionDump(any(TransactionDump.class), any());
    }

    @Test
    void updateTransaction_gatewaySuccessAmountMatches_enrichesAndPublishes() {
        Transaction previous = baseTransaction("100", Transaction.TxnStatusEnum.PENDING);
        previous.setTxnId("TXN-5");
        previous.setTenantId("pb");

        Transaction fromGateway = baseTransaction("100.00", Transaction.TxnStatusEnum.SUCCESS);
        fromGateway.setTxnId("TXN-5");
        fromGateway.setAuditDetails(new AuditDetails("u", "u", 1L, 2L));
        fromGateway.setResponseJson(Map.of("status", "ok"));

        when(validator.validateUpdateTxn(any())).thenReturn(previous);
        when(gatewayService.getLiveStatus(eq(previous), any())).thenReturn(fromGateway);
        when(appProperties.getMessageBrokerEnabled()).thenReturn(true);
        when(appProperties.getUpdateTxnTopic()).thenReturn("topic-update");

        List<Transaction> result = transactionService.updateTransaction(Map.of("transactionId", "TXN-5"), "pb", "client-c", null);

        assertThat(result).singleElement().isSameAs(fromGateway);
        assertThat(fromGateway.getTxnStatus()).isEqualTo(Transaction.TxnStatusEnum.SUCCESS);
        assertThat(fromGateway.getTxnStatusMsg()).isEqualTo(PgConstants.TXN_SUCCESS);
        verify(enrichmentService).enrichUpdateTransaction(previous, fromGateway, "client-c");
        verify(paymentsService).registerPayment(fromGateway, "pb", "client-c");
        verify(producer).push("topic-update", fromGateway);
    }

    @Test
    void shouldGenerateReceipt_previousAlreadySuccessfulWithReceipt_returnsFalse() {
        Transaction previous = baseTransaction("10", Transaction.TxnStatusEnum.SUCCESS);
        previous.setReceipt("PT-REC-1");
        Transaction latest = baseTransaction("10", Transaction.TxnStatusEnum.SUCCESS);

        boolean shouldGenerate = transactionService.shouldGenerateReceipt(previous, latest);

        assertThat(shouldGenerate).isFalse();
    }

    @Test
    void shouldGenerateReceipt_amountMismatch_marksFailure() {
        Transaction previous = baseTransaction("100", Transaction.TxnStatusEnum.PENDING);
        Transaction latest = baseTransaction("90", Transaction.TxnStatusEnum.SUCCESS);

        boolean shouldGenerate = transactionService.shouldGenerateReceipt(previous, latest);

        assertThat(shouldGenerate).isFalse();
        assertThat(latest.getTxnStatus()).isEqualTo(Transaction.TxnStatusEnum.FAILURE);
        assertThat(latest.getTxnStatusMsg()).isEqualTo(PgConstants.TXN_FAILURE_AMT_MISMATCH);
    }

    @Test
    void shouldGenerateReceipt_gatewayFailure_marksFailure() {
        Transaction previous = baseTransaction("100", Transaction.TxnStatusEnum.PENDING);
        Transaction latest = baseTransaction("100", Transaction.TxnStatusEnum.FAILURE);

        boolean shouldGenerate = transactionService.shouldGenerateReceipt(previous, latest);

        assertThat(shouldGenerate).isFalse();
        assertThat(latest.getTxnStatusMsg()).isEqualTo(PgConstants.TXN_FAILURE_GATEWAY);
    }

    @Test
    void skipGateway_zeroAndNonZeroAmount_returnsExpectedFlag() {
        assertThat(transactionService.skipGateway(baseTransaction("0.00", Transaction.TxnStatusEnum.PENDING))).isTrue();
        assertThat(transactionService.skipGateway(baseTransaction("1", Transaction.TxnStatusEnum.PENDING))).isFalse();
    }

    private Transaction baseTransaction(String amount, Transaction.TxnStatusEnum status) {
        TaxAndPayment tax = TaxAndPayment.builder()
                .amountPaid(new BigDecimal(amount))
                .taxAmount(new BigDecimal(amount))
                .billId("B1")
                .build();

        User user = User.builder()
                .tenantId("pb")
                .name("Test User")
                .mobileNumber("9999999999")
                .uuid("uuid-1")
                .userName("user-1")
                .build();

        return Transaction.builder()
                .tenantId("pb")
                .txnAmount(amount)
                .billId("B1")
                .module("PT")
                .consumerCode("PT-1")
                .taxAndPayments(Collections.singletonList(tax))
                .productInfo("Property Tax")
                .gateway("PAYTM")
                .callbackUrl("https://callback")
                .user(user)
                .txnStatus(status)
                .build();
    }
}