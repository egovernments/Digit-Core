package org.egov.web.notification.sms.service.impl;

import lombok.extern.slf4j.*;
import org.egov.web.notification.sms.service.*;

import org.egov.web.notification.sms.models.Sms;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import org.springframework.http.*;

import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.*;

@Service
@Slf4j
@ConditionalOnProperty(value = "sms.provider.class", matchIfMissing = true, havingValue = "Generic")
public class GenericSMSServiceImpl extends BaseSMSService {

    @Value("${sms.url.dont_encode_url:true}")
    private boolean dontEncodeURL;

    protected void submitToExternalSmsService(Sms sms) {
        try {
            // Modify the mobile number based on its length.
            modifyMobileNumber(sms, smsProperties);

            // Retrieve the SMS provider URL from configuration.
            String url = smsProperties.getUrl();

            // Prepare the SMS request body with necessary parameters.
            final MultiValueMap<String, String> requestBody = getSmsRequestBody(sms);

            // Build the final URL with query parameters encoded.
            URI final_url = UriComponentsBuilder.fromHttpUrl(url).queryParams(requestBody).build().encode().toUri();

            // Execute the API call to send the SMS.
            executeAPI(final_url, HttpMethod.GET, null, String.class);
        } catch (RestClientException e) {
            log.error("Error occurred while sending SMS to " + sms.getMobileNumber(), e);
            throw e;
        }
    }

}
