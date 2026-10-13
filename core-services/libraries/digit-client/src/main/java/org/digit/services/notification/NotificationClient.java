package org.digit.services.notification;

import org.digit.config.ApiProperties;
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
            log.debug("Sending email for templateId: {} to {} recipients", (Object)emailRequest.getTemplateId(), (Object)emailRequest.getEmailIds().size());
            String url = this.apiProperties.getNotificationServiceUrl() + "/notification/v3/email/send";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            ResponseEntity response = this.restTemplate.exchange(url, HttpMethod.POST, new HttpEntity((Object)emailRequest, headers), SendEmailResponse.class, new Object[0]);
            SendEmailResponse emailResponse = (SendEmailResponse)response.getBody();
            log.debug("Successfully sent email. Status: {}", (Object)(emailResponse != null ? emailResponse.getStatus() : "null"));
            return emailResponse;
        }
        catch (Exception e) {
            if (e instanceof DigitClientException) {
                throw e;
            }
            throw new DigitClientException("Failed to send email: " + e.getMessage(), e);
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
            log.debug("Sending SMS for templateId: {} to {} recipients", (Object)smsRequest.getTemplateId(), (Object)smsRequest.getMobileNumbers().size());
            String url = this.apiProperties.getNotificationServiceUrl() + "/notification/v3/sms/send";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            ResponseEntity response = this.restTemplate.exchange(url, HttpMethod.POST, new HttpEntity((Object)smsRequest, headers), SendSMSResponse.class, new Object[0]);
            SendSMSResponse smsResponse = (SendSMSResponse)response.getBody();
            log.debug("Successfully sent SMS. Status: {}", (Object)(smsResponse != null ? smsResponse.getStatus() : "null"));
            return smsResponse;
        }
        catch (Exception e) {
            if (e instanceof DigitClientException) {
                throw e;
            }
            throw new DigitClientException("Failed to send SMS: " + e.getMessage(), e);
        }
    }

    public SendSMSResponse sendSMS(String templateId, String version, List<String> mobileNumbers, Map<String, Object> payload, SendSMSRequest.SMSCategory category) {
        SendSMSRequest request = SendSMSRequest.builder().templateId(templateId).version(version).mobileNumbers(mobileNumbers).payload(payload).category(category).enrich(false).build();
        return this.sendSMS(request);
    }
}