package org.egov.keycloak.auth.clients.otp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.keycloak.auth.clients.otp.models.*;
import org.egov.keycloak.auth.config.OtpConfig;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;

public class OtpClientImpl implements OtpClient {

    private static final Logger log = Logger.getLogger(OtpClientImpl.class);

    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final OtpConfig otpConfig;
    /** Per-request response timeout. The connect timeout is set on the shared {@link HttpClient}. */
    private final Duration requestTimeout;

    public OtpClientImpl(OtpConfig otpConfig,
                         HttpClient httpClient,
                         ObjectMapper objectMapper) {
        this.otpConfig      = otpConfig;
        this.baseUrl        = otpConfig.getOtpHost();
        this.httpClient     = httpClient;
        this.objectMapper   = objectMapper;
        this.requestTimeout = Duration.ofMillis(otpConfig.getRequestTimeoutMs());
    }

    @Override
    public GenerateOtpResponse generate(String tenantId, GenerateOtpRequest request) {
        return post(tenantId, baseUrl + otpConfig.getOtpGeneratePath(),
                request, GenerateOtpResponse.class);
    }

    @Override
    public ResendOtpResponse resend(String tenantId, ResendOtpRequest request) {
        return post(tenantId, baseUrl + otpConfig.getOtpResendPath(),
                request, ResendOtpResponse.class);
    }

    @Override
    public VerifyOtpResponse verify(String tenantId, VerifyOtpRequest request) {
        return post(tenantId, baseUrl + otpConfig.getOtpVerifyPath(),
                request, VerifyOtpResponse.class);
    }

    @Override
    public InvalidateOtpResponse invalidate(String tenantId, InvalidateOtpRequest request) {
        return post(tenantId, baseUrl + otpConfig.getOtpInvalidatePath(),
                request, InvalidateOtpResponse.class);
    }

    private <T> T post(String tenantId, String url, Object body, Class<T> responseType) {
        try {
            String json = objectMapper.writeValueAsString(body);
            log.debugf("POST %s tenantId=%s body=%s", url, tenantId, json);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(requestTimeout)
                    .header("Content-Type", "application/json")
                    .header("X-Tenant-Id", tenantId)
                    .header("X-Request-Id", UUID.randomUUID().toString())
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response =
                    httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            String responseBody = response.body();
            log.debugf("Response from %s: status=%d body=%s", url, status, responseBody);

            if (status >= 200 && status < 300) {
                return objectMapper.readValue(responseBody, responseType);
            }

            // The OTP service returns errors as a JSON array: [{"code": ...}]
            ErrorResponse err = null;
            try {
                ErrorResponse[] errors = objectMapper.readValue(responseBody, ErrorResponse[].class);
                if (errors.length > 0) {
                    err = errors[0];
                }
            } catch (Exception ignored) {}
            throw new OtpClientException(status,
                    "OTP service returned HTTP " + status, err);

        } catch (OtpClientException e) {
            throw e;
        } catch (IOException e) {
            throw new OtpClientException(503, "Error calling OTP service: " + e.getMessage(), null);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OtpClientException(503, "Interrupted calling OTP service: " + e.getMessage(), null);
        }
    }
}
