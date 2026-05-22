package org.egov.payment.clients.billing;

import org.egov.payment.clients.billing.models.Payment;
import org.egov.payment.clients.billing.models.PaymentCreate;

public interface BillingClient {

    Payment createPayment(String tenantId, String clientId, PaymentCreate payment);

    Payment validatePayment(String tenantId, String clientId, PaymentCreate payment);
}
