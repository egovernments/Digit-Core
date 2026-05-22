package org.egov.payment.service.gateways.payu;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.gateway.spi.GatewayProvider;
import org.egov.payment.models.Transaction;
import org.egov.payment.utils.Utils;
import org.egov.tracer.model.CustomException;
import org.egov.tracer.model.ServiceCallException;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

/**
 * PayU gateway provider. No Spring annotations.
 */
@Slf4j
class PayuGatewayProvider implements GatewayProvider {

    private static final String GATEWAY_ID = "payu";

    private final String MERCHANT_KEY;
    private final String MERCHANT_SALT;
    private final String MERCHANT_URL_PAY;
    private final String MERCHANT_URL_STATUS;
    private final String MERCHANT_PATH_PAY;
    private final String MERCHANT_PATH_STATUS;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    PayuGatewayProvider(Map<String, String> config) {
        this.MERCHANT_KEY = config.get("payu.merchant.key");
        this.MERCHANT_SALT = config.get("payu.merchant.salt");
        this.MERCHANT_URL_PAY = config.get("payu.url");
        this.MERCHANT_URL_STATUS = config.get("payu.url.status");
        this.MERCHANT_PATH_PAY = config.get("payu.path.pay");
        this.MERCHANT_PATH_STATUS = config.get("payu.path.status");

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
        String hashSequence = "key|txnid|amount|productinfo|firstname|email|||||||||||";
        hashSequence = hashSequence.concat(MERCHANT_SALT);
        hashSequence = hashSequence.replace("key", MERCHANT_KEY);
        hashSequence = hashSequence.replace("txnid", transaction.getTxnId());
        hashSequence = hashSequence.replace("amount", Utils.formatAmtAsRupee(transaction.getTxnAmount()));
        hashSequence = hashSequence.replace("productinfo", transaction.getProductInfo());
        hashSequence = hashSequence.replace("firstname", transaction.getUser().getName());
        hashSequence = hashSequence.replace("email", Objects.toString(transaction.getUser().getEmailId(), ""));

        String hash = hashCal(hashSequence);

        String formBody = buildFormBody(Map.of(
                "key", MERCHANT_KEY,
                "txnid", transaction.getTxnId(),
                "amount", Utils.formatAmtAsRupee(transaction.getTxnAmount()),
                "productinfo", transaction.getProductInfo(),
                "firstname", transaction.getUser().getName(),
                "email", Objects.toString(transaction.getUser().getEmailId(), ""),
                "phone", transaction.getUser().getMobileNumber(),
                "surl", transaction.getCallbackUrl(),
                "furl", transaction.getCallbackUrl(),
                "hash", hash
        ));

        try {
            String uri = "https://" + MERCHANT_URL_PAY + MERCHANT_PATH_PAY;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(uri))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());

            java.util.Optional<String> location = response.headers().firstValue("Location");
            if (location.isPresent()) {
                return new URI(location.get());
            }

            throw new CustomException("PAYU_REDIRECT_URI_GEN_FAILED", "Failed to generate redirect URI from PayU");
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unable to retrieve redirect URI from PayU gateway", e);
            throw new ServiceCallException("Redirect URI generation failed, invalid response received from PayU gateway");
        }
    }

    @Override
    public Transaction fetchStatus(Transaction currentStatus, Map<String, String> params) {
        PayuResponse resp = objectMapper.convertValue(params, PayuResponse.class);

        if (!isNull(resp.getHash()) && !isNull(resp.getStatus()) && !isNull(resp.getTxnid())
                && !isNull(resp.getAmount()) && !isNull(resp.getProductinfo()) && !isNull(resp.getFirstname())) {
            resp.setTransaction_amount(resp.getAmount());
            String checksum = resp.getHash();

            String hashSequence = "SALT|status||||||udf5|udf4|udf3|udf2|udf1|email|firstname|productinfo|amount|txnid|";
            hashSequence = hashSequence.concat(MERCHANT_KEY);
            hashSequence = hashSequence.replace("SALT", MERCHANT_SALT);
            hashSequence = hashSequence.replace("status", resp.getStatus());
            hashSequence = hashSequence.replace("udf5", nullSafe(resp.getUdf5()));
            hashSequence = hashSequence.replace("udf4", nullSafe(resp.getUdf4()));
            hashSequence = hashSequence.replace("udf3", nullSafe(resp.getUdf3()));
            hashSequence = hashSequence.replace("udf2", nullSafe(resp.getUdf2()));
            hashSequence = hashSequence.replace("udf1", nullSafe(resp.getUdf1()));
            hashSequence = hashSequence.replace("email", nullSafe(resp.getEmail()));
            hashSequence = hashSequence.replace("firstname", nullSafe(resp.getFirstname()));
            hashSequence = hashSequence.replace("productinfo", nullSafe(resp.getProductinfo()));
            hashSequence = hashSequence.replace("amount", nullSafe(resp.getTransaction_amount()));
            hashSequence = hashSequence.replace("txnid", nullSafe(resp.getTxnid()));
            String hash = hashCal(hashSequence);

            if (checksum.equalsIgnoreCase(hash)) {
                Transaction txn = transformRawResponse(resp, currentStatus);
                if (txn.getTxnStatus().equals(Transaction.TxnStatusEnum.PENDING) || txn.getTxnStatus().equals(Transaction.TxnStatusEnum.FAILURE)) {
                    return txn;
                }
            }
        }

        return fetchStatusFromGateway(currentStatus);
    }

    @Override
    public String transactionIdKeyInResponse() {
        return "txnid";
    }

    private Transaction transformRawResponse(PayuResponse resp, Transaction currentStatus) {
        Transaction.TxnStatusEnum status;
        String gatewayStatus = resp.getStatus();

        if (gatewayStatus.equalsIgnoreCase("success")) {
            status = Transaction.TxnStatusEnum.SUCCESS;
            return Transaction.builder()
                    .txnId(currentStatus.getTxnId())
                    .txnAmount(resp.getTransaction_amount())
                    .txnStatus(status)
                    .gatewayTxnId(resp.getMihpayid())
                    .gatewayPaymentMode(resp.getMode())
                    .gatewayStatusCode(resp.getUnmappedstatus())
                    .gatewayStatusMsg(resp.getStatus())
                    .responseJson(resp)
                    .build();
        } else {
            status = Transaction.TxnStatusEnum.FAILURE;
            return Transaction.builder()
                    .txnId(currentStatus.getTxnId())
                    .txnAmount(resp.getTransaction_amount())
                    .txnStatus(status)
                    .gatewayTxnId(resp.getMihpayid())
                    .gatewayStatusCode(resp.getError_code())
                    .gatewayStatusMsg(resp.getError_Message())
                    .responseJson(resp)
                    .build();
        }
    }

    private Transaction fetchStatusFromGateway(Transaction currentStatus) {
        String txnRef = currentStatus.getTxnId();
        String hash = hashCal(MERCHANT_KEY + "|" + "verify_payment" + "|" + txnRef + "|" + MERCHANT_SALT);

        String formBody = buildFormBody(Map.of(
                "key", MERCHANT_KEY,
                "command", "verify_payment",
                "hash", hash,
                "var1", txnRef
        ));

        try {
            String uri = "https://" + MERCHANT_URL_STATUS + MERCHANT_PATH_STATUS + "?form=2";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(uri))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("PayU status response: {}", response.body());

            JsonNode payuRawResponse = objectMapper.readTree(response.body());
            JsonNode status = payuRawResponse.path("transaction_details").path(txnRef);

            if (status.isNull() || status.isMissingNode()) {
                throw new CustomException("FAILED_TO_FETCH_STATUS_FROM_GATEWAY",
                        "Unable to fetch status from PayU gateway for txnid: " + currentStatus.getTxnId());
            }

            PayuResponse payuResponse = objectMapper.treeToValue(status, PayuResponse.class);
            return transformRawResponse(payuResponse, currentStatus);

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unable to fetch status from PayU gateway for txnid: {}", currentStatus.getTxnId(), e);
            throw new ServiceCallException("Error occurred while fetching status from PayU payment gateway");
        }
    }

    private String hashCal(String str) {
        byte[] hashSequence = str.getBytes(StandardCharsets.UTF_8);
        StringBuilder hexString = new StringBuilder();
        try {
            MessageDigest algorithm = MessageDigest.getInstance("SHA-512");
            algorithm.reset();
            algorithm.update(hashSequence);
            byte[] messageDigest = algorithm.digest();

            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xFF & b);
                if (hex.length() == 1) hexString.append("0");
                hexString.append(hex);
            }
        } catch (NoSuchAlgorithmException nsae) {
            log.error("Error occurred while generating hash", nsae);
            throw new CustomException("CHECKSUM_GEN_FAILED", "Hash generation failed, gateway redirect URI cannot be generated");
        }
        return hexString.toString();
    }

    private String buildFormBody(Map<String, String> params) {
        return params.entrySet().stream()
                .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }

    private String nullSafe(String value) {
        return value != null ? value : "";
    }
}
