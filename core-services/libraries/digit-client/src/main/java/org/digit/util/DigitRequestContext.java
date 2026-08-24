package org.digit.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The identity and correlation a DIGIT call needs, supplied explicitly by the caller.
 *
 * <p>Header propagation normally lifts these off the inbound servlet request, which works for a
 * service handling an HTTP call and not at all for anything else — a Kafka consumer, an
 * {@code @Async} method, a scheduled job, or tenant-onboarding code acting as a freshly created
 * tenant's own user rather than as the ambient caller. Supplying one of these via
 * {@link DigitContextHolder} makes those paths work without every client method growing parameters.
 *
 * <p>Header names match what the services read: tenant and user are required by most write routes,
 * and registry requires both on reads too. {@code clientId} is only consumed by mdms.
 */
public final class DigitRequestContext {

    private final String tenantId;
    private final String userId;
    private final String clientId;
    private final String authToken;
    private final String correlationId;
    private final String requestId;

    private DigitRequestContext(Builder builder) {
        this.tenantId = builder.tenantId;
        this.userId = builder.userId;
        this.clientId = builder.clientId;
        this.authToken = builder.authToken;
        this.correlationId = builder.correlationId;
        this.requestId = builder.requestId;
    }

    public static Builder builder() {
        return new Builder();
    }

    /** Shorthand for the common case: acting as a known user in a known tenant. */
    public static DigitRequestContext of(String tenantId, String userId, String authToken) {
        return builder().tenantId(tenantId).userId(userId).authToken(authToken).build();
    }

    public String getTenantId() {
        return this.tenantId;
    }

    public String getUserId() {
        return this.userId;
    }

    public String getClientId() {
        return this.clientId;
    }

    public String getAuthToken() {
        return this.authToken;
    }

    public String getCorrelationId() {
        return this.correlationId;
    }

    public String getRequestId() {
        return this.requestId;
    }

    /**
     * Renders the context as outbound headers. Only the values that were set appear, so a context
     * carrying just a tenant does not blank out an ambient user id.
     *
     * <p>{@code authToken} is emitted as {@code Authorization}, with the {@code Bearer } prefix
     * added only when the caller has not already included one.
     */
    public Map<String, String> toHeaders() {
        Map<String, String> headers = new LinkedHashMap<>();
        putIfPresent(headers, "X-Tenant-ID", this.tenantId);
        putIfPresent(headers, "X-User-ID", this.userId);
        putIfPresent(headers, "X-Client-ID", this.clientId);
        putIfPresent(headers, "X-Correlation-ID", this.correlationId);
        putIfPresent(headers, "X-Request-ID", this.requestId);
        if (this.authToken != null && !this.authToken.isBlank()) {
            String value = this.authToken.startsWith("Bearer ") ? this.authToken : "Bearer " + this.authToken;
            headers.put("Authorization", value);
        }
        return headers;
    }

    private static void putIfPresent(Map<String, String> headers, String name, String value) {
        if (value != null && !value.isBlank()) {
            headers.put(name, value);
        }
    }

    public static final class Builder {
        private String tenantId;
        private String userId;
        private String clientId;
        private String authToken;
        private String correlationId;
        private String requestId;

        public Builder tenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder authToken(String authToken) {
            this.authToken = authToken;
            return this;
        }

        public Builder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public DigitRequestContext build() {
            return new DigitRequestContext(this);
        }
    }
}
