package org.egov.payment.service.gateways.phonepe;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.gateway.spi.GatewayException;
import org.egov.gateway.spi.GatewayProvider;
import org.egov.payment.models.Transaction;
import org.egov.payment.utils.Utils;
import org.egov.tracer.model.CustomException;
import org.egov.tracer.model.ServiceCallException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * PhonePe gateway provider. No Spring annotations.
 * Uses Java 17 HttpClient and HexFormat (replaces DatatypeConverter).
 */
@Slf4j
class PhonepeGatewayProvider implements GatewayProvider {

    private static final String GATEWAY_ID = "phonepe";

    private final String MERCHANT_HOST;
    private final String MERCHANT_PATH_DEBIT;
    private final String MERCHANT_PATH_STATUS;
    private final String MERCHANT_ID;
    private final String SALT;
    private final String SALT_INDEX;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    PhonepeGatewayProvider(Map<String, String> config) {
        this.MERCHANT_ID = config.get("phonepe.merchant.id");
        this.SALT = config.get("phonepe.merchant.secret.key");
        this.SALT_INDEX = config.get("phonepe.merchant.secret.index");
        this.MERCHANT_HOST = config.get("phonepe.merchant.host");
        this.MERCHANT_PATH_DEBIT = config.get("phonepe.url.debit");
        this.MERCHANT_PATH_STATUS = config.get("phonepe.url.status");

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getGatewayId() {
        return GATEWAY_ID;
    }

    @Override
    public URI generateRedirectURI(Transaction transaction) {
        Map<String, Object> map = new TreeMap<>();
        map.put("merchantId", MERCHANT_ID);
        map.put("transactionId", transaction.getTxnId());
        map.put("merchantUserId", transaction.getUser().getUserName());
        map.put("amount", Long.valueOf(Utils.formatAmtAsPaise(transaction.getTxnAmount())));
        map.put("merchantOrderId", transaction.getBillId());
        map.put("mobileNumber", transaction.getUser().getMobileNumber());
        map.put("message", transaction.getProductInfo());

        try {
            String jsonPayload = objectMapper.writeValueAsString(map);
            String encodedPayload = Base64.getEncoder().encodeToString(jsonPayload.getBytes(StandardCharsets.UTF_8));
            String hashInput = encodedPayload + MERCHANT_PATH_DEBIT + SALT;
            String xVerify = PhonepeUtils.buildHash(hashInput) + "###" + SALT_INDEX;

            String uri = "https://" + MERCHANT_HOST + MERCHANT_PATH_DEBIT;

            String requestBody = objectMapper.writeValueAsString(Collections.singletonMap("request", encodedPayload));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(uri))
                    .header("Content-Type", "application/json")
                    .header("X-VERIFY", xVerify)
                    .header("X-REDIRECT-URL", transaction.getCallbackUrl())
                    .header("X-REDIRECT-MODE", "POST")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());

            // PhonePe returns a 302 redirect — follow the Location header
            java.util.Optional<String> location = response.headers().firstValue("Location");
            if (location.isPresent()) {
                URI redirectUri = new URI(location.get());
                return new URI("https", MERCHANT_HOST, redirectUri.getPath(), redirectUri.getQuery(), null);
            }

            // If we followed redirects, return the final URI
            throw new GatewayException(GatewayException.ErrorType.INVALID_RESPONSE, GATEWAY_ID,
                    "No redirect location received from PhonePe");

        } catch (GatewayException e) {
            throw e;
        } catch (Exception e) {
            log.error("PhonePe generateRedirectURI failed", e);
            throw new CustomException("CHECKSUM_GEN_FAILED", "Hash generation failed, gateway redirect URI cannot be generated");
        }
    }

    @Override
    public Transaction fetchStatus(Transaction currentStatus, Map<String, String> params) {
        String path = MERCHANT_PATH_STATUS + "/" + MERCHANT_ID + "/" + currentStatus.getTxnId() + "/status";

        try {
            String xVerify = PhonepeUtils.buildHash(path + SALT) + "###" + SALT_INDEX;

            String uri = "https://" + MERCHANT_HOST + path;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(uri))
                    .header("Content-Type", "application/json")
                    .header("X-VERIFY", xVerify)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("PhonePe status response: {}", response.body());

            PhonepeResponse resp = objectMapper.readValue(response.body(), PhonepeResponse.class);
            return transformRawResponse(resp, currentStatus);

        } catch (Exception e) {
            log.error("Unable to fetch status from PhonePe gateway for txnid: {}", currentStatus.getTxnId(), e);
            throw new ServiceCallException("Error occurred while fetching status from PhonePe payment gateway");
        }
    }

    @Override
    public String transactionIdKeyInResponse() {
        return "transactionId";
    }

    private Transaction transformRawResponse(PhonepeResponse resp, Transaction currentStatus) {
        Transaction.TxnStatusEnum status;

        if (Boolean.TRUE.equals(resp.getSuccess())) {
            status = Transaction.TxnStatusEnum.SUCCESS;
            return Transaction.builder()
                    .txnId(currentStatus.getTxnId())
                    .txnAmount(Utils.convertPaiseToRupee(resp.getAmount()))
                    .txnStatus(status)
                    .gatewayTxnId(resp.getProviderReferenceId())
                    .gatewayStatusCode(resp.getCode())
                    .gatewayStatusMsg(resp.getMessage())
                    .responseJson(resp)
                    .build();
        } else {
            if (resp.getCode() != null && resp.getCode().equalsIgnoreCase("PAYMENT_PENDING"))
                status = Transaction.TxnStatusEnum.PENDING;
            else
                status = Transaction.TxnStatusEnum.FAILURE;
            return Transaction.builder()
                    .txnId(currentStatus.getTxnId())
                    .txnStatus(status)
                    .gatewayTxnId(resp.getProviderReferenceId())
                    .gatewayStatusCode(resp.getCode())
                    .gatewayStatusMsg(resp.getMessage())
                    .responseJson(resp)
                    .build();
        }
    }
}
