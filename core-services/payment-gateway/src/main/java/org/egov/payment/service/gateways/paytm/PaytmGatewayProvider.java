package org.egov.payment.service.gateways.paytm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.pg.merchant.PaytmChecksum;
import lombok.extern.slf4j.Slf4j;
import org.egov.gateway.spi.GatewayException;
import org.egov.gateway.spi.GatewayProvider;
import org.egov.payment.models.Transaction;
import org.egov.payment.utils.Utils;
import org.egov.tracer.model.CustomException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Paytm gateway provider. No Spring annotations.
 */
@Slf4j
class PaytmGatewayProvider implements GatewayProvider {

    private static final String GATEWAY_ID = "paytm";

    private final String MID;
    private final String MERCHANT_KEY;
    private final String MERCHANT_URL_DEBIT;
    private final String MERCHANT_URL_STATUS;
    private final String INDUSTRY_TYPE_ID;
    private final String CHANNEL_ID;
    private final String WEBSITE;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    PaytmGatewayProvider(Map<String, String> config) {
        this.MID = config.get("paytm.merchant.id");
        this.MERCHANT_KEY = config.get("paytm.merchant.secret.key");
        this.INDUSTRY_TYPE_ID = config.get("paytm.merchant.industry.type");
        this.CHANNEL_ID = config.get("paytm.merchant.channel.id");
        this.WEBSITE = config.get("paytm.merchant.website");
        this.MERCHANT_URL_DEBIT = config.get("paytm.url.debit");
        this.MERCHANT_URL_STATUS = config.get("paytm.url.status");

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
        TreeMap<String, String> paramMap = new TreeMap<>();
        paramMap.put("MID", MID);
        paramMap.put("ORDER_ID", transaction.getTxnId());
        paramMap.put("CUST_ID", transaction.getUser().getUserName());
        paramMap.put("INDUSTRY_TYPE_ID", INDUSTRY_TYPE_ID);
        paramMap.put("CHANNEL_ID", CHANNEL_ID);
        paramMap.put("TXN_AMOUNT", Utils.formatAmtAsRupee(transaction.getTxnAmount()));
        paramMap.put("WEBSITE", WEBSITE);
        paramMap.put("EMAIL", transaction.getUser().getEmailId());
        paramMap.put("MOBILE_NO", transaction.getUser().getMobileNumber());
        paramMap.put("CALLBACK_URL", transaction.getCallbackUrl());

        try {
            String checkSum = PaytmChecksum.generateSignature(paramMap, MERCHANT_KEY);
            paramMap.put("CHECKSUMHASH", checkSum);

            String query = paramMap.entrySet().stream()
                    .map(e -> java.net.URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "=" + java.net.URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                    .collect(Collectors.joining("&"));

            return new URI(MERCHANT_URL_DEBIT + "?" + query);
        } catch (Exception e) {
            log.error("Paytm Checksum generation failed", e);
            throw new CustomException("CHECKSUM_GEN_FAILED", "Hash generation failed, gateway redirect URI cannot be generated");
        }
    }

    @Override
    public Transaction fetchStatus(Transaction currentStatus, Map<String, String> params) {
        TreeMap<String, String> treeMap = new TreeMap<>();
        treeMap.put("MID", MID);
        treeMap.put("ORDER_ID", currentStatus.getTxnId());

        try {
            String checkSum = PaytmChecksum.generateSignature(treeMap, MERCHANT_KEY);
            treeMap.put("CHECKSUMHASH", checkSum);

            String body = objectMapper.writeValueAsString(treeMap);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(MERCHANT_URL_STATUS))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            PaytmResponse paytmResponse = objectMapper.readValue(response.body(), PaytmResponse.class);
            return transformRawResponse(paytmResponse, currentStatus);

        } catch (Exception e) {
            log.error("Unable to fetch status from Paytm gateway", e);
            throw new GatewayException(GatewayException.ErrorType.CONNECTIVITY_ERROR, GATEWAY_ID,
                    "Unable to fetch status from Paytm gateway: " + e.getMessage(), e);
        }
    }

    @Override
    public String transactionIdKeyInResponse() {
        return "ORDERID";
    }

    private Transaction transformRawResponse(PaytmResponse resp, Transaction currentStatus) {
        Transaction.TxnStatusEnum status = Transaction.TxnStatusEnum.PENDING;

        if (resp.getStatus().equalsIgnoreCase("TXN_SUCCESS"))
            status = Transaction.TxnStatusEnum.SUCCESS;
        else if (resp.getStatus().equalsIgnoreCase("TXN_FAILURE"))
            status = Transaction.TxnStatusEnum.FAILURE;

        return Transaction.builder()
                .txnId(currentStatus.getTxnId())
                .txnAmount(Utils.formatAmtAsRupee(resp.getTxnAmount()))
                .txnStatus(status)
                .gatewayTxnId(resp.getTxnId())
                .gatewayPaymentMode(resp.getPaymentMode())
                .gatewayStatusCode(resp.getRespCode())
                .gatewayStatusMsg(resp.getRespMsg())
                .responseJson(resp)
                .build();
    }
}
