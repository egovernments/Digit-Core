package org.egov.pg.service.gateways.stripe;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentRetrieveParams;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionRetrieveParams;
import lombok.extern.slf4j.Slf4j;
import org.egov.pg.constants.PgConstants;
import org.egov.pg.models.Transaction;
import org.egov.pg.service.Gateway;
import org.egov.pg.utils.Utils;
import org.egov.tracer.model.CustomException;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Stripe Gateway implementation, based on Stripe hosted Checkout Sessions
 * <p>
 * The Checkout Session id is stored in the transaction's gatewayTxnId at initiation,
 * status is always fetched server-to-server from Stripe's API (never trusted from
 * redirect/callback params), which serves both the browser callback and the
 * reconciliation jobs.
 */
@Component
@Slf4j
public class StripeGateway implements Gateway {

    private static final String GATEWAY_NAME = "STRIPE";

    // Checkout sessions expire after 30 minutes, aligned with the reconciliation
    // window, so a citizen cannot complete payment after reconciliation has
    // already marked the transaction as failed. 30 minutes is Stripe's minimum.
    private static final long SESSION_EXPIRY_SECONDS = 30 * 60;

    private final String CURRENCY;
    private final boolean ACTIVE;

    private final StripeClient stripeClient;
    private final ObjectMapper objectMapper;

    /**
     * Initialize by populating all required config parameters
     *
     * @param environment containing all required config parameters
     */
    @Autowired
    public StripeGateway(Environment environment, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;

        ACTIVE = Boolean.parseBoolean(environment.getRequiredProperty("stripe.active"));
        CURRENCY = environment.getRequiredProperty("stripe.currency");
        String secretKey = environment.getRequiredProperty("stripe.secret.key");

        // Allow the app to boot with an empty key when the gateway is unused
        this.stripeClient = StringUtils.hasText(secretKey) ? new StripeClient(secretKey) : null;
    }

    @Override
    public URI generateRedirectURI(Transaction transaction) {
        if (Objects.isNull(stripeClient))
            throw new CustomException("STRIPE_KEY_MISSING", "Stripe secret key is not configured");

        SessionCreateParams.Builder params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setClientReferenceId(transaction.getTxnId())
                .setSuccessUrl(transaction.getCallbackUrl())
                .setCancelUrl(transaction.getCallbackUrl())
                .setExpiresAt(Instant.now().getEpochSecond() + SESSION_EXPIRY_SECONDS)
                .putMetadata("txnId", transaction.getTxnId())
                .putMetadata("tenantId", transaction.getTenantId())
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency(CURRENCY)
                                .setUnitAmount(Long.parseLong(Utils.formatAmtAsPaise(transaction.getTxnAmount())))
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName(transaction.getProductInfo())
                                        .build())
                                .build())
                        .build());

        if (StringUtils.hasText(transaction.getUser().getEmailId()))
            params.setCustomerEmail(transaction.getUser().getEmailId());

        try {
            Session session = stripeClient.checkout().sessions().create(params.build(),
                    RequestOptions.builder().setIdempotencyKey(transaction.getTxnId()).build());

            // Persisted with the transaction, used later to fetch status from Stripe
            transaction.setGatewayTxnId(session.getId());

            return URI.create(session.getUrl());
        } catch (StripeException e) {
            log.error("Unable to create Stripe checkout session for txnid: " + transaction.getTxnId(), e);
            throw new ServiceCallException("Redirect URI generation failed, invalid response received from gateway");
        }
    }

    @Override
    public Transaction fetchStatus(Transaction currentStatus, Map<String, String> params) {
        // Stripe redirects carry no signed params, status is always fetched from
        // Stripe's authenticated API using the reference stored at initiation
        String gatewayRef = currentStatus.getGatewayTxnId();

        if (!StringUtils.hasText(gatewayRef)) {
            log.error("No Stripe reference found for txnid: {}", currentStatus.getTxnId());
            return currentStatus;
        }

        try {
            Session session = null;
            PaymentIntent paymentIntent;

            if (gatewayRef.startsWith("pi_")) {
                paymentIntent = stripeClient.paymentIntents().retrieve(gatewayRef,
                        PaymentIntentRetrieveParams.builder().addExpand("latest_charge").build(), null);
            } else {
                session = stripeClient.checkout().sessions().retrieve(gatewayRef,
                        SessionRetrieveParams.builder()
                                .addExpand("payment_intent")
                                .addExpand("payment_intent.latest_charge")
                                .build(), null);
                paymentIntent = session.getPaymentIntentObject();
            }

            return transformRawResponse(session, paymentIntent, currentStatus);
        } catch (StripeException e) {
            log.error("Unable to fetch status from payment gateway for txnid: " + currentStatus.getTxnId(), e);
            throw new ServiceCallException("Error occurred while fetching status from payment gateway");
        }
    }

    @Override
    public boolean isActive() {
        return ACTIVE;
    }

    @Override
    public String gatewayName() {
        return GATEWAY_NAME;
    }

    @Override
    public String transactionIdKeyInResponse() {
        // Stripe redirects back to the callback URL which already carries eg_pg_txnid
        return PgConstants.PG_TXN_IN_LABEL;
    }

    private Transaction transformRawResponse(Session session, PaymentIntent paymentIntent, Transaction currentStatus) {

        // No payment attempt yet, checkout page was never submitted
        if (Objects.isNull(paymentIntent)) {
            if (!Objects.isNull(session) && "expired".equals(session.getStatus())) {
                return Transaction.builder()
                        .txnId(currentStatus.getTxnId())
                        .txnAmount(currentStatus.getTxnAmount())
                        .txnStatus(Transaction.TxnStatusEnum.FAILURE)
                        .gatewayTxnId(session.getId())
                        .gatewayStatusCode(session.getStatus())
                        .gatewayStatusMsg("Checkout session expired without payment")
                        .responseJson(toJsonNode(session.toJson()))
                        .build();
            }
            log.info("No payment attempted yet on Stripe for txnid: {}", currentStatus.getTxnId());
            return currentStatus;
        }

        Transaction.TxnStatusEnum status;
        String stripeStatus = paymentIntent.getStatus();

        switch (stripeStatus) {
            case "succeeded":
                status = Transaction.TxnStatusEnum.SUCCESS;
                break;
            case "processing":
            case "requires_action":
            case "requires_confirmation":
            case "requires_payment_method":
                status = Transaction.TxnStatusEnum.PENDING;
                break;
            default:
                // canceled and anything unrecognized
                status = Transaction.TxnStatusEnum.FAILURE;
                break;
        }

        String statusMsg = stripeStatus;
        if (!Objects.isNull(paymentIntent.getLastPaymentError()))
            statusMsg = paymentIntent.getLastPaymentError().getMessage();

        Charge charge = paymentIntent.getLatestChargeObject();
        String paymentMode = !Objects.isNull(charge) && !Objects.isNull(charge.getPaymentMethodDetails())
                ? charge.getPaymentMethodDetails().getType() : null;

        // Amounts from Stripe are in paise, convert back to the app default rupee format
        String amount = status.equals(Transaction.TxnStatusEnum.SUCCESS)
                ? Utils.convertPaiseToRupee(String.valueOf(paymentIntent.getAmountReceived()))
                : Utils.convertPaiseToRupee(String.valueOf(paymentIntent.getAmount()));

        return Transaction.builder()
                .txnId(currentStatus.getTxnId())
                .txnAmount(amount)
                .txnStatus(status)
                .gatewayTxnId(paymentIntent.getId())
                .gatewayPaymentMode(paymentMode)
                .gatewayStatusCode(stripeStatus)
                .gatewayStatusMsg(statusMsg)
                .responseJson(toJsonNode(paymentIntent.toJson()))
                .build();
    }

    private Object toJsonNode(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            log.warn("Unable to parse Stripe response as JSON, storing raw string");
            return json;
        }
    }
}
