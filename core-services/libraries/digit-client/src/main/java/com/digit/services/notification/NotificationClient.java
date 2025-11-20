package com.digit.services.notification;

import com.digit.config.ApiProperties;
import com.digit.exception.DigitClientException;
import com.digit.services.notification.model.SendEmailRequest;
import com.digit.services.notification.model.SendEmailResponse;
import com.digit.services.notification.model.SendSMSRequest;
import com.digit.services.notification.model.SendSMSResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Service client for Notification API operations.
 * Provides methods to send emails and SMS notifications.
 */
@Slf4j
@Getter
@Setter
public class NotificationClient {

    private final RestTemplate restTemplate;
    private final ApiProperties apiProperties;

    /**
     * Constructor for NotificationClient.
     *
     * @param restTemplate the RestTemplate for HTTP operations
     * @param apiProperties the API configuration properties
     */
    public NotificationClient(RestTemplate restTemplate, ApiProperties apiProperties) {
        this.restTemplate = restTemplate;
        this.apiProperties = apiProperties;

        // DEBUG: Check which RestTemplate we're using
        System.out.println("🔍 NotificationClient created with RestTemplate: " + restTemplate.getClass().getSimpleName());
        System.out.println("🔍 RestTemplate interceptors: " + restTemplate.getInterceptors().size());
        restTemplate.getInterceptors().forEach(interceptor -> {
            System.out.println("  - " + interceptor.getClass().getSimpleName());
        });
    }

    /**
     * Sends an email notification using the specified template and parameters.
     *
     * @param emailRequest the email send request
     * @return the SendEmailResponse object
     * @throws DigitClientException if email sending fails
     */
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
            log.debug("Sending email for templateId: {} to {} recipients", 
                    emailRequest.getTemplateId(), emailRequest.getEmailIds().size());
            
            String url = apiProperties.getNotificationServiceUrl() + "/notification/v1/email/send";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            
            HttpEntity<SendEmailRequest> entity = new HttpEntity<>(emailRequest, headers);
            
            ResponseEntity<SendEmailResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, SendEmailResponse.class);
            
            SendEmailResponse emailResponse = response.getBody();
            log.debug("Successfully sent email. Status: {}", 
                    emailResponse != null ? emailResponse.getStatus() : "null");
            
            return emailResponse;
            
        } catch (Exception e) {
            log.error("Failed to send email for templateId: {}", emailRequest.getTemplateId(), e);
            if (e instanceof DigitClientException) {
                throw e;
            }
            throw new DigitClientException("Failed to send email: " + e.getMessage(), e);
        }
    }

    /**
     * Sends an SMS notification using the specified template and parameters.
     *
     * @param smsRequest the SMS send request
     * @return the SendSMSResponse object
     * @throws DigitClientException if SMS sending fails
     */
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
            log.debug("Sending SMS for templateId: {} to {} recipients", 
                    smsRequest.getTemplateId(), smsRequest.getMobileNumbers().size());
            
            String url = apiProperties.getNotificationServiceUrl() + "/notification/v1/sms/send";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            
            HttpEntity<SendSMSRequest> entity = new HttpEntity<>(smsRequest, headers);
            
            ResponseEntity<SendSMSResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, SendSMSResponse.class);
            
            SendSMSResponse smsResponse = response.getBody();
            log.debug("Successfully sent SMS. Status: {}", 
                    smsResponse != null ? smsResponse.getStatus() : "null");
            
            return smsResponse;
            
        } catch (Exception e) {
            log.error("Failed to send SMS for templateId: {}", smsRequest.getTemplateId(), e);
            if (e instanceof DigitClientException) {
                throw e;
            }
            throw new DigitClientException("Failed to send SMS: " + e.getMessage(), e);
        }
    }

    /**
     * Sends an email with simplified parameters.
     *
     * @param templateId the template ID
     * @param version the template version
     * @param emailIds the list of email addresses
     * @param payload the payload data
     * @return the SendEmailResponse object
     * @throws DigitClientException if email sending fails
     */
    public SendEmailResponse sendEmail(String templateId, String version, List<String> emailIds, Map<String, Object> payload) {
        SendEmailRequest request = SendEmailRequest.builder()
                .templateId(templateId)
                .version(version)
                .emailIds(emailIds)
                .payload(payload)
                .enrich(false)
                .build();
        
        return sendEmail(request);
    }

    /**
     * Sends an SMS with simplified parameters.
     *
     * @param templateId the template ID
     * @param version the template version
     * @param mobileNumbers the list of mobile numbers
     * @param payload the payload data
     * @param category the SMS category
     * @return the SendSMSResponse object
     * @throws DigitClientException if SMS sending fails
     */
    public SendSMSResponse sendSMS(String templateId, String version, List<String> mobileNumbers, 
                                  Map<String, Object> payload, SendSMSRequest.SMSCategory category) {
        SendSMSRequest request = SendSMSRequest.builder()
                .templateId(templateId)
                .version(version)
                .mobileNumbers(mobileNumbers)
                .payload(payload)
                .category(category)
                .enrich(false)
                .build();
        
        return sendSMS(request);
    }
}
