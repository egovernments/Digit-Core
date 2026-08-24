package org.digit.services.billing;

import org.digit.config.ApiProperties;
import org.digit.exception.DigitClientException;
import org.digit.services.billing.model.Bill;
import org.digit.services.billing.model.BillStatus;
import org.digit.services.billing.model.BusinessService;
import org.digit.services.billing.model.BusinessServiceCreate;
import org.digit.services.billing.model.BusinessServicePatch;
import org.digit.services.billing.model.BusinessServiceUpdate;
import org.digit.services.billing.model.BulkBillRequest;
import org.digit.services.billing.model.BulkBillResponse;
import org.digit.services.billing.model.TaxHeadPatch;
import org.digit.services.billing.model.TaxHeadUpdate;
import org.digit.services.billing.model.UpdateBillStatus;
import org.digit.services.billing.model.CancelDemandRequest;
import org.digit.services.billing.model.Demand;
import org.digit.services.billing.model.DemandCreate;
import org.digit.services.billing.model.DemandPatch;
import org.digit.services.billing.model.DemandStatus;
import org.digit.services.billing.model.DemandUpdate;
import org.digit.services.billing.model.GenerateBillCriteria;
import org.digit.services.billing.model.Payment;
import org.digit.services.billing.model.PaymentCreate;
import org.digit.services.billing.model.TaxHead;
import org.digit.services.billing.model.TaxHeadCreate;
import org.digit.services.billing.model.TaxHeadCategory;
import org.digit.services.common.model.BulkFailure;
import org.digit.services.common.model.BulkResult;
import org.digit.services.common.model.DigitError;
import org.digit.util.DigitJson;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

public class BillingClient {
    private final RestTemplate restTemplate;
    private final ApiProperties apiProperties;
    private final ObjectMapper objectMapper;

    public BillingClient(RestTemplate restTemplate, ApiProperties apiProperties) {
        this(restTemplate, apiProperties, DigitJson.mapper());
    }

    /** Retained for callers that supply their own mapper; must tolerate unknown response fields. */
    public BillingClient(RestTemplate restTemplate, ApiProperties apiProperties, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.apiProperties = apiProperties;
        this.objectMapper = objectMapper;
    }

    // ── Demands ──────────────────────────────────────────────────────────────

    /**
     * Creates demands. The endpoint takes a JSON array and can accept some items while rejecting
     * others, so the result reports both.
     */
    public BulkResult<Demand> createDemands(List<DemandCreate> demands) {
        requireNonEmpty(demands, "at least one demand must be provided");
        String url = this.apiProperties.getBillingServiceUrl() + "/billing/v3/demands";
        return bulk(url, HttpMethod.POST, demands, Demand.class);
    }

    /** Creates a single demand, throwing if the service rejected it. */
    public Demand createDemand(DemandCreate demandCreate) {
        return createDemands(List.of(demandCreate)).successOrThrow().get(0);
    }

    public BulkResult<Demand> updateDemands(List<DemandUpdate> demands) {
        requireNonEmpty(demands, "at least one demand must be provided");
        String url = this.apiProperties.getBillingServiceUrl() + "/billing/v3/demands";
        return bulk(url, HttpMethod.PUT, demands, Demand.class);
    }

    public Demand updateDemand(DemandUpdate demandUpdate) {
        return updateDemands(List.of(demandUpdate)).successOrThrow().get(0);
    }

    public Demand patchDemand(String demandId, DemandPatch demandPatch) {
        String url = this.apiProperties.getBillingServiceUrl() + "/billing/v3/demands/" + demandId;
        ResponseEntity<JsonNode> response = this.restTemplate.exchange(url, HttpMethod.PATCH, new HttpEntity<>(demandPatch), JsonNode.class);
        return this.objectMapper.convertValue(response.getBody(), Demand.class);
    }

    public Demand getDemandById(String demandId) {
        String url = this.apiProperties.getBillingServiceUrl() + "/billing/v3/demands/" + demandId;
        ResponseEntity<JsonNode> response = this.restTemplate.getForEntity(url, JsonNode.class);
        return this.objectMapper.convertValue(response.getBody(), Demand.class);
    }

    public List<Demand> searchDemands(String businessServiceCode, String consumerCode) {
        return searchDemands(businessServiceCode, consumerCode, null, null, null, null, null);
    }

    /**
     * Searches demands. {@code createdFrom}/{@code createdTo} bound the demand's creation time —
     * the service has no filter on the billing period. {@code limit} and {@code offset} fall back to
     * the service defaults of 25 and 0 when null.
     */
    public List<Demand> searchDemands(String businessServiceCode, String consumerCode, DemandStatus status,
                                      Long createdFrom, Long createdTo, Integer limit, Integer offset) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(this.apiProperties.getBillingServiceUrl() + "/billing/v3/demands");
        addIfPresent(builder, "businessServiceCode", businessServiceCode);
        addIfPresent(builder, "consumerCode", consumerCode);
        addIfPresent(builder, "status", status);
        addIfPresent(builder, "createdFrom", createdFrom);
        addIfPresent(builder, "createdTo", createdTo);
        addIfPresent(builder, "limit", limit);
        addIfPresent(builder, "offset", offset);
        return getList(builder.toUriString(), Demand.class);
    }

    public Demand freezeDemand(String demandId) {
        String url = this.apiProperties.getBillingServiceUrl() + "/billing/v3/demands/" + demandId + "/freeze";
        ResponseEntity<JsonNode> response = this.restTemplate.postForEntity(url, null, JsonNode.class);
        return this.objectMapper.convertValue(response.getBody(), Demand.class);
    }

    public Demand cancelDemand(String demandId, String reasonCode, String note) {
        String url = this.apiProperties.getBillingServiceUrl() + "/billing/v3/demands/" + demandId + "/cancel";
        CancelDemandRequest request = CancelDemandRequest.builder().reasonCode(reasonCode).note(note).build();
        ResponseEntity<JsonNode> response = this.restTemplate.postForEntity(url, request, JsonNode.class);
        return this.objectMapper.convertValue(response.getBody(), Demand.class);
    }

    public Demand cancelDemand(String demandId) {
        return this.cancelDemand(demandId, null, null);
    }

    // ── Bills ────────────────────────────────────────────────────────────────

    public Bill generateBill(GenerateBillCriteria criteria) {
        String url = this.apiProperties.getBillingServiceUrl() + "/billing/v3/bills/generate";
        ResponseEntity<JsonNode> response = this.restTemplate.postForEntity(url, criteria, JsonNode.class);
        return this.objectMapper.convertValue(response.getBody(), Bill.class);
    }

    public List<Bill> searchBills(String businessServiceCode, List<String> consumerCodes) {
        return searchBills(businessServiceCode, consumerCodes, null, null, null, null, null);
    }

    /** Searches bills. List filters are sent as comma-separated values, as the service parses them. */
    public List<Bill> searchBills(String businessServiceCode, List<String> consumerCodes, List<String> billIds,
                                  BillStatus status, String mobileNumber, Integer limit, Integer offset) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(this.apiProperties.getBillingServiceUrl() + "/billing/v3/bills");
        addIfPresent(builder, "businessServiceCode", businessServiceCode);
        addIfPresent(builder, "consumerCodes", csv(consumerCodes));
        addIfPresent(builder, "billIds", csv(billIds));
        addIfPresent(builder, "status", status);
        addIfPresent(builder, "mobileNumber", mobileNumber);
        addIfPresent(builder, "limit", limit);
        addIfPresent(builder, "offset", offset);
        return getList(builder.toUriString(), Bill.class);
    }

    public Bill getBillById(String billId) {
        List<Bill> bills = searchBills(null, null, List.of(billId), null, null, null, null);
        return bills != null && !bills.isEmpty() ? bills.get(0) : null;
    }

    // ── Payments ─────────────────────────────────────────────────────────────

    public Payment createPayment(PaymentCreate paymentCreate) {
        String url = this.apiProperties.getBillingServiceUrl() + "/billing/v3/payments";
        ResponseEntity<JsonNode> response = this.restTemplate.postForEntity(url, paymentCreate, JsonNode.class);
        return this.objectMapper.convertValue(response.getBody(), Payment.class);
    }

    public List<Payment> searchPayments(String businessServiceCode, List<String> consumerCodes) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(this.apiProperties.getBillingServiceUrl() + "/billing/v3/payments");
        addIfPresent(builder, "businessServiceCode", businessServiceCode);
        addIfPresent(builder, "consumerCodes", csv(consumerCodes));
        return getList(builder.toUriString(), Payment.class);
    }

    public Payment getPaymentById(String paymentId) {
        String url = this.apiProperties.getBillingServiceUrl() + "/billing/v3/payments/" + paymentId;
        ResponseEntity<JsonNode> response = this.restTemplate.getForEntity(url, JsonNode.class);
        return this.objectMapper.convertValue(response.getBody(), Payment.class);
    }

    // ── Business services and tax heads ──────────────────────────────────────

    public List<BusinessService> searchBusinessServices(String code) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(this.apiProperties.getBillingServiceUrl() + "/billing/v3/business-services");
        addIfPresent(builder, "code", code);
        return getList(builder.toUriString(), BusinessService.class);
    }

    /**
     * Registers business services. Like demands, the endpoint takes an array and can accept some
     * entries while rejecting others.
     */
    public BulkResult<BusinessService> createBusinessServices(List<BusinessServiceCreate> businessServices) {
        requireNonEmpty(businessServices, "at least one business service must be provided");
        businessServices.forEach(BillingClient::validateBusinessService);
        String url = this.apiProperties.getBillingServiceUrl() + "/billing/v3/business-services";
        return bulk(url, HttpMethod.POST, businessServices, BusinessService.class);
    }

    public BusinessService createBusinessService(BusinessServiceCreate businessService) {
        return createBusinessServices(List.of(businessService)).successOrThrow().get(0);
    }

    /** Registers tax heads. Each must name an existing business service. */
    public BulkResult<TaxHead> createTaxHeads(List<TaxHeadCreate> taxHeads) {
        requireNonEmpty(taxHeads, "at least one tax head must be provided");
        taxHeads.forEach(BillingClient::validateTaxHead);
        String url = this.apiProperties.getBillingServiceUrl() + "/billing/v3/tax-heads";
        return bulk(url, HttpMethod.POST, taxHeads, TaxHead.class);
    }

    public TaxHead createTaxHead(TaxHeadCreate taxHead) {
        return createTaxHeads(List.of(taxHead)).successOrThrow().get(0);
    }

    public List<TaxHead> searchTaxHeads(String businessServiceCode, String code, TaxHeadCategory category) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(this.apiProperties.getBillingServiceUrl() + "/billing/v3/tax-heads");
        addIfPresent(builder, "businessServiceCode", businessServiceCode);
        addIfPresent(builder, "code", code);
        addIfPresent(builder, "category", category);
        return getList(builder.toUriString(), TaxHead.class);
    }

    // ── Internals ────────────────────────────────────────────────────────────

    /**
     * Runs a bulk request and normalises the three answer shapes into one result.
     *
     * <p>A fully successful call returns a bare array; a mixed one returns 207 with {@code success}
     * and {@code failures}; a wholly failed one is turned into an exception by the error handler
     * before it reaches here, so its body is recovered from the exception.
     */
    private <T> BulkResult<T> bulk(String url, HttpMethod method, List<?> body, Class<T> type) {
        ResponseEntity<JsonNode> response;
        try {
            response = this.restTemplate.exchange(url, method, new HttpEntity<>(body), JsonNode.class);
        } catch (DigitClientException e) {
            throw toBulkFailure(e);
        }
        JsonNode payload = response.getBody();
        int status = response.getStatusCode().value();
        if (payload != null && payload.isObject() && (payload.has("success") || payload.has("failures"))) {
            List<T> success = convertList(payload.get("success"), type);
            List<BulkFailure> failures = convertList(payload.get("failures"), BulkFailure.class);
            return new BulkResult<>(status, success, failures);
        }
        return new BulkResult<>(status, convertList(payload, type), List.of());
    }

    /**
     * Rebuilds the per-item failures for an all-items-rejected response, whose body is a flat error
     * array with no index. The whole batch failed, so every submitted index is implicated; index -1
     * marks "not attributable to one item".
     */
    private org.digit.exception.DigitBulkOperationException toBulkFailure(DigitClientException e) {
        List<DigitError> errors = List.of();
        if (e.getResponseBody() != null && !e.getResponseBody().isBlank()) {
            try {
                errors = this.objectMapper.readValue(e.getResponseBody(),
                        this.objectMapper.getTypeFactory().constructCollectionType(List.class, DigitError.class));
            } catch (Exception parseFailure) {
                // Not the structured contract — keep the raw body on the original exception.
            }
        }
        return new org.digit.exception.DigitBulkOperationException(e.getMessage(),
                e.getHttpStatus().value(), List.of(new BulkFailure(-1, errors)));
    }

    private <T> List<T> convertList(JsonNode node, Class<T> type) {
        if (node == null || node.isNull()) {
            return List.of();
        }
        JavaType listType = this.objectMapper.getTypeFactory().constructCollectionType(List.class, type);
        return this.objectMapper.convertValue(node, listType);
    }

    private <T> List<T> getList(String url, Class<T> type) {
        ResponseEntity<JsonNode> response = this.restTemplate.getForEntity(url, JsonNode.class);
        return convertList(response.getBody(), type);
    }

    private static void addIfPresent(UriComponentsBuilder builder, String name, Object value) {
        if (value != null && !(value instanceof String s && s.isBlank())) {
            builder.queryParam(name, value);
        }
    }

    private static String csv(List<String> values) {
        return values == null || values.isEmpty() ? null : String.join(",", values);
    }

    private static void requireNonEmpty(List<?> items, String message) {
        if (items == null || items.isEmpty()) {
            throw new DigitClientException(message);
        }
    }

    /**
     * Checks the fields the service requires outright, so an incomplete catalogue entry fails with a
     * message naming the field rather than a bare constraint violation.
     */
    private static void validateBusinessService(BusinessServiceCreate businessService) {
        if (businessService == null) {
            throw new DigitClientException("business service must not be null");
        }
        requireField(businessService.getCode(), "code");
        requireField(businessService.getName(), "name");
        requireField(businessService.getCurrency(), "currency");
        if (businessService.getAllowedPaymentModes() == null || businessService.getAllowedPaymentModes().isEmpty()) {
            throw new DigitClientException("allowedPaymentModes must contain at least one mode");
        }
        if (businessService.getBillExpiryDays() == null) {
            throw new DigitClientException("billExpiryDays is required");
        }
        if (businessService.getEffectiveFrom() == null) {
            throw new DigitClientException("effectiveFrom is required");
        }
        if (businessService.getIsActive() == null) {
            throw new DigitClientException("isActive is required");
        }
        requireWindow(businessService.getEffectiveFrom(), businessService.getEffectiveTo());
    }

    private static void validateTaxHead(TaxHeadCreate taxHead) {
        if (taxHead == null) {
            throw new DigitClientException("tax head must not be null");
        }
        requireField(taxHead.getCode(), "code");
        requireField(taxHead.getName(), "name");
        requireField(taxHead.getBusinessServiceCode(), "businessServiceCode");
        if (taxHead.getOrder() == null || taxHead.getOrder() < 1) {
            throw new DigitClientException("order is required and must be at least 1");
        }
        if (taxHead.getEffectiveFrom() == null) {
            throw new DigitClientException("effectiveFrom is required");
        }
        if (taxHead.getIsActive() == null) {
            throw new DigitClientException("isActive is required");
        }
        requireWindow(taxHead.getEffectiveFrom(), taxHead.getEffectiveTo());
    }

    private static void requireField(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new DigitClientException(name + " is required");
        }
    }

    private static void requireWindow(Long effectiveFrom, Long effectiveTo) {
        if (effectiveTo != null && effectiveFrom != null && effectiveTo <= effectiveFrom) {
            throw new DigitClientException("effectiveTo must be strictly greater than effectiveFrom");
        }
    }

    /** One business service by code, or null when it is not registered. */
    public BusinessService getBusinessService(String code) {
        requireField(code, "code");
        return getOne(businessServicesUrl() + "/" + code, BusinessService.class);
    }

    /** Replaces a business service. Everything a create requires is required here too. */
    public BusinessService updateBusinessService(String code, BusinessServiceUpdate update) {
        requireField(code, "code");
        if (update == null) {
            throw new DigitClientException("update payload is required");
        }
        return exchangeOne(businessServicesUrl() + "/" + code, HttpMethod.PUT, update, BusinessService.class);
    }

    /** Changes only the fields set on {@code patch}. */
    public BusinessService patchBusinessService(String code, BusinessServicePatch patch) {
        requireField(code, "code");
        if (patch == null) {
            throw new DigitClientException("patch payload is required");
        }
        return exchangeOne(businessServicesUrl() + "/" + code, HttpMethod.PATCH, patch, BusinessService.class);
    }

    public boolean deleteBusinessService(String code) {
        requireField(code, "code");
        return deleted(businessServicesUrl() + "/" + code);
    }

    /** One tax head by code, or null. */
    public TaxHead getTaxHead(String code) {
        requireField(code, "code");
        return getOne(taxHeadsUrl() + "/" + code, TaxHead.class);
    }

    public TaxHead updateTaxHead(String code, TaxHeadUpdate update) {
        requireField(code, "code");
        if (update == null) {
            throw new DigitClientException("update payload is required");
        }
        return exchangeOne(taxHeadsUrl() + "/" + code, HttpMethod.PUT, update, TaxHead.class);
    }

    public TaxHead patchTaxHead(String code, TaxHeadPatch patch) {
        requireField(code, "code");
        if (patch == null) {
            throw new DigitClientException("patch payload is required");
        }
        return exchangeOne(taxHeadsUrl() + "/" + code, HttpMethod.PATCH, patch, TaxHead.class);
    }

    public boolean deleteTaxHead(String code) {
        requireField(code, "code");
        return deleted(taxHeadsUrl() + "/" + code);
    }

    // ── Bulk bills, cancellation and payment validation ──────────────────────

    /**
     * Queues bill generation for every eligible consumer of a business service.
     *
     * <p>Answered with 202: the response describes the scheduled work, not the bills, which appear
     * later. Nothing is generated synchronously.
     */
    public BulkBillResponse bulkGenerateBills(BulkBillRequest request) {
        if (request == null || isBlank(request.getBusinessServiceCode())) {
            throw new DigitClientException("businessServiceCode is required for a bulk bill run");
        }
        String url = this.apiProperties.getBillingServiceUrl() + "/billing/v3/bills/bulk-generate";
        ResponseEntity<JsonNode> response = this.restTemplate.postForEntity(url, request, JsonNode.class);
        return this.objectMapper.convertValue(response.getBody(), BulkBillResponse.class);
    }

    /** Moves a consumer's bills to another status — how a bill is cancelled. */
    public List<Bill> cancelBills(UpdateBillStatus request) {
        if (request == null || isBlank(request.getBusinessServiceCode())
                || isBlank(request.getConsumerCode()) || request.getStatusToBeUpdated() == null) {
            throw new DigitClientException("businessServiceCode, consumerCode and statusToBeUpdated are required");
        }
        String url = this.apiProperties.getBillingServiceUrl() + "/billing/v3/bills/cancel";
        ResponseEntity<JsonNode> response = this.restTemplate.postForEntity(url, request, JsonNode.class);
        return convertList(response.getBody(), Bill.class);
    }

    /**
     * Dry-runs a payment: the service applies the same checks as a create and returns what would be
     * recorded, without persisting anything.
     */
    public Payment validatePayment(PaymentCreate payment) {
        if (payment == null) {
            throw new DigitClientException("payment payload is required");
        }
        String url = this.apiProperties.getBillingServiceUrl() + "/billing/v3/payments/validate";
        ResponseEntity<JsonNode> response = this.restTemplate.postForEntity(url, payment, JsonNode.class);
        return this.objectMapper.convertValue(response.getBody(), Payment.class);
    }

    private String businessServicesUrl() {
        return this.apiProperties.getBillingServiceUrl() + "/billing/v3/business-services";
    }

    private String taxHeadsUrl() {
        return this.apiProperties.getBillingServiceUrl() + "/billing/v3/tax-heads";
    }

    private <T> T getOne(String url, Class<T> type) {
        try {
            ResponseEntity<JsonNode> response = this.restTemplate.getForEntity(url, JsonNode.class);
            return this.objectMapper.convertValue(response.getBody(), type);
        } catch (DigitClientException e) {
            if (e.getHttpStatus() != null && e.getHttpStatus().value() == 404) {
                return null;
            }
            throw e;
        }
    }

    private <T> T exchangeOne(String url, HttpMethod method, Object body, Class<T> type) {
        ResponseEntity<JsonNode> response = this.restTemplate.exchange(
                url, method, new HttpEntity<>(body), JsonNode.class);
        return this.objectMapper.convertValue(response.getBody(), type);
    }

    private boolean deleted(String url) {
        ResponseEntity<JsonNode> response = this.restTemplate.exchange(
                url, HttpMethod.DELETE, HttpEntity.EMPTY, JsonNode.class);
        JsonNode body = response.getBody();
        return body != null && body.path("deleted").asBoolean(false);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
