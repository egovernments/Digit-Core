package org.digit.live;

import org.digit.config.ApiProperties;
import org.digit.config.HeaderPropagationAutoConfiguration;
import org.digit.config.PropagationProperties;
import org.digit.util.DigitContextHolder;
import org.digit.util.DigitRequestContext;
import org.digit.util.JwtTokenUtil;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Wires the SDK's clients against the real services for the live suite.
 *
 * <p>Two modes, because they prove different things:
 *
 * <ul>
 *   <li>{@code pod} (default) — one localhost port per service via {@code e2e/port-forward.sh}.
 *       Isolates the SDK's own request and response contracts: nothing sits between the client and
 *       the service, so a failure is in one of those two and never in a proxy.
 *   <li>{@code gateway} — every client points at the same origin and Kong routes by context path.
 *       This is the only mode that exercises {@code Authorization} propagation and the JWT decode in
 *       {@code JwtTokenUtil}, and it is a live check that the SDK's hardcoded context paths match
 *       what the platform actually routes.
 * </ul>
 *
 * <p>The RestTemplate comes from {@link HeaderPropagationAutoConfiguration} rather than being
 * assembled here, so the suite exercises the real wiring — the converter, the error handler and the
 * propagation interceptor — instead of a copy of it that could drift.
 *
 * <p>Configuration is read from the environment, never from a committed file, so no token is ever
 * stored in the repository. {@code e2e/.env.local} supplies it and is gitignored.
 */
final class LiveEnv {

    enum Mode { POD, GATEWAY }

    /** Topology lives in e2e/services.properties so the tunnel and the client cannot disagree. */
    private static final Properties TOPOLOGY = loadTopology();

    private static final Mode MODE = Mode.valueOf(
            setting("digit.live.mode", "DIGIT_LIVE_MODE", "pod").toUpperCase());

    static final String TENANT_ID = setting("digit.live.tenant", "DIGIT_TENANT_ID", "TEST3");
    static final String USER_ID = setting("digit.live.userId", "DIGIT_USER_ID", null);
    static final String TOKEN = setting("digit.live.token", "DIGIT_TOKEN", null);
    static final String GATEWAY_URL = setting("digit.live.gateway", "DIGIT_GATEWAY_URL", null);

    private static final ApiProperties PROPERTIES = buildProperties();
    private static final RestTemplate REST_TEMPLATE = buildRestTemplate();

    private static final HttpClient PROBE = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    private LiveEnv() {
    }

    static Mode mode() {
        return MODE;
    }

    static ApiProperties properties() {
        return PROPERTIES;
    }

    static RestTemplate restTemplate() {
        return REST_TEMPLATE;
    }

    /**
     * Installs the tenant, user and token the clients will send.
     *
     * <p>Uses the SDK's own {@code DigitContextHolder} rather than stubbing headers, because outside
     * a servlet request that holder is the only thing that can supply them — which makes this the
     * live test of the non-servlet propagation path a batch job or scheduled task depends on.
     */
    static void installContext() {
        DigitRequestContext.Builder context = DigitRequestContext.builder()
                .tenantId(TENANT_ID)
                .userId(USER_ID)
                .authToken(TOKEN);
        if (TOKEN != null) {
            // MdmsClient refuses to call without a client id, and in a servlet request the SDK derives
            // it from the token rather than requiring the caller to supply one. Deriving it the same
            // way here keeps the two paths identical, and gives JwtTokenUtil its only live coverage.
            context.clientId(JwtTokenUtil.extractClientId(TOKEN));
        }
        DigitContextHolder.set(context.build());
    }

    static void clearContext() {
        DigitContextHolder.clear();
    }

    /** The base URL a client should be given for {@code service}, per the active mode. */
    static String baseUrl(String service) {
        if (MODE == Mode.GATEWAY) {
            if (GATEWAY_URL == null) {
                throw new IllegalStateException(
                        "gateway mode needs DIGIT_GATEWAY_URL; source e2e/.env.local first");
            }
            return GATEWAY_URL;
        }
        return "http://localhost:" + required(service + ".port");
    }

    /**
     * Whether {@code service} answers on its health endpoint right now.
     *
     * <p>Used to skip rather than fail: a service that is down or a tunnel that was never opened is
     * not an SDK defect, and turning it into a red test would bury the failures that are.
     */
    static boolean reachable(String service) {
        String url = baseUrl(service) + required(service + ".context") + "/actuator/health";
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(4))
                    .GET()
                    .build();
            PROBE.send(request, HttpResponse.BodyHandlers.discarding());
            // Any status counts, deliberately. notification answers 500 on its health endpoint while
            // its API serves fine, so gating on 2xx would skip a service that works. What this needs
            // to distinguish is "answering at all" from "nothing listening", and only an IOException
            // tells us the latter.
            return true;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    // ── internals ────────────────────────────────────────────────────────────

    private static ApiProperties buildProperties() {
        ApiProperties properties = new ApiProperties();
        properties.setAccountServiceUrl(baseUrl("account"));
        properties.setBillingServiceUrl(baseUrl("billing"));
        properties.setBoundaryServiceUrl(baseUrl("boundary"));
        properties.setEmployeeServiceUrl(baseUrl("employee"));
        properties.setFilestoreServiceUrl(baseUrl("filestore"));
        properties.setIdgenServiceUrl(baseUrl("idgen"));
        properties.setIndividualServiceUrl(baseUrl("individual"));
        properties.setMdmsServiceUrl(baseUrl("mdms"));
        properties.setNotificationServiceUrl(baseUrl("notification"));
        properties.setOtpServiceUrl(baseUrl("otp"));
        properties.setRegistryServiceUrl(baseUrl("registry"));
        properties.setWorkflowServiceUrl(baseUrl("workflow"));

        // Must be set explicitly: the @Value defaults only apply when Spring instantiates this, and
        // the request factory rejects a zero connect timeout.
        properties.setConnectTimeout(5000);
        properties.setReadTimeout(30000);
        return properties;
    }

    private static RestTemplate buildRestTemplate() {
        HeaderPropagationAutoConfiguration autoConfiguration = new HeaderPropagationAutoConfiguration();
        ClientHttpRequestInterceptor interceptor =
                autoConfiguration.headerPropagationInterceptor(new PropagationProperties());
        RestTemplate restTemplate = autoConfiguration.digitRestTemplate(PROPERTIES, interceptor);

        // The recorder replays the body it reads, so no buffering factory is needed — and one
        // installed here would sit outside the interceptor chain and break every call. Adding the
        // recorder is otherwise passive: same URL, same headers as the auto-configured template.
        List<ClientHttpRequestInterceptor> interceptors =
                new ArrayList<>(restTemplate.getInterceptors());
        interceptors.add(new RawResponseRecorder());
        restTemplate.setInterceptors(interceptors);
        return restTemplate;
    }

    private static Properties loadTopology() {
        // Surefire runs with the module directory as the working directory.
        Path path = Path.of("e2e", "services.properties");
        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            properties.load(in);
        } catch (IOException e) {
            throw new UncheckedIOException("cannot read " + path.toAbsolutePath(), e);
        }
        return properties;
    }

    private static String required(String key) {
        String value = TOPOLOGY.getProperty(key);
        if (value == null) {
            throw new IllegalStateException("e2e/services.properties has no " + key);
        }
        return value;
    }

    /** System property wins over environment variable, so a single run can be overridden inline. */
    private static String setting(String systemProperty, String environmentVariable, String fallback) {
        String value = System.getProperty(systemProperty);
        if (value == null || value.isBlank()) {
            value = System.getenv(environmentVariable);
        }
        return value == null || value.isBlank() ? fallback : value;
    }
}
