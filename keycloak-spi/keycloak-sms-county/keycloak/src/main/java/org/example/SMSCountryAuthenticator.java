package org.example;

import org.example.utils.KafkaProducerUtil;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SMSCountryAuthenticator implements Authenticator {

    private static final Logger log = LoggerFactory.getLogger(SMSCountryAuthenticator.class);


    @Override
    public void authenticate(AuthenticationFlowContext context) {
        // Generate OTP
        String otp = String.valueOf(new Random().nextInt(900000) + 100000);
        log.warn("OTP Generated: "+otp);
        // Send OTP via SMS Country
        String mobileNumber = context.getUser().getFirstAttribute("mobileNumber");
        if (sendSMS(mobileNumber, otp)) {
            // Store OTP in the authentication session
            context.getAuthenticationSession().setAuthNote("otp", otp);

            // Prepare data for the FreeMarker template
            Map<String, Object> otpLogin = new HashMap<>();
            otpLogin.put("userOtpCredentials", new String[] {"SMS"});
            otpLogin.put("mobileNumber", mobileNumber);

            // Render the OTP form
            Response challenge = context.form()
                    .setAttribute("otpLogin", otpLogin)
                    .createForm("login-otp.ftl");
            context.challenge(challenge);
        } else {
            context.failure(AuthenticationFlowError.INTERNAL_ERROR);
        }
    }

    private boolean sendSMS(String mobileNumber, String otp) {
        try {

            String message = String.format(
                    "Dear Citizen, Your Login OTP is %s",
                    otp
            );
            log.info("Running in simulation mode. OTP is : %s",otp);
            Map<String, Object> smsRequest = new HashMap<>();
            smsRequest.put("mobileNumber", mobileNumber);
            smsRequest.put("message", message);
            KafkaProducerUtil kafkaUtil = KafkaProducerUtil.getInstance();
            kafkaUtil.sendSmsMessage(null,smsRequest);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        String enteredOtp = context.getHttpRequest().getDecodedFormParameters().getFirst("otp");
        String expectedOtp = context.getAuthenticationSession().getAuthNote("otp");

        if (expectedOtp != null && expectedOtp.equals(enteredOtp)) {
            context.success();
        } else {
            context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS,
                    context.form().setError("Invalid OTP").createForm("login-otp.ftl"));
        }
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        // Check if the user has a phone number attribute
        return user.getFirstAttribute("mobileNumber") != null;
    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public void close() {
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        // No actions required
    }


}