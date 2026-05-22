package org.egov.payment.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.gateway.spi.GatewayProvider;
import org.egov.payment.constants.PgConstants;
import org.egov.payment.models.Transaction;
import org.egov.payment.service.registry.GatewayProviderRegistry;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.*;

/**
 * Spring service that delegates to the SPI-based GatewayProviderRegistry.
 * Adapts gateway operations for use by TransactionService and TransactionValidator.
 */
@Slf4j
@Service
public class GatewayService {

    private final GatewayProviderRegistry registry;
    // Ordered set of txn ID keys to check in callback params
    private Set<String> TXN_IDS_KEY_SET;

    public GatewayService(GatewayProviderRegistry registry) {
        this.registry = registry;
        initTxnIdKeys();
    }

    private void initTxnIdKeys() {
        Set<String> keys = new LinkedHashSet<>();
        // First try the standard DIGIT callback label
        keys.add(PgConstants.PG_TXN_IN_LABEL);
        // Then try each active gateway's response key
        for (String gatewayId : registry.getActiveGatewayIds()) {
            try {
                var factory = registry.getFactory(gatewayId);
                var provider = factory.create(registry.resolveConfigFor(gatewayId));
                keys.add(provider.transactionIdKeyInResponse());
            } catch (Exception e) {
                log.warn("Could not get transactionIdKeyInResponse for gateway {}: {}", gatewayId, e.getMessage());
            }
        }
        TXN_IDS_KEY_SET = Collections.unmodifiableSet(keys);
        log.info("Transaction ID key set initialized: {}", TXN_IDS_KEY_SET);
    }

    /**
     * Initiates a transaction via the specified gateway.
     */
    public URI initiateTxn(Transaction transaction) {
        String gatewayId = transaction.getGateway().toLowerCase();
        var factory = registry.getFactory(gatewayId);
        GatewayProvider provider = factory.create(registry.resolveConfigFor(gatewayId));
        return provider.generateRedirectURI(transaction);
    }

    /**
     * Fetches live transaction status from the gateway.
     */
    public Transaction getLiveStatus(Transaction current, Map<String, String> params) {
        String gatewayId = current.getGateway().toLowerCase();
        var factory = registry.getFactory(gatewayId);
        GatewayProvider provider = factory.create(registry.resolveConfigFor(gatewayId));
        return provider.fetchStatus(current, params);
    }

    /**
     * Extracts the transaction ID from the callback params.
     * Tries PG_TXN_IN_LABEL first, then each gateway's transactionIdKeyInResponse().
     */
    public Optional<String> getTxnId(Map<String, String> params) {
        Map<String, String> caseInsensitiveMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        caseInsensitiveMap.putAll(params);
        for (String txnIdKey : TXN_IDS_KEY_SET) {
            if (caseInsensitiveMap.containsKey(txnIdKey)) {
                return Optional.of(caseInsensitiveMap.get(txnIdKey));
            }
        }
        return Optional.empty();
    }

    public boolean isGatewayActive(String gateway) {
        return registry.isActive(gateway.toLowerCase());
    }

    public Set<String> getActiveGateways() {
        return registry.getActiveGatewayIds();
    }
}
