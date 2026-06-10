package org.egov.web.notification.sms.consumer.contract;

import org.egov.web.notification.sms.models.Category;
import org.egov.web.notification.sms.models.Sms;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SMSRequest {

	private String countryCode;
    private String mobileNumber;

    @Size(max = 1000)
    private String message;
    private Category category;
    private Long expiryTime;

    private String locale;
    private String tenantId;
    private String email;
    private String[] users;

    public Sms toDomain() {
        Category cat = category != null ? category : Category.OTHERS;
        return new Sms(countryCode, mobileNumber, message, cat, expiryTime);
    }
}
