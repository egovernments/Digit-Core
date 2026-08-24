package org.digit.live;

import org.digit.util.DigitJson;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import tools.jackson.databind.JsonNode;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Captures the exact bytes a service returned, so a test can compare them with what the SDK parsed.
 *
 * <p>Recording the SDK's own call is what makes the comparison trustworthy. Fetching the "same"
 * endpoint a second time with a hand-written URL sounds equivalent and is not: the URL has to be
 * reconstructed by hand, and getting it subtly wrong produces a confident answer about the wrong
 * endpoint. Here there is only ever one request, and the model and the raw body come from it.
 *
 * <p>Requires the RestTemplate's factory to be wrapped in {@code BufferingClientHttpRequestFactory},
 * otherwise reading the stream here would consume it before the message converter runs.
 */
final class RawResponseRecorder implements ClientHttpRequestInterceptor {

    private static final ThreadLocal<String> LAST_BODY = new ThreadLocal<>();
    private static final ThreadLocal<String> LAST_CALL = new ThreadLocal<>();

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        ClientHttpResponse response = execution.execute(request, body);
        LAST_CALL.set(request.getMethod() + " " + request.getURI());
        byte[] bytes;
        try {
            bytes = response.getBody().readAllBytes();
        } catch (IOException e) {
            LAST_BODY.set(null);
            return response;
        }
        String raw = new String(bytes, StandardCharsets.UTF_8);
        LAST_BODY.set(raw.isBlank() ? null : raw);

        // Reading the body consumed it, so hand the converter a response that replays the same
        // bytes. Wrapping here rather than buffering at the factory is what keeps this an observer:
        // RestTemplate.getRequestFactory() already returns the intercepting wrapper, so a
        // BufferingClientHttpRequestFactory installed around it sits *outside* this interceptor and
        // buffers nothing it can see — the body still arrives consumed, and every call fails.
        return new ReplayedResponse(response, bytes);
    }

    /** The last response body as a tree, or null when the service returned nothing parseable. */
    static JsonNode lastBody() {
        String raw = LAST_BODY.get();
        if (raw == null) {
            return null;
        }
        try {
            return DigitJson.mapper().readTree(raw);
        } catch (RuntimeException e) {
            // A non-JSON body is itself worth seeing rather than hiding behind an exception.
            return null;
        }
    }

    /** Method and URI of the last call, for failure messages that name the endpoint. */
    static String lastCall() {
        return LAST_CALL.get();
    }

    static void reset() {
        LAST_BODY.remove();
        LAST_CALL.remove();
    }

    /** The original response with its already-read body replaced by a replayable copy. */
    private record ReplayedResponse(ClientHttpResponse delegate, byte[] body)
            implements ClientHttpResponse {

        @Override
        public InputStream getBody() {
            return new ByteArrayInputStream(body);
        }

        @Override
        public HttpHeaders getHeaders() {
            return delegate.getHeaders();
        }

        @Override
        public HttpStatusCode getStatusCode() throws IOException {
            return delegate.getStatusCode();
        }

        @Override
        public String getStatusText() throws IOException {
            return delegate.getStatusText();
        }

        @Override
        public void close() {
            delegate.close();
        }
    }
}
