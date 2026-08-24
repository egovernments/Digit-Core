package org.digit.services.notification;

import org.digit.config.ApiProperties;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.digit.services.notification.model.TemplateSearchCriteria;
import org.digit.services.notification.model.TemplateRequest;
import org.digit.services.notification.model.TemplatePreviewResponse;
import org.digit.services.notification.model.TemplatePreviewRequest;
import org.digit.services.notification.model.Template;
import org.digit.exception.DigitClientException;
import org.digit.services.notification.model.SendEmailRequest;
import org.digit.services.notification.model.SendEmailResponse;
import org.digit.services.notification.model.SendSMSRequest;
import org.digit.services.notification.model.SendSMSResponse;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class NotificationClient {
    private static final ParameterizedTypeReference<List<Template>> TEMPLATE_LIST =
            new ParameterizedTypeReference<List<Template>>() {};
    private static final ParameterizedTypeReference<Map<String, Object>> DELETED_RESULT =
            new ParameterizedTypeReference<Map<String, Object>>() {};

    private final RestTemplate restTemplate;
    private final ApiProperties apiProperties;

    public NotificationClient(RestTemplate restTemplate, ApiProperties apiProperties) {
        this.restTemplate = restTemplate;
        this.apiProperties = apiProperties;
    }

    public SendEmailResponse sendEmail(SendEmailRequest emailRequest) {
        if (emailRequest == null) {
            throw new DigitClientException("SendEmailRequest cannot be null");
        }
        if (emailRequest.getTemplateId() == null || emailRequest.getTemplateId().trim().isEmpty()) {
            throw new DigitClientException("Template ID cannot be null or empty");
        }
        if (emailRequest.getEmailIds() == null || emailRequest.getEmailIds().isEmpty()) {
            throw new DigitClientException("Email IDs cannot be null or empty");
        }
        try {
            log.debug("Sending email for templateId: {} to {} recipients", emailRequest.getTemplateId(), emailRequest.getEmailIds().size());
            String url = this.apiProperties.getNotificationServiceUrl() + "/notification/v3/email/send";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            ResponseEntity response = this.restTemplate.exchange(url, HttpMethod.POST, new HttpEntity(emailRequest, headers), SendEmailResponse.class);
            SendEmailResponse emailResponse = (SendEmailResponse)response.getBody();
            log.debug("Successfully sent email. Status: {}", (emailResponse != null ? emailResponse.getStatus() : "null"));
            return emailResponse;
        }
        catch (Exception e) {
            throw DigitClientException.wrap("Failed to send email", e);
        }
    }

    public SendEmailResponse sendEmail(String templateId, String version, List<String> emailIds, Map<String, Object> payload) {
        SendEmailRequest request = SendEmailRequest.builder().templateId(templateId).version(version).emailIds(emailIds).payload(payload).enrich(false).build();
        return this.sendEmail(request);
    }

    public SendSMSResponse sendSMS(SendSMSRequest smsRequest) {
        if (smsRequest == null) {
            throw new DigitClientException("SendSMSRequest cannot be null");
        }
        if (smsRequest.getTemplateId() == null || smsRequest.getTemplateId().trim().isEmpty()) {
            throw new DigitClientException("Template ID cannot be null or empty");
        }
        if (smsRequest.getMobileNumbers() == null || smsRequest.getMobileNumbers().isEmpty()) {
            throw new DigitClientException("Mobile numbers cannot be null or empty");
        }
        try {
            log.debug("Sending SMS for templateId: {} to {} recipients", smsRequest.getTemplateId(), smsRequest.getMobileNumbers().size());
            String url = this.apiProperties.getNotificationServiceUrl() + "/notification/v3/sms/send";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            ResponseEntity response = this.restTemplate.exchange(url, HttpMethod.POST, new HttpEntity(smsRequest, headers), SendSMSResponse.class);
            SendSMSResponse smsResponse = (SendSMSResponse)response.getBody();
            log.debug("Successfully sent SMS. Status: {}", (smsResponse != null ? smsResponse.getStatus() : "null"));
            return smsResponse;
        }
        catch (Exception e) {
            throw DigitClientException.wrap("Failed to send SMS", e);
        }
    }

    public SendSMSResponse sendSMS(String templateId, String version, List<String> mobileNumbers, Map<String, Object> payload, SendSMSRequest.SMSCategory category) {
        SendSMSRequest request = SendSMSRequest.builder().templateId(templateId).version(version).mobileNumbers(mobileNumbers).payload(payload).category(category).enrich(false).build();
        return this.sendSMS(request);
    }

    // ── Templates ────────────────────────────────────────────────────────────

    /** Registers a template as its first version. */
    public Template createTemplate(TemplateRequest request) {
        requireTemplateRequest(request);
        ResponseEntity<Template> response = this.restTemplate.postForEntity(
                templateUrl(), request, Template.class);
        return response.getBody();
    }

    /**
     * Publishes a new version of a template.
     *
     * <p>Versions are immutable, so this adds one rather than editing what exists — messages already
     * sent keep rendering from the version they used.
     */
    public Template updateTemplate(TemplateRequest request) {
        requireTemplateRequest(request);
        ResponseEntity<Template> response = this.restTemplate.exchange(
                templateUrl(), HttpMethod.PUT, new HttpEntity<>(request), Template.class);
        return response.getBody();
    }

    /** Searches templates. */
    public List<Template> searchTemplates(TemplateSearchCriteria criteria) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(templateUrl());
        if (criteria != null) {
            if (criteria.getIds() != null && !criteria.getIds().isEmpty()) {
                // A single comma-separated parameter, not one per id.
                builder.queryParam("ids", String.join(",", criteria.getIds()));
            }
            addIfText(builder, "templateId", criteria.getTemplateId());
            addIfText(builder, "version", criteria.getVersion());
            addIfText(builder, "type", criteria.getType());
            if (criteria.getIsHTML() != null) {
                builder.queryParam("isHTML", criteria.getIsHTML());
            }
            if (criteria.getLimit() != null && criteria.getLimit() > 0) {
                builder.queryParam("limit", criteria.getLimit());
            }
            if (criteria.getOffset() != null && criteria.getOffset() > 0) {
                builder.queryParam("offset", criteria.getOffset());
            }
        }
        ResponseEntity<List<Template>> response = this.restTemplate.exchange(
                builder.toUriString(), HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), TEMPLATE_LIST);
        List<Template> found = response.getBody();
        return found == null ? List.of() : found;
    }

    /** The latest version of a template, or null when the id is unknown. */
    public Template getTemplate(String templateId) {
        requireText(templateId, "templateId is required");
        List<Template> found = searchTemplates(TemplateSearchCriteria.builder().templateId(templateId).build());
        return found.isEmpty() ? null : found.get(found.size() - 1);
    }

    /**
     * Renders a template against a payload without sending it — the way to check a template before
     * putting it into use.
     */
    public TemplatePreviewResponse previewTemplate(TemplatePreviewRequest request) {
        if (request == null || isBlank(request.getTemplateId())) {
            throw new DigitClientException("templateId is required to preview a template");
        }
        ResponseEntity<TemplatePreviewResponse> response = this.restTemplate.postForEntity(
                templateUrl() + "/preview", request, TemplatePreviewResponse.class);
        return response.getBody();
    }

    /** Removes one version of a template; both arguments identify it. */
    public boolean deleteTemplate(String templateId, String version) {
        requireText(templateId, "templateId is required");
        requireText(version, "version is required — delete removes a single version");
        String url = UriComponentsBuilder.fromUriString(templateUrl())
                .queryParam("templateId", templateId)
                .queryParam("version", version)
                .toUriString();
        ResponseEntity<Map<String, Object>> response = this.restTemplate.exchange(
                url, HttpMethod.DELETE, new HttpEntity<>(new HttpHeaders()), DELETED_RESULT);
        return response.getBody() != null && Boolean.TRUE.equals(response.getBody().get("deleted"));
    }

    // ── Internals ────────────────────────────────────────────────────────────

    private String templateUrl() {
        return this.apiProperties.getNotificationServiceUrl() + "/notification/v3/template";
    }

    private static void requireTemplateRequest(TemplateRequest request) {
        if (request == null || isBlank(request.getTemplateId())) {
            throw new DigitClientException("templateId is required");
        }
        if (isBlank(request.getContent())) {
            throw new DigitClientException("content is required");
        }
        if (isBlank(request.getType())) {
            throw new DigitClientException("type is required (EMAIL or SMS)");
        }
    }

    private static void addIfText(UriComponentsBuilder builder, String name, String value) {
        if (!isBlank(value)) {
            builder.queryParam(name, value);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static void requireText(String value, String message) {
        if (isBlank(value)) {
            throw new DigitClientException(message);
        }
    }
}
