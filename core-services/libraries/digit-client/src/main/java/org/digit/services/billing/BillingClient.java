package org.digit.services.billing;

import org.digit.config.ApiProperties;
import org.digit.services.billing.model.Bill;
import org.digit.services.billing.model.BusinessService;
import org.digit.services.billing.model.CancelDemandRequest;
import org.digit.services.billing.model.Demand;
import org.digit.services.billing.model.DemandCreate;
import org.digit.services.billing.model.DemandPatch;
import org.digit.services.billing.model.DemandUpdate;
import org.digit.services.billing.model.GenerateBillCriteria;
import org.digit.services.billing.model.Payment;
import org.digit.services.billing.model.PaymentCreate;
import org.digit.services.billing.model.TaxHead;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    public BillingClient(RestTemplate restTemplate, ApiProperties apiProperties, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.apiProperties = apiProperties;
        this.objectMapper = objectMapper;
    }

    public Demand createDemand(DemandCreate demandCreate) {
        String url = this.apiProperties.getBillingServiceUrl() + "/billing/v3/demands";
        ResponseEntity response = this.restTemplate.postForEntity(url, (Object)demandCreate, JsonNode.class, new Object[0]);
        return (Demand)this.objectMapper.convertValue(response.getBody(), Demand.class);
    }

    public Demand updateDemand(DemandUpdate demandUpdate) {
        String url = this.apiProperties.getBillingServiceUrl() + "/billing/v3/demands";
        ResponseEntity response = this.restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity((Object)demandUpdate), JsonNode.class, new Object[0]);
        return (Demand)this.objectMapper.convertValue(response.getBody(), Demand.class);
    }

    public Demand patchDemand(String demandId, DemandPatch demandPatch) {
        String url = this.apiProperties.getBillingServiceUrl() + "/billing/v3/demands/" + demandId;
        ResponseEntity response = this.restTemplate.exchange(url, HttpMethod.PATCH, new HttpEntity((Object)demandPatch), JsonNode.class, new Object[0]);
        return (Demand)this.objectMapper.convertValue(response.getBody(), Demand.class);
    }

    public Demand getDemandById(String demandId) {
        String url = this.apiProperties.getBillingServiceUrl() + "/billing/v3/demands/" + demandId;
        ResponseEntity response = this.restTemplate.getForEntity(url, JsonNode.class, new Object[0]);
        return (Demand)this.objectMapper.convertValue(response.getBody(), Demand.class);
    }

    public List<Demand> searchDemands(String businessServiceCode, String consumerCode) {
        String url = UriComponentsBuilder.fromUriString((String)(this.apiProperties.getBillingServiceUrl() + "/billing/v3/demands")).queryParam("businessServiceCode", new Object[]{businessServiceCode}).queryParam("consumerCode", new Object[]{consumerCode}).toUriString();
        ResponseEntity response = this.restTemplate.getForEntity(url, JsonNode.class, new Object[0]);
        return (List)this.objectMapper.convertValue(response.getBody(), (JavaType)this.objectMapper.getTypeFactory().constructCollectionType(List.class, Demand.class));
    }

    public List<Demand> searchDemandsWithFilters(String businessServiceCode, String consumerCode, String status, Long periodFrom, Long periodTo, int limit, int offset) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString((String)(this.apiProperties.getBillingServiceUrl() + "/billing/v3/demands")).queryParam("businessServiceCode", new Object[]{businessServiceCode}).queryParam("consumerCode", new Object[]{consumerCode}).queryParam("limit", new Object[]{limit}).queryParam("offset", new Object[]{offset});
        if (status != null) {
            builder.queryParam("status", new Object[]{status});
        }
        if (periodFrom != null) {
            builder.queryParam("periodFrom", new Object[]{periodFrom});
        }
        if (periodTo != null) {
            builder.queryParam("periodTo", new Object[]{periodTo});
        }
        ResponseEntity response = this.restTemplate.getForEntity(builder.toUriString(), JsonNode.class, new Object[0]);
        return (List)this.objectMapper.convertValue(response.getBody(), (JavaType)this.objectMapper.getTypeFactory().constructCollectionType(List.class, Demand.class));
    }

    public Demand freezeDemand(String demandId) {
        String url = this.apiProperties.getBillingServiceUrl() + "/billing/v3/demands/" + demandId + "/freeze";
        ResponseEntity response = this.restTemplate.postForEntity(url, null, JsonNode.class, new Object[0]);
        return (Demand)this.objectMapper.convertValue(response.getBody(), Demand.class);
    }

    public Demand cancelDemand(String demandId, String reasonCode, String note) {
        String url = this.apiProperties.getBillingServiceUrl() + "/billing/v3/demands/" + demandId + "/cancel";
        CancelDemandRequest request = CancelDemandRequest.builder().reasonCode(reasonCode).note(note).build();
        ResponseEntity response = this.restTemplate.postForEntity(url, (Object)request, JsonNode.class, new Object[0]);
        return (Demand)this.objectMapper.convertValue(response.getBody(), Demand.class);
    }

    public Demand cancelDemand(String demandId) {
        return this.cancelDemand(demandId, null, null);
    }

    public Bill generateBill(GenerateBillCriteria criteria) {
        String url = this.apiProperties.getBillingServiceUrl() + "/billing/v3/bills/generate";
        ResponseEntity response = this.restTemplate.postForEntity(url, (Object)criteria, JsonNode.class, new Object[0]);
        return (Bill)this.objectMapper.convertValue(response.getBody(), Bill.class);
    }

    public List<Bill> searchBills(String businessServiceCode, String consumerCode) {
        String url = UriComponentsBuilder.fromUriString((String)(this.apiProperties.getBillingServiceUrl() + "/billing/v3/bills")).queryParam("businessServiceCode", new Object[]{businessServiceCode}).queryParam("consumerCode", new Object[]{consumerCode}).toUriString();
        ResponseEntity response = this.restTemplate.getForEntity(url, JsonNode.class, new Object[0]);
        return (List)this.objectMapper.convertValue(response.getBody(), (JavaType)this.objectMapper.getTypeFactory().constructCollectionType(List.class, Bill.class));
    }

    public Bill getBillById(String billId) {
        String url = UriComponentsBuilder.fromUriString((String)(this.apiProperties.getBillingServiceUrl() + "/billing/v3/bills")).queryParam("billIds", new Object[]{billId}).toUriString();
        ResponseEntity response = this.restTemplate.getForEntity(url, JsonNode.class, new Object[0]);
        List bills = (List)this.objectMapper.convertValue(response.getBody(), (JavaType)this.objectMapper.getTypeFactory().constructCollectionType(List.class, Bill.class));
        return bills != null && !bills.isEmpty() ? (Bill)bills.get(0) : null;
    }

    public Payment createPayment(PaymentCreate paymentCreate) {
        String url = this.apiProperties.getBillingServiceUrl() + "/billing/v3/payments";
        ResponseEntity response = this.restTemplate.postForEntity(url, (Object)paymentCreate, JsonNode.class, new Object[0]);
        return (Payment)this.objectMapper.convertValue(response.getBody(), Payment.class);
    }

    public List<Payment> searchPayments(String businessServiceCode, String consumerCode) {
        String url = UriComponentsBuilder.fromUriString((String)(this.apiProperties.getBillingServiceUrl() + "/billing/v3/payments")).queryParam("businessServiceCode", new Object[]{businessServiceCode}).queryParam("consumerCode", new Object[]{consumerCode}).toUriString();
        ResponseEntity response = this.restTemplate.getForEntity(url, JsonNode.class, new Object[0]);
        return (List)this.objectMapper.convertValue(response.getBody(), (JavaType)this.objectMapper.getTypeFactory().constructCollectionType(List.class, Payment.class));
    }

    public Payment getPaymentById(String paymentId) {
        String url = this.apiProperties.getBillingServiceUrl() + "/billing/v3/payments/" + paymentId;
        ResponseEntity response = this.restTemplate.getForEntity(url, JsonNode.class, new Object[0]);
        return (Payment)this.objectMapper.convertValue(response.getBody(), Payment.class);
    }

    public List<BusinessService> searchBusinessServices(String code) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString((String)(this.apiProperties.getBillingServiceUrl() + "/billing/v3/business-services"));
        if (code != null) {
            builder.queryParam("code", new Object[]{code});
        }
        ResponseEntity response = this.restTemplate.getForEntity(builder.toUriString(), JsonNode.class, new Object[0]);
        return (List)this.objectMapper.convertValue(response.getBody(), (JavaType)this.objectMapper.getTypeFactory().constructCollectionType(List.class, BusinessService.class));
    }

    public List<TaxHead> searchTaxHeads(String businessServiceCode, String code, String category) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString((String)(this.apiProperties.getBillingServiceUrl() + "/billing/v3/tax-heads"));
        if (businessServiceCode != null) {
            builder.queryParam("businessServiceCode", new Object[]{businessServiceCode});
        }
        if (code != null) {
            builder.queryParam("code", new Object[]{code});
        }
        if (category != null) {
            builder.queryParam("category", new Object[]{category});
        }
        ResponseEntity response = this.restTemplate.getForEntity(builder.toUriString(), JsonNode.class, new Object[0]);
        return (List)this.objectMapper.convertValue(response.getBody(), (JavaType)this.objectMapper.getTypeFactory().constructCollectionType(List.class, TaxHead.class));
    }
}

