package org.egov.payment.service.gateways.axis;

import lombok.extern.slf4j.Slf4j;
import org.egov.gateway.spi.GatewayException;
import org.egov.gateway.spi.GatewayProvider;
import org.egov.payment.models.Transaction;
import org.egov.payment.utils.Utils;
import org.egov.tracer.model.CustomException;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import static org.egov.payment.constants.TransactionAdditionalFields.BANK_ACCOUNT_NUMBER;

/**
 * Axis Bank gateway provider. No Spring annotations.
 * HTTP calls use java.net.http.HttpClient.
 */
@Slf4j
class AxisGatewayProvider implements GatewayProvider {

    private static final String GATEWAY_ID = "axis";

    private final String MERCHANT_URL_PAY;
    private final String MERCHANT_URL_STATUS;
    private final String MERCHANT_ID;
    private final String SECURE_SECRET;
    private final String AMA_USER;
    private final String AMA_PWD;
    private final String VPC_ACCESS_CODE;
    private final String VPC_VERSION;
    private final String VPC_COMMAND_PAY;
    private final String VPC_COMMAND_STATUS;
    private final String LOCALE;
    private final String CURRENCY;

    private final HttpClient httpClient;

    AxisGatewayProvider(Map<String, String> config) {
        this.MERCHANT_URL_PAY = config.get("axis.url.debit");
        this.MERCHANT_URL_STATUS = config.get("axis.url.status");
        this.MERCHANT_ID = config.get("axis.merchant.id");
        this.SECURE_SECRET = config.get("axis.merchant.secret.key");
        this.AMA_USER = config.get("axis.merchant.user");
        this.AMA_PWD = config.get("axis.merchant.pwd");
        this.VPC_ACCESS_CODE = config.get("axis.merchant.access.code");
        this.VPC_VERSION = config.get("axis.merchant.vpc.version");
        this.VPC_COMMAND_PAY = config.get("axis.merchant.vpc.command.pay");
        this.VPC_COMMAND_STATUS = config.get("axis.merchant.vpc.command.status");
        this.LOCALE = config.get("axis.locale");
        this.CURRENCY = config.get("axis.currency");

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    @Override
    public String getGatewayId() {
        return GATEWAY_ID;
    }

    @Override
    public URI generateRedirectURI(Transaction transaction) {
        Map<String, String> fields = new HashMap<>();
        fields.put("vpc_Version", VPC_VERSION);
        fields.put("vpc_Command", VPC_COMMAND_PAY);
        fields.put("vpc_AccessCode", VPC_ACCESS_CODE);
        fields.put("vpc_Merchant", MERCHANT_ID);
        fields.put("vpc_Locale", LOCALE);
        fields.put("vpc_Currency", CURRENCY);
        fields.put("vpc_ReturnURL", transaction.getCallbackUrl());
        fields.put("vpc_MerchTxnRef", transaction.getTxnId());
        fields.put("vpc_OrderInfo", (String) transaction.getAdditionalFields().get(BANK_ACCOUNT_NUMBER));
        fields.put("vpc_Amount", String.valueOf(Utils.formatAmtAsPaise(transaction.getTxnAmount())));

        String secureHash = AxisUtils.SHAhashAllFields(fields, SECURE_SECRET);
        fields.put("vpc_SecureHash", secureHash);
        fields.put("vpc_SecureHashType", "SHA256");

        String query = fields.entrySet().stream()
                .map(e -> encodeParam(e.getKey()) + "=" + encodeParam(e.getValue()))
                .collect(Collectors.joining("&"));

        try {
            return new URI(MERCHANT_URL_PAY + "?" + query);
        } catch (Exception e) {
            log.error("Failed to build Axis redirect URI", e);
            throw new GatewayException(GatewayException.ErrorType.CONFIGURATION_ERROR, GATEWAY_ID,
                    "Failed to build redirect URI: " + e.getMessage(), e);
        }
    }

    @Override
    public Transaction fetchStatus(Transaction currentStatus, Map<String, String> params) {
        String checksum = params.get("vpc_SecureHash");
        params.remove("vpc_SecureHash");
        params.remove("vpc_SecureHashType");

        if (!StringUtils.isEmpty(checksum)) {
            if (checksum.equals(AxisUtils.SHAhashAllFields(params, SECURE_SECRET))) {
                Map<String, List<String>> resp = new HashMap<>();
                params.forEach((k, v) -> resp.put(k, Collections.singletonList(v)));
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
        return "vpc_MerchTxnRef";
    }

    private Transaction fetchStatusFromGateway(Transaction currentStatus) {
        Map<String, String> fields = new HashMap<>();

        String txnRef = StringUtils.isEmpty(currentStatus.getModule()) ? currentStatus.getTxnId() :
                currentStatus.getModule() + "-" + currentStatus.getTxnId();

        fields.put("vpc_Version", VPC_VERSION);
        fields.put("vpc_Command", VPC_COMMAND_STATUS);
        fields.put("vpc_AccessCode", VPC_ACCESS_CODE);
        fields.put("vpc_Merchant", MERCHANT_ID);
        fields.put("vpc_MerchTxnRef", txnRef);
        fields.put("vpc_User", AMA_USER);
        fields.put("vpc_Password", AMA_PWD);

        String query = fields.entrySet().stream()
                .map(e -> encodeParam(e.getKey()) + "=" + encodeParam(e.getValue()))
                .collect(Collectors.joining("&"));

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(MERCHANT_URL_STATUS + "?" + query))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("Axis status response: {}", response.body());

            Map<String, List<String>> responseParams = AxisUtils.splitQuery(response.body());
            log.info("Axis parsed response: {}", responseParams);

            return transformRawResponse(responseParams, currentStatus);
        } catch (Exception e) {
            log.error("Unable to fetch status from Axis gateway for txnid: {}", currentStatus.getTxnId(), e);
            throw new ServiceCallException("Error occurred while fetching status from Axis payment gateway");
        }
    }

    private Transaction transformRawResponse(Map<String, List<String>> resp, Transaction currentStatus) {
        Transaction.TxnStatusEnum status;

        List<String> respCodeList = resp.get("vpc_TxnResponseCode");
        if (Objects.isNull(respCodeList) || respCodeList.isEmpty()) {
            log.error("Transaction not found in the Axis payment gateway");
            return currentStatus;
        }

        String respCode = respCodeList.get(0);
        String respMsg = mapResponseCode(respCode);

        if (respCode.equalsIgnoreCase("0")) {
            status = Transaction.TxnStatusEnum.SUCCESS;
            return Transaction.builder()
                    .txnId(currentStatus.getTxnId())
                    .txnAmount(Utils.convertPaiseToRupee(resp.get("vpc_Amount").get(0)))
                    .txnStatus(status)
                    .gatewayTxnId(resp.get("vpc_TransactionNo").get(0))
                    .gatewayPaymentMode(resp.get("vpc_Card").get(0))
                    .gatewayStatusCode(respCode)
                    .gatewayStatusMsg(respMsg)
                    .responseJson(resp)
                    .build();
        } else {
            status = Transaction.TxnStatusEnum.FAILURE;
            return Transaction.builder()
                    .txnId(currentStatus.getTxnId())
                    .txnAmount(Utils.convertPaiseToRupee(resp.get("vpc_Amount").get(0)))
                    .txnStatus(status)
                    .gatewayTxnId(resp.get("vpc_TransactionNo").get(0))
                    .gatewayStatusCode(respCode)
                    .gatewayStatusMsg(respMsg)
                    .responseJson(resp)
                    .build();
        }
    }

    private String mapResponseCode(String respCode) {
        return switch (respCode) {
            case "0" -> "Transaction Successful";
            case "1" -> "Transaction Declined";
            case "2" -> "Bank Declined Transaction";
            case "3" -> "No Reply from Bank";
            case "4" -> "Expired Card";
            case "5" -> "Insufficient Funds";
            case "6" -> "Error Communicating with Bank";
            case "7" -> "Payment Server detected an error";
            case "8" -> "Transaction Type Not Supported";
            case "9" -> "Bank declined transaction (Do not contact Bank)";
            case "A" -> "Transaction Aborted";
            case "B" -> "Transaction Declined - Contact the Bank";
            case "C" -> "Transaction Cancelled";
            case "D" -> "Deferred transaction has been received and is awaiting processing";
            case "E" -> "Transaction Declined - Refer to card issuer";
            case "F" -> "3-D Secure Authentication failed";
            case "I" -> "Card Security Code verification failed";
            case "L" -> "Shopping Transaction Locked (Please try the transaction again later)";
            case "M" -> "Transaction Submitted (No response from acquirer)";
            case "N" -> "Cardholder is not enrolled in Authentication scheme";
            case "P" -> "Transaction has been received by the Payment Adaptor and is being processed";
            case "R" -> "Transaction was not processed - Reached limit of retry attempts allowed";
            case "S" -> "Duplicate SessionID";
            case "T" -> "Address Verification Failed";
            case "U" -> "Card Security Code Failed";
            case "V" -> "Address Verification and Card Security Code Failed";
            case "?" -> "Transaction status is unknown";
            default -> "Unable to be determined";
        };
    }

    private static String encodeParam(String value) {
        try {
            return URLEncoder.encode(value != null ? value : "", StandardCharsets.UTF_8);
        } catch (Exception e) {
            return value != null ? value : "";
        }
    }
}
