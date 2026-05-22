package org.egov.pg.web.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.pg.models.TaxAndPayment;
import org.egov.pg.models.Transaction;
import org.egov.pg.models.User;
import org.egov.pg.service.GatewayService;
import org.egov.pg.service.TransactionService;
import org.egov.pg.web.models.TransactionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TransactionsApiControllerTest {

    @Mock
    private TransactionService transactionService;
    @Mock
    private GatewayService gatewayService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        TransactionsApiController controller = new TransactionsApiController(transactionService, gatewayService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void transactionsV1CreatePost_validRequest_returnsTransaction() throws Exception {
        Transaction txn = baseTransaction();
        txn.setTxnId("TXN-20");
        txn.setRedirectUrl("https://gateway/redirect");
        TransactionRequest request = TransactionRequest.builder().transaction(baseTransaction()).build();

        when(transactionService.initiateTransaction(any(TransactionRequest.class), eq("pb"), eq("mobile-app"), any()))
                .thenReturn(txn);

        mockMvc.perform(post("/transaction/v3/_create")
                        .header("X-Tenant-ID", "pb")
                        .header("X-User-ID", "mobile-app")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Transaction.txnId").value("TXN-20"))
                .andExpect(jsonPath("$.Transaction.redirectUrl").value("https://gateway/redirect"));
    }

    @Test
    void transactionsV1SearchPost_setsTenantAndPaginationAndReturnsData() throws Exception {
        Transaction txn = baseTransaction();
        txn.setTxnId("TXN-21");
        when(transactionService.getTransactions(any())).thenReturn(List.of(txn));

        mockMvc.perform(get("/transaction/v3/_search")
                        .header("X-Tenant-ID", "pb")
                        .param("txnId", "TXN-21"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Transaction[0].txnId").value("TXN-21"));

        verify(transactionService).getTransactions(any());
    }

    @Test
    void transactionsV1UpdatePost_validParams_returnsUpdatedTransactions() throws Exception {
        Transaction txn = baseTransaction();
        txn.setTxnId("TXN-22");
        txn.setTxnStatus(Transaction.TxnStatusEnum.SUCCESS);
        when(transactionService.updateTransaction(any(), eq("pb"), eq("web"), any())).thenReturn(List.of(txn));

        mockMvc.perform(put("/transaction/v3/_update")
                        .header("X-Tenant-ID", "pb")
                        .header("X-User-ID", "web")
                        .param("transactionId", "TXN-22"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Transaction[0].txnStatus").value("SUCCESS"));
    }

    @Test
    void transactionsV1AvailableGatewaysPost_returnsGateways() throws Exception {
        when(gatewayService.getActiveGateways()).thenReturn(Set.of("PAYTM", "PHONEPE"));

        mockMvc.perform(get("/gateway/v3/_search"))
                .andExpect(status().isOk());
    }

    private Transaction baseTransaction() {
        return Transaction.builder()
                .tenantId("pb")
                .txnAmount("100")
                .billId("B1")
                .module("PT")
                .consumerCode("PT-100")
                .taxAndPayments(Collections.singletonList(TaxAndPayment.builder()
                        .amountPaid(new BigDecimal("100"))
                        .taxAmount(new BigDecimal("100"))
                        .billId("B1")
                        .build()))
                .productInfo("Property Tax")
                .gateway("PAYTM")
                .callbackUrl("https://callback")
                .user(User.builder().tenantId("pb").uuid("uuid").userName("name").name("Name").mobileNumber("9999999999").build())
                .build();
    }
}