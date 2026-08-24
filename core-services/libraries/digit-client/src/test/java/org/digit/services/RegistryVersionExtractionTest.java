package org.digit.services;

import org.digit.config.ApiProperties;
import org.digit.exception.DigitClientException;
import org.digit.services.registry.RegistryClient;
import org.digit.services.registry.model.RegistryData;
import org.digit.services.registry.model.RegistryDataResponse;
import org.digit.util.DigitJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pins the update path's read of {@code registryId} and {@code version} out of an untyped payload.
 *
 * <p>Worth its own test because those two values are not just sent back to the service — they also
 * form the Redis cache key, so reading the wrong one is a cross-tenant cache collision rather than a
 * visible failure. The client is built without a {@code RedisTemplate} here, which disables the cache
 * and forces every update through the search-then-extract path under test.
 */
@ExtendWith(MockitoExtension.class)
class RegistryVersionExtractionTest {

    private static final String BASE = "http://localhost:8085";

    @Mock RestTemplate restTemplate;

    RegistryClient client;

    @BeforeEach
    void setup() {
        ApiProperties props = new ApiProperties();
        props.setRegistryServiceUrl(BASE);
        client = new RegistryClient(restTemplate, props);
    }

    @Test
    void readsTheVersionAndIdFromAListPayload() {
        // What a search actually answers with: the record wrapped in a list.
        stubSearch("""
                {"data":[{"registryId":"7f2a0d16-6d1b-4e57-8f43-2a4b9c5e1d90","version":3,
                          "data":{"licenceNumber":"TL-1"}}]}
                """);
        stubUpdate();

        client.updateRegistryData("Trade.License", payload(), "licenceNumber", "TL-1");

        assertEquals(BASE + "/registry/v3/Trade.License/data?id=7f2a0d16-6d1b-4e57-8f43-2a4b9c5e1d90",
                capturedUpdateUrl());
        assertEquals(3, sentVersion());
    }

    @Test
    void readsTheVersionAndIdFromASingleObjectPayload() {
        // A write answers with the bare record on the same envelope, so both shapes must work.
        stubSearch("""
                {"data":{"registryId":"1c8e5f34-91aa-4b0e-93d2-6d7c8f0a2b45","version":1}}
                """);
        stubUpdate();

        client.updateRegistryData("Trade.License", payload(), "licenceNumber", "TL-1");

        assertTrue(capturedUpdateUrl().endsWith("?id=1c8e5f34-91aa-4b0e-93d2-6d7c8f0a2b45"),
                capturedUpdateUrl());
        assertEquals(1, sentVersion());
    }

    @Test
    void narrowsAVersionThatArrivesTooWideToBeAnInt() {
        // data is untyped, so Jackson binds a large JSON integer to Long, not Integer. Reading it as
        // Integer would leave the version null and fail an update that should have succeeded.
        stubSearch("""
                {"data":[{"registryId":"r-1","version":3000000000}]}
                """);
        stubUpdate();

        client.updateRegistryData("Trade.License", payload(), "licenceNumber", "TL-1");

        assertEquals((int) 3000000000L, sentVersion());
    }

    @Test
    void stringifiesAnIdThatIsNotAString() {
        stubSearch("""
                {"data":[{"registryId":42,"version":1}]}
                """);
        stubUpdate();

        client.updateRegistryData("Trade.License", payload(), "licenceNumber", "TL-1");

        assertTrue(capturedUpdateUrl().endsWith("?id=42"), capturedUpdateUrl());
    }

    @Test
    void refusesToGuessANonNumericVersion() {
        // Deliberate: coercing "3" here would send an optimistic-lock version the service either
        // rejects or, worse, accepts against a revision we never read.
        stubSearch("""
                {"data":[{"registryId":"r-1","version":"3"}]}
                """);

        DigitClientException thrown = assertThrows(DigitClientException.class,
                () -> client.updateRegistryData("Trade.License", payload(), "licenceNumber", "TL-1"));
        assertEquals("Could not extract version from existing registry data", thrown.getMessage());
    }

    @Test
    void treatsAnEmptyResultAsNothingToUpdate() {
        stubSearch("""
                {"data":[]}
                """);

        assertThrows(DigitClientException.class,
                () -> client.updateRegistryData("Trade.License", payload(), "licenceNumber", "TL-1"));
    }

    // ── the shared wrap-or-rethrow the catch blocks now call ──────────────────

    @Test
    void wrapReturnsAnExistingClientExceptionUntouched() {
        // Re-wrapping would replace the service's own status, code and raw body with a generic 500.
        DigitClientException original = new DigitClientException(
                "not found", HttpStatus.NOT_FOUND, "REGISTRY_NOT_FOUND", "[{\"code\":\"NOT_FOUND\"}]");

        DigitClientException result = DigitClientException.wrap("Failed to update registry data", original);

        assertSame(original, result);
        assertEquals("not found", result.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, result.getHttpStatus());
        assertEquals("REGISTRY_NOT_FOUND", result.getErrorCode());
        assertEquals("[{\"code\":\"NOT_FOUND\"}]", result.getResponseBody());
    }

    @Test
    void wrapPrefixesAnythingElseAndKeepsTheCause() {
        IllegalStateException cause = new IllegalStateException("boom");

        DigitClientException result = DigitClientException.wrap("Failed to update registry data", cause);

        assertEquals("Failed to update registry data: boom", result.getMessage());
        assertSame(cause, result.getCause());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getHttpStatus());
    }

    @Test
    void nestedClientCallsDoNotStackPrefixes() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(RegistryDataResponse.class)))
                .thenThrow(new IllegalStateException("connection reset"));

        DigitClientException thrown = assertThrows(DigitClientException.class,
                () -> client.updateRegistryData("Trade.License", payload(), "licenceNumber", "TL-1"));
        // updateRegistryData calls searchRegistryData, which has already wrapped this. The outer
        // catch therefore passes it through untouched rather than prefixing a second time, so the
        // message names the call that actually failed instead of the outermost one.
        assertEquals("Failed to search registry data: connection reset", thrown.getMessage());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    /** Parsed rather than hand-built, so {@code data} binds to the Maps and Lists Jackson produces. */
    private void stubSearch(String json) {
        RegistryDataResponse response = DigitJson.mapper().readValue(json, RegistryDataResponse.class);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(RegistryDataResponse.class)))
                .thenReturn(ResponseEntity.ok(response));
    }

    private void stubUpdate() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(), eq(RegistryDataResponse.class)))
                .thenReturn(ResponseEntity.ok(new RegistryDataResponse()));
    }

    private String capturedUpdateUrl() {
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).exchange(urlCaptor.capture(), eq(HttpMethod.PUT), any(),
                eq(RegistryDataResponse.class));
        return urlCaptor.getValue();
    }

    private int sentVersion() {
        ArgumentCaptor<HttpEntity> bodyCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.PUT), bodyCaptor.capture(),
                eq(RegistryDataResponse.class));
        return ((RegistryData) bodyCaptor.getValue().getBody()).getVersion();
    }

    private static RegistryData payload() {
        return RegistryData.builder()
                .data(DigitJson.mapper().createObjectNode().put("licenceNumber", "TL-1"))
                .build();
    }
}
