package org.egov.gateway.spi;

import org.egov.payment.models.Transaction;

import java.net.URI;
import java.util.Map;

/**
 * SPI contract for a gateway provider instance (created per-request by GatewayProviderFactory.create()).
 * Zero Spring annotations. Instances are NOT singletons — they are constructed with config.
 *
 * Note: Transaction here is org.egov.payment.models.Transaction.
 * This is a deliberate single-module trade-off (see DECISIONS.md).
 */
public interface GatewayProvider {

    /**
     * Returns the gateway identifier (lowercase), e.g. "axis", "paytm", "phonepe", "payu".
     */
    String getGatewayId();

    /**
     * Build the redirect URI to the payment gateway for this transaction.
     *
     * @param transaction the transaction to initiate
     * @return the URI to redirect the user to
     * @throws GatewayException on any failure
     */
    URI generateRedirectURI(Transaction transaction);

    /**
     * Fetch the live status of a transaction from the gateway.
     *
     * @param transaction       the current transaction state from DB
     * @param responseParameters parameters posted back by the gateway callback
     * @return updated transaction with live status fields populated
     * @throws GatewayException on any failure
     */
    Transaction fetchStatus(Transaction transaction, Map<String, String> responseParameters);

    /**
     * The key in the gateway callback parameters that contains our transaction ID.
     * Used by GatewayService.getTxnId() for matching.
     */
    String transactionIdKeyInResponse();
}
