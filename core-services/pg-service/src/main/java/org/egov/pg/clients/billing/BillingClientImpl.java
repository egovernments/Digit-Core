package org.egov.pg.clients.billing;

import lombok.extern.slf4j.Slf4j;
import org.egov.pg.clients.billing.models.Payment;
import org.egov.pg.clients.billing.models.PaymentCreate;
import org.egov.pg.config.AppProperties;
import org.egov.tracer.model.CustomException;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class BillingClientImpl implements BillingClient {

	private final RestClient restClient;
	private final AppProperties appProperties;

	public BillingClientImpl(RestClient restClient, AppProperties appProperties) {
		this.restClient = restClient;
		this.appProperties = appProperties;
	}

	@Override
	public Payment createPayment(String tenantId, String userId, PaymentCreate payment) {
		return callPaymentEndpoint(
				tenantId,
				userId,
				payment,
				appProperties.getBillingHost() + appProperties.getBillingPaymentCreatePath(),
				"create"
		);
	}

	@Override
	public Payment validatePayment(String tenantId, String userId, PaymentCreate payment) {
		return callPaymentEndpoint(
				tenantId,
				userId,
				payment,
				appProperties.getBillingHost() + appProperties.getBillingPaymentValidatePath(),
				"validate"
		);
	}

	private Payment callPaymentEndpoint(String tenantId, String userId,
										PaymentCreate payment, String uri, String operation) {
		try {
			Payment response = restClient.post()
					.uri(uri)
					.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
					.header("X-Tenant-ID", tenantId)
					.header("X-User-ID", userId)
					.body(payment)
					.retrieve()
					.body(Payment.class);

			if (response == null) {
				throw new CustomException("PAYMENT_" + operation.toUpperCase() + "_FAILED",
						"Failed to " + operation + " payment at billing service");
			}

			log.info("Payment {} successful, id: {}", operation, response.getId());
			return response;

		} catch (HttpClientErrorException e) {
			log.error("Billing client error during payment {}: status={}", operation, e.getStatusCode(), e);
			throw new ServiceCallException(e.getResponseBodyAsString());
		} catch (CustomException e) {
			throw e;
		} catch (Exception e) {
			log.error("Billing unknown error during payment {}", operation, e);
			throw new CustomException("BILLING_ERROR", "Failed to " + operation + " payment: " + e.getMessage());
		}
	}
}