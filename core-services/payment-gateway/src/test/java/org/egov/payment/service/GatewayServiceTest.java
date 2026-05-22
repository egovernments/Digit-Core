package org.egov.payment.service;

import org.egov.payment.service.registry.GatewayProviderRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GatewayService.
 * Builds a real GatewayProviderRegistry backed by a MockEnvironment
 * (no gateways active) to test the service's delegation logic and
 * txn-ID extraction from callback params.
 */
class GatewayServiceTest {

    private GatewayService gatewayService;

    @BeforeEach
    void setUp() {
        // No gateways active — tests the default/empty state
        MockEnvironment env = new MockEnvironment();
        GatewayProviderRegistry registry = new GatewayProviderRegistry(env);
        registry.initialize();
        gatewayService = new GatewayService(registry);
    }

    @Test
    void isGatewayActiveShouldReturnFalseWhenNoGatewaysEnabled() {
        assertFalse(gatewayService.isGatewayActive("axis"));
        assertFalse(gatewayService.isGatewayActive("paytm"));
        assertFalse(gatewayService.isGatewayActive("phonepe"));
        assertFalse(gatewayService.isGatewayActive("payu"));
    }

    @Test
    void isGatewayActiveShouldBeCaseInsensitive() {
        assertFalse(gatewayService.isGatewayActive("AXIS"));
        assertFalse(gatewayService.isGatewayActive("Paytm"));
    }

    @Test
    void getActiveGatewaysShouldReturnEmptySetWhenNoneEnabled() {
        assertTrue(gatewayService.getActiveGateways().isEmpty());
    }

    @Test
    void getTxnIdShouldReturnValueFromPgTxnInLabel() {
        // PG_TXN_IN_LABEL = "eg_pg_txnid"
        Map<String, String> params = new HashMap<>();
        params.put("eg_pg_txnid", "TXN12345");
        params.put("vpc_MerchTxnRef", "OTHER_ID");

        Optional<String> txnId = gatewayService.getTxnId(params);
        assertTrue(txnId.isPresent(), "Should find txn ID from eg_pg_txnid");
        assertEquals("TXN12345", txnId.get());
    }

    @Test
    void getTxnIdShouldReturnEmptyForEmptyParams() {
        Optional<String> txnId = gatewayService.getTxnId(Map.of());
        assertFalse(txnId.isPresent(), "Empty params should return empty optional");
    }

    @Test
    void getTxnIdShouldBeCaseInsensitiveForPgLabel() {
        Map<String, String> params = new HashMap<>();
        params.put("EG_PG_TXNID", "TXN999");

        Optional<String> txnId = gatewayService.getTxnId(params);
        assertTrue(txnId.isPresent(), "Lookup should be case-insensitive");
        assertEquals("TXN999", txnId.get());
    }

    @Test
    void getTxnIdShouldReturnEmptyWhenNoKnownKeyPresent() {
        Map<String, String> params = new HashMap<>();
        params.put("some_unknown_key", "VALUE123");

        Optional<String> txnId = gatewayService.getTxnId(params);
        // No active gateways, so TXN_IDS_KEY_SET only has eg_pg_txnid. "some_unknown_key" not found.
        assertFalse(txnId.isPresent());
    }
}
