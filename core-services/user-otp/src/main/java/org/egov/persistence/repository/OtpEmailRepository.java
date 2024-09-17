package org.egov.persistence.repository;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.domain.model.OtpRequest;
import org.egov.domain.model.OtpRequestType;
import org.egov.domain.service.LocalizationService;
import org.egov.persistence.contract.Email;
import org.egov.persistence.contract.EmailRequest;
import org.egov.tracer.kafka.CustomKafkaTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Collections;
import java.util.Map;

import static java.lang.String.format;

@Service
@Slf4j
public class OtpEmailRepository {

	private static final String LOCALIZATION_KEY_LOGIN_SUBJECT_EMAIL = "email.login.otp.sub";
	private static final String LOCALIZATION_KEY_LOGIN_BODY_EMAIL = "email.login.otp.body";
	private static final String LOCALIZATION_KEY_PWD_RESET_SUBJECT_EMAIL = "email.pwd.reset.otp.sub";
	private static final String LOCALIZATION_KEY_PWD_RESET_BODY_EMAIL = "email.pwd.reset.otp.body";
	private static final String PWD_RESET_SUBJECT_EMAIL = 	"Password Reset";
	private static final String PWD_RESET_BODY_EMAIL = "Your OTP for recovering password is %s.";
	private static final String LOGIN_SUBJECT_EMAIL = "Login OTP";
	private static final String LOGIN_BODY_EMAIL = "Dear Citizen, Your Login OTP is %s.";
    private CustomKafkaTemplate<String, EmailRequest> kafkaTemplate;
    private String emailTopic;

	private LocalizationService localizationService;
	@Value("${egov.localisation.tenantid.strip.suffix.count}")
	private int tenantIdStripSuffixCount;

	@Autowired
	private MultiStateInstanceUtil centralInstanceUtil;

    @Autowired
    public OtpEmailRepository(CustomKafkaTemplate<String, EmailRequest> kafkaTemplate,
							  @Value("${email.topic}") String emailTopic, LocalizationService localizationService) {
        this.kafkaTemplate = kafkaTemplate;
        this.emailTopic = emailTopic;
		this.localizationService = localizationService;
    }

    public void send(String emailId, String otpNumber, OtpRequest otpRequest) {
    	if (emailId == null || emailId.isEmpty()) {
			return;
		}
		sendEmail(emailId, otpNumber, otpRequest);
    }

	private void sendEmail(String emailId, String otpNumber, OtpRequest otpRequest) {
		Email email = Email.builder()
			.body(getBody(otpNumber,otpRequest))
			.subject(getSubject(otpRequest))
			.emailTo(Collections.singleton(emailId))
			.build();
		EmailRequest emailRequest = EmailRequest.builder().requestInfo(RequestInfo.builder().build()).email(email).build();
		String updatedTopic = centralInstanceUtil.getStateSpecificTopicName(otpRequest.getTenantId(), emailTopic);
		kafkaTemplate.send(updatedTopic, emailRequest);
	}

	private String getLocale(OtpRequest otpRequest){
		String locale;
		if(otpRequest.getRequestInfo() != null && otpRequest.getRequestInfo().getMsgId() != null && otpRequest.getRequestInfo().getMsgId().contains("|"))
		{
			locale = otpRequest.getRequestInfo().getMsgId().split("|")[1];
		}
		else {
			locale = "en_IN";
		}
		return locale;
	}

	private String getMessages(OtpRequest otpRequest, String localizationKey){
		String tenantId = getRequiredTenantId(otpRequest.getTenantId());
		String locale = getLocale(otpRequest);
		Map<String, String> localisedMessages = localizationService.getLocalisedMessages(tenantId, locale, "egov-user");
		if (localisedMessages.isEmpty()) {
			log.info("Localization Service didn't return any Subject so using default...");
			localisedMessages.put(LOCALIZATION_KEY_PWD_RESET_SUBJECT_EMAIL, "Password Reset");
			localisedMessages.put(LOCALIZATION_KEY_PWD_RESET_BODY_EMAIL, "Your OTP for recovering password is %s.");
			localisedMessages.put(LOCALIZATION_KEY_LOGIN_SUBJECT_EMAIL, "Login OTP");
			localisedMessages.put(LOCALIZATION_KEY_LOGIN_BODY_EMAIL, "Dear Citizen, Your Login OTP is %s.");
		}
		return localisedMessages.get(localizationKey);
	}

	private String getSubject(OtpRequest otpRequest) {
		String subject;
		if(otpRequest.getType() == OtpRequestType.PASSWORD_RESET){
			subject = getMessages(otpRequest, LOCALIZATION_KEY_PWD_RESET_SUBJECT_EMAIL);
			if(ObjectUtils.isEmpty(subject))
				subject = PWD_RESET_SUBJECT_EMAIL;
		}
		else {
			subject = getMessages(otpRequest, LOCALIZATION_KEY_LOGIN_SUBJECT_EMAIL);
			if(ObjectUtils.isEmpty(subject))
				subject = LOGIN_SUBJECT_EMAIL;
		}
		return subject;
	}

	private String getBody(String otpNumber, OtpRequest otpRequest) {
		String body;
		if (otpRequest.getType() == OtpRequestType.PASSWORD_RESET){
			body = getMessages(otpRequest, LOCALIZATION_KEY_PWD_RESET_BODY_EMAIL);
			if(ObjectUtils.isEmpty(body))
				body = PWD_RESET_BODY_EMAIL;
			body = format(body, otpNumber);
		}
		else {
			body = getMessages(otpRequest, LOCALIZATION_KEY_LOGIN_BODY_EMAIL);
			if(ObjectUtils.isEmpty(body))
				body = LOGIN_BODY_EMAIL;
			body = format(body, otpNumber);
		}
		return body;
	}


	private String getRequiredTenantId(String tenantId) {
		String[] tenantList = tenantId.split("\\.");
		if(tenantIdStripSuffixCount>0 && tenantIdStripSuffixCount<tenantList.length) {    // handeled case if tenantIdStripSuffixCount
			int cutIndex = tenantList.length - tenantIdStripSuffixCount;                  // is in between 0 and tenantList size
			String requriedTenantId = tenantList[0];                                      // (excluding 0 & tenantList size)
			for(int idx =1; idx<cutIndex; idx++)
				requriedTenantId = requriedTenantId + "." + tenantList[idx];

			return requriedTenantId;
		}
		else if(tenantIdStripSuffixCount>=tenantList.length)                              // handled case if tenantIdStripSuffixCount
			return tenantList[0];                                                         // is greater than or equal to tenantList size
		else
			return tenantId;                                                              // handled case if tenantIdStripSuffixCount
		// is less than or equal to 0
	}
}
