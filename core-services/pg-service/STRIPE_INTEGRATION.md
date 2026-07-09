# Stripe Payment Gateway Integration in pg-service

pg-service now supports **Stripe** as a payment gateway. The integration uses **Stripe Checkout** (Stripe-hosted payment page): pg-service
creates a Checkout Session, the citizen pays on Stripe's page, Stripe redirects back to the
supplied callback URL, and pg-service confirms the payment by querying Stripe's API directly —
no card data ever touches DIGIT infrastructure.

```
Citizen/UI ──create──▶ pg-service ──create session──▶ Stripe
Citizen/UI ◀─redirectUrl─┘
Citizen ──pays on Stripe checkout page──▶ Stripe ──redirects browser──▶ callbackUrl (UI)
UI ──update──▶ pg-service ──status query──▶ Stripe ──succeeded──▶ bill settled, receipt
```

## Setup

1. Create a Stripe sandbox (test-mode) account — from the [Stripe Dashboard](https://dashboard.stripe.com)
   (Developers → API keys, test mode), or without registration via the Stripe CLI:
   ```bash
   npm i -g @stripe/cli
   stripe sandbox create --email you@egovernments.org
   ```
   This gives a **secret key** (`sk_test_…` / `rkcs_test_…`) and a **publishable key** (`pk_test_…`).
2. Set the secret key in pg-service's environment/properties:
   ```properties
   stripe.active=true
   stripe.currency=inr
   stripe.secret.key=sk_test_...     # never commit this value
   ```
   The publishable key is not needed for this server-side hosted-checkout flow
   (`stripe.publishable.key` is reserved for future embedded-UI use).
3. Restart pg-service.

## Payment flow

### Step 1 — Initiate the payment (transaction create API)

Call transaction create with `gateway: "STRIPE"`. Ensure the `billId` refers to a valid
**ACTIVE** bill in billing-service, and `callbackUrl` is the URL Stripe should send the
citizen back to after payment — preferably a UI page that can then call the update API.

```bash
curl --location 'http://localhost:9000/pg-service/transaction/v3/_create' \
--header 'Content-Type: application/json' \
--header 'X-Tenant-ID: pg' \
--header 'X-User-ID: initiate-txn' \
--data-raw '{
    "Transaction": {
        "txnAmount": "700.00",
        "billId": "ee82c6a6-b0bf-49a5-924a-b96402516654",
        "module": "PT",
        "consumerCode": "PG-PT-1111",
        "taxAndPayments": [
            {
                "billId": "ee82c6a6-b0bf-49a5-924a-b96402516654",
                "taxAmount": 700.0,
                "amountPaid": 700.0
            }
        ],
        "productInfo": "Property Tax Payment",
        "gateway": "STRIPE",
        "callbackUrl": "https://example.com/success?stripe_session_id={CHECKOUT_SESSION_ID}",
        "user": {
            "uuid": "8d2a34df-7f65-4b9b-bf5e-cf0d2a23fb9b",
            "name": "Test Citizen",
            "userName": "test.citizen",
            "mobileNumber": "+919999999999",
            "emailId": "test@example.org",
            "tenantId": "pb"
        }
    }
}'
```

Response (actual sandbox run):

```json
{
    "Transaction": {
        "tenantId": "pg",
        "txnAmount": "700.00",
        "billId": "ee82c6a6-b0bf-49a5-924a-b96402516654",
        "module": "PT",
        "consumerCode": "PG-PT-1111",
        "productInfo": "Property Tax Payment",
        "gateway": "STRIPE",
        "callbackUrl": "https://example.com/success?stripe_session_id={CHECKOUT_SESSION_ID}&eg_pg_txnid=pg-PG-2026-07-09-0005-L6",
        "txnId": "pg-PG-2026-07-09-0005-L6",
        "redirectUrl": "https://checkout.stripe.com/c/pay/cs_test_a1CXkhB61dQwa0ig8lU92mamyrRNtW5i8P2t0iPlxH1ITgJLV3Szo7TTCA#fidnandhYHdW...",
        "txnStatus": "PENDING",
        "txnStatusMsg": "Transaction initiated",
        "gatewayTxnId": "cs_test_a1CXkhB61dQwa0ig8lU92mamyrRNtW5i8P2t0iPlxH1ITgJLV3Szo7TTCA",
        "gatewayPaymentMode": null,
        "gatewayStatusCode": null,
        "gatewayStatusMsg": null,
        "receipt": null,
        "auditDetails": { "createdBy": "initiate-txn", "createdTime": 1783598259553 }
    }
}
```

**What pg-service does internally with this request:**

1. Validates the transaction — the gateway must be active, the bill must not already have a
   PENDING/SUCCESS transaction, and `txnAmount` must equal the sum of `taxAndPayments`
   amounts.
2. Enriches it — generates the `txnId` from idgen (e.g. `pg-PG-2026-07-09-0005-L6`), sets
   status `PENDING`, and **appends `eg_pg_txnid=<txnId>` to your `callbackUrl`** (visible in
   the response above). This is how the UI later knows which transaction to update.
3. **Creates a Stripe Checkout Session.** The equivalent raw Stripe API call pg-service
   constructs (via the official `stripe-java` SDK):

```bash
curl --location 'https://api.stripe.com/v1/checkout/sessions' \
--header 'Idempotency-Key: pg-PG-2026-07-09-0005-L6' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--header 'Authorization: Basic <base64 of stripe.secret.key>' \
--data-urlencode 'mode=payment' \
--data-urlencode 'client_reference_id=pg-PG-2026-07-09-0005-L6' \
--data-urlencode 'line_items[0][price_data][currency]=inr' \
--data-urlencode 'line_items[0][price_data][unit_amount]=70000' \
--data-urlencode 'line_items[0][price_data][product_data][name]=Property Tax Payment' \
--data-urlencode 'line_items[0][quantity]=1' \
--data-urlencode 'metadata[txnId]=pg-PG-2026-07-09-0005-L6' \
--data-urlencode 'metadata[tenantId]=pg' \
--data-urlencode 'customer_email=test@example.org' \
--data-urlencode 'expires_at=<now + 30 minutes>' \
--data-urlencode 'success_url=https://example.com/success?stripe_session_id={CHECKOUT_SESSION_ID}&eg_pg_txnid=pg-PG-2026-07-09-0005-L6' \
--data-urlencode 'cancel_url=https://example.com/success?stripe_session_id={CHECKOUT_SESSION_ID}&eg_pg_txnid=pg-PG-2026-07-09-0005-L6'
```

   Key points about this request:
   - **`success_url` and `cancel_url` are both set to your `callbackUrl` + `eg_pg_txnid`** —
     whether the citizen pays or cancels, Stripe redirects the browser back to your page with
     the transaction id in the query string. (Stripe also substitutes the optional
     `{CHECKOUT_SESSION_ID}` placeholder if your callbackUrl contains one, as in this
     example.)
   - **`unit_amount=70000`** — Stripe expects amounts in **paise** (smallest currency unit);
     pg-service converts ₹700.00 → 70000 at this boundary and converts back when reading
     statuses.
   - **`client_reference_id` / `metadata`** carry our `txnId` and `tenantId` for traceability
     in the Stripe Dashboard.
   - **`Idempotency-Key` = txnId** — if the same create is retried due to a network glitch,
     Stripe returns the same session instead of creating a duplicate.
   - **`expires_at` = 30 minutes** — the checkout page dies after 30 min, aligned with the
     reconciliation jobs (below) so a payment can never land after the transaction has been
     closed.
   - `payment_method_types` is deliberately omitted — Stripe automatically offers the payment
     methods enabled in the Dashboard.

   Stripe's response (trimmed to the fields pg-service uses; full response contains ~60
   fields):

```json
{
    "id": "cs_test_a1CXkhB61dQwa0ig8lU92mamyrRNtW5i8P2t0iPlxH1ITgJLV3Szo7TTCA",
    "object": "checkout.session",
    "amount_total": 70000,
    "currency": "inr",
    "client_reference_id": "pg-PG-2026-07-09-0005-L6",
    "mode": "payment",
    "payment_intent": null,
    "payment_status": "unpaid",
    "status": "open",
    "livemode": false,
    "success_url": "https://example.com/success?stripe_session_id={CHECKOUT_SESSION_ID}&eg_pg_txnid=pg-PG-2026-07-09-0005-L6",
    "url": "https://checkout.stripe.com/c/pay/cs_test_a1CXkhB61dQwa0ig8lU92mamyrRNtW5i8P2t0iPlxH1ITgJLV3Szo7TTCA#fidnandhYHdW..."
}
```

4. Persists the transaction with the session id in `gatewayTxnId` (`cs_test_…` in the create
   response above) — this is the reference pg-service uses for every later status check —
   and returns the session's `url` as **`redirectUrl`**.

**The UI then redirects the citizen to `redirectUrl`** — Stripe's hosted checkout page,
showing the product info and ₹700.00. Test-mode payments use Stripe test cards
(`4242 4242 4242 4242` succeeds, `4000 0000 0000 0002` declines; any future expiry/CVC).
After payment, Stripe redirects the browser to the `callbackUrl` — which is why a UI page URL
is preferred there: that page reads `eg_pg_txnid` from its query string and triggers step 2.

### Step 2 — Confirm the payment (transaction update API)

From the callback page, call transaction update with the txnId:

```bash
curl --location --request PUT 'http://localhost:9000/pg-service/transaction/v3/_update?transactionId=pg-PG-2026-07-09-0005-L6' \
--header 'Content-Type: application/json' \
--header 'X-User-ID: update-txn' \
--header 'X-Tenant-ID: pg' \
--data ''
```

> The canonical query-parameter name is **`eg_pg_txnid`** (what pg-service appends to the
> callbackUrl).

Response (same sandbox run, after paying with `4242 4242 4242 4242`):

```json
{
    "Transaction": [
        {
            "tenantId": "pg",
            "txnAmount": "700.00",
            "billId": "ee82c6a6-b0bf-49a5-924a-b96402516654",
            "consumerCode": "PG-PT-1111",
            "productInfo": "Property Tax Payment",
            "gateway": "STRIPE",
            "txnId": "pg-PG-2026-07-09-0005-L6",
            "txnStatus": "SUCCESS",
            "txnStatusMsg": "Transaction successful",
            "gatewayTxnId": "pi_3TrGcJRp8JNyZBA51POQf7AZ",
            "gatewayPaymentMode": "card",
            "gatewayStatusCode": "succeeded",
            "gatewayStatusMsg": "succeeded",
            "auditDetails": {
                "createdBy": "initiate-txn",
                "lastModifiedBy": "update-txn",
                "createdTime": 1783598259553,
                "lastModifiedTime": 1783598445295
            }
        }
    ]
}
```

**What happens internally:** pg-service looks up the transaction, takes the stored Stripe
reference (`cs_test_…`), and **queries Stripe's API server-to-server** for the real status —
nothing sent by the browser is trusted. Stripe returns the PaymentIntent; its id (`pi_…`)
replaces the session id in `gatewayTxnId`, the amount is converted back from paise (70000 →
"700.00") and verified against our records, and the payment mode (`card`) and Stripe status
are recorded.

**Bill / demand outcome by case:**

| Case (as reported by Stripe) | Transaction result | Bill / demand effect                                                                                                     |
|---|---|--------------------------------------------------------------------------------------------------------------------------|
| Payment **succeeded** and amount matches exactly | `SUCCESS`, "Transaction successful" | pg-service calls billing-service's payment create — the payment is registered against the bill and the demand is settled |
| Payment succeeded but **amount mismatch** | `FAILURE`, "Transaction failed, amount mismatch" | **No** bill update, no receipt — flagged for investigation                                                               |
| Payment failed / cancelled / checkout expired | `FAILURE`, "Transaction failed at gateway" | No bill update; the bill is freed so the citizen can initiate a fresh payment                                            |
| Still pending at Stripe (citizen abandoned mid-checkout) | Finalized as `FAILURE` | Same as above — bill freed for retry                                                                                     |
| Transaction already SUCCESS with a receipt (duplicate/late update call) | Unchanged | No duplicate receipt — guarded                                                                                           |

The update API checks status **once per call** (no internal polling).

### Step 3 — Reconciliation safety net (cron jobs)

If the citizen pays but never returns to the callback page (closed the tab, network drop),
the transaction would sit in `PENDING` forever with the money already collected. Two Quartz
cron jobs inside pg-service close this gap — they pick up PENDING transactions and run the
exact same update flow (same Stripe status query) for each:

| Job | Schedule | Picks up |
|---|---|---|
| Early reconciliation | every 15 minutes | PENDING transactions created 15–30 minutes ago |
| Daily reconciliation | 00:00 and 12:00 | PENDING transactions older than 30 minutes |

Outcome per stuck transaction: if Stripe says the citizen actually paid → `SUCCESS`, bill
settled; if the checkout expired unpaid → `FAILURE`, bill freed for retry.
Because the Stripe checkout session is capped at 30 minutes, Stripe's answer is always final
by the time a job first touches the transaction — **worst-case detection time ≈ 30 minutes**,
the same as the other gateways.