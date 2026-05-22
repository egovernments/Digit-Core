package org.egov.pg.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.pg.clients.billing.BillingClient;
import org.egov.pg.clients.billing.models.Payment;
import org.egov.pg.clients.billing.models.PaymentCreate;
import org.egov.pg.clients.billing.models.PaymentDetailCreate;
import org.egov.pg.clients.billing.models.enums.PaymentMode;
import org.egov.pg.models.Transaction;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PaymentsService {

	private final BillingClient billingClient;
	private final ObjectMapper mapper;

	public PaymentsService(BillingClient billingClient, ObjectMapper mapper) {
		this.billingClient = billingClient;
		this.mapper = mapper;
	}

	public Payment registerPayment(Transaction txn, String tenantId, String userId) {
		PaymentCreate paymentCreate = toPaymentCreate(txn);
		paymentCreate.setInstrumentDate(txn.getAuditDetails().getCreatedTime());
		paymentCreate.setInstrumentNumber(txn.getTxnId());
		paymentCreate.setTransactionNumber(txn.getTxnId());

		if (txn.getAdditionalDetails() != null) {
			Map<String, Object> metadata = mapper.convertValue(txn.getAdditionalDetails(),
					new TypeReference<Map<String, Object>>() {
					});
			paymentCreate.setMetadata(metadata);
		}
		return billingClient.createPayment(tenantId, userId, paymentCreate);
	}

	public Payment validatePayment(Transaction txn, String tenantId, String userId) {
		PaymentCreate paymentCreate = toPaymentCreate(txn);
		return billingClient.validatePayment(tenantId, userId, paymentCreate);
	}

	private PaymentCreate toPaymentCreate(Transaction txn) {
		List<PaymentDetailCreate> paymentDetails = txn.getTaxAndPayments().stream()
				.map(taxAndPayment -> PaymentDetailCreate.builder()
						.billId(taxAndPayment.getBillId())
						.totalAmountPaid(taxAndPayment.getAmountPaid())
						.build())
				.toList();

		return PaymentCreate.builder()
				.totalAmountPaid(new BigDecimal(txn.getTxnAmount()))
				.paymentMode(PaymentMode.ONLINE)
				.paidBy(txn.getUser().getName())
				.payerId(txn.getUser().getUuid())
				.payerName(txn.getUser().getName())
				.payerMobileNumber(txn.getUser().getMobileNumber())
				.payerEmail(txn.getUser().getEmailId())
				.instrumentDate(System.currentTimeMillis())
				.instrumentNumber("PROV_PAYMENT_VALIDATION")
				.transactionNumber("PROV_PAYMENT_VALIDATION")
				.paymentDetails(paymentDetails)
				.build();
	}
}
