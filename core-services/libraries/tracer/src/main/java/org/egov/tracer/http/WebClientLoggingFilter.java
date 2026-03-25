package org.egov.tracer.http;

import static org.egov.tracer.constants.TracerConstants.CORRELATION_ID_HEADER;
import static org.egov.tracer.constants.TracerConstants.CORRELATION_ID_MDC;
import static org.egov.tracer.constants.TracerConstants.TENANTID_MDC;
import static org.egov.tracer.constants.TracerConstants.TENANT_ID_HEADER;

import org.egov.tracer.config.TracerProperties;
import org.slf4j.MDC;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class WebClientLoggingFilter implements ExchangeFilterFunction {

    private final TracerProperties tracerProperties;

    public WebClientLoggingFilter(TracerProperties tracerProperties) {
        this.tracerProperties = tracerProperties;
    }

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        // Capture MDC values from the calling thread (safe with virtual threads)
        String correlationId = MDC.get(CORRELATION_ID_MDC);
        String tenantId = MDC.get(TENANTID_MDC);

        ClientRequest.Builder requestBuilder = ClientRequest.from(request);
        if (correlationId != null) {
            requestBuilder.header(CORRELATION_ID_HEADER, correlationId);
        }
        if (tenantId != null && !request.headers().containsKey(TENANT_ID_HEADER)) {
            requestBuilder.header(TENANT_ID_HEADER, tenantId);
        }
        ClientRequest enrichedRequest = requestBuilder.build();

        if (tracerProperties.isRestTemplateDetailedLoggingEnabled()) {
            log.info("Sending request to {} with verb {}", enrichedRequest.url(), enrichedRequest.method());
        }

        return next.exchange(enrichedRequest)
                .doOnNext(response -> {
                    if (tracerProperties.isRestTemplateDetailedLoggingEnabled()) {
                        log.info("Received from {} response code {}",
                                enrichedRequest.url(), response.statusCode());
                    } else {
                        log.info("Received response from {}", enrichedRequest.url());
                    }
                })
                .doOnError(e -> log.warn("Received error response from {}", enrichedRequest.url(), e));
    }
}
