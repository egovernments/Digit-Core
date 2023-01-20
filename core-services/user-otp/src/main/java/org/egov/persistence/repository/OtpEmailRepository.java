package org.egov.persistence.repository;

import org.egov.common.contract.request.RequestInfo;
import org.egov.domain.model.OtpRequestType;
import org.egov.persistence.contract.Email;
import org.egov.persistence.contract.EmailRequest;
import org.egov.tracer.kafka.CustomKafkaTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Service
public class OtpEmailRepository {
    private static final String PASSWORD_RESET_SUBJECT = "Password Reset";
    private static final String PASSWORD_RESET_BODY = "Your OTP for recovering password is %s.";
	private static final String LOGIN_OTP_SUBJECT = "Login OTP";
	private static final String LOGIN_OTP_BODY = "Dear Citizen, Your Login OTP is %s.";
	private static final String REGISTRATION_OTP_SUBJECT = "Registration";
	private static final String REGISTRATION_OTP_BODY = "Dear Citizen, Your OTP to complete your mSeva Registration is %s.";
    private CustomKafkaTemplate<String, EmailRequest> kafkaTemplate;
    private String emailTopic;


    @Autowired
    public OtpEmailRepository(CustomKafkaTemplate<String, EmailRequest> kafkaTemplate,
							  @Value("${email.topic}") String emailTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.emailTopic = emailTopic;
    }

    public void send(String emailId, String otpNumber, OtpRequestType otpRequestType) {
    	if (isEmpty(emailId)) {
			return;
		}
		sendEmail(emailId, otpNumber, otpRequestType);
    }

	private void sendEmail(String emailId, String otpNumber, OtpRequestType otpRequestType) {
		final Email email = Email.builder()
				.body(getBody(otpNumber,otpRequestType))
				.subject(getSubject(otpRequestType))
				.emailTo(Collections.singleton(emailId))
				.build();
		EmailRequest emailRequest = EmailRequest.builder().requestInfo(RequestInfo.builder().build()).email(email).build();
		kafkaTemplate.send(emailTopic, emailRequest);
	}

	private String getSubject(OtpRequestType otpRequestType) {
		String subject;
		if(otpRequestType == OtpRequestType.PASSWORD_RESET){
			subject = PASSWORD_RESET_SUBJECT;
		}
		else if(otpRequestType == OtpRequestType.LOGIN){
			subject = LOGIN_OTP_SUBJECT;
		}
		else{
			subject = REGISTRATION_OTP_SUBJECT;
		}
		return subject;
	}

	private String getBody(String otpNumber, OtpRequestType otpRequestType) {
		String body;
		if (otpRequestType == OtpRequestType.PASSWORD_RESET){
			body = format(PASSWORD_RESET_BODY, otpNumber);
		}
		else if(otpRequestType == OtpRequestType.LOGIN){
			body = format(LOGIN_OTP_BODY, otpNumber);
		}
		else{
			body = format(REGISTRATION_OTP_BODY, otpNumber);
		}
		return body;
	}

}
