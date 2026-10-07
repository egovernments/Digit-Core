package org.egov.web.requestvalidation;

import io.digit.requestvalidation.autoconfigure.RequestValidationAutoConfiguration;
import org.egov.domain.service.MessageService;
import org.egov.tracer.model.CustomException;
import org.egov.web.controller.MessageController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The real controller, annotations and request validation; only the message service is mocked. */
@WebMvcTest(controllers = MessageController.class)
@ContextConfiguration(classes = {MessageController.class, MessageMarkupExemption.class,
        MessageMarkupExemptionWebTest.ErrorAdvice.class})
@ImportAutoConfiguration(RequestValidationAutoConfiguration.class)
@TestPropertySource(properties = {
    "egov.request-validation.enabled=true",
    "egov.request-validation.structured-default=true",
    "egov.request-validation.mode=ENFORCE",
    "egov.localization.markup.allowed-codes=HEALTH_HRMS_EMAIL_CODE, USER_CREDENTIAL_BODY_DETAILS"
})
class MessageMarkupExemptionWebTest {
    private static final String TEMPLATE = "<h2>Console login credentials</h2><p>Hi {User's name},<br>URL: {website URL}"
            + "<br>Username: {Username}<br>Thank you,<br>{Implementation partner}</p>";
    private static final String LINKS = "<strong>Campaign:</strong> {campaignName}<br/><a href=\\\"{accessLink}\\\">Access Link</a>";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private MessageService messageService;

    @Test
    void listedCodesAcceptSafeTemplatesOnEveryWriteEndpoint() throws Exception {
        for (String endpoint : new String[] {"/messages/v1/_upsert", "/messages/v1/_create"}) {
            send(endpoint, create("HEALTH_HRMS_EMAIL_CODE", TEMPLATE), HttpStatus.OK);
            send(endpoint, create("USER_CREDENTIAL_BODY_DETAILS", LINKS), HttpStatus.OK);
        }
        send("/messages/v1/_update", update("HEALTH_HRMS_EMAIL_CODE", TEMPLATE), HttpStatus.OK);
    }

    @Test
    void codeMayFollowTheMessage() throws Exception {
        send("/messages/v1/_upsert", "{\"RequestInfo\":{\"userInfo\":{\"id\":1}},\"tenantId\":\"chad\",\"messages\":[{"
                + "\"message\":\"" + TEMPLATE + "\",\"module\":\"rainmaker-hr\",\"locale\":\"en_CHAD\","
                + "\"code\":\"HEALTH_HRMS_EMAIL_CODE\"}]}", HttpStatus.OK);
    }

    @Test
    void otherCodesFieldsAndUnsafeMarkupAreStillRejected() throws Exception {
        send("/messages/v1/_upsert", create("SOME_OTHER_CODE", TEMPLATE), HttpStatus.BAD_REQUEST);
        send("/messages/v1/_update", update("SOME_OTHER_CODE", TEMPLATE), HttpStatus.BAD_REQUEST);
        send("/messages/v1/_upsert", create("HEALTH_HRMS_EMAIL_CODE", TEMPLATE + "<img src=x onerror=alert(1)>"),
                HttpStatus.BAD_REQUEST);
        send("/messages/v1/_create", create("HEALTH_HRMS_EMAIL_CODE", "<script>alert(1)</script>"), HttpStatus.BAD_REQUEST);
        send("/messages/v1/_update", update("HEALTH_HRMS_EMAIL_CODE", "<a href=\\\"javascript:alert(1)\\\">x</a>"),
                HttpStatus.BAD_REQUEST);
        // The exemption covers the message text only: markup in the module, locale or tenant is rejected.
        send("/messages/v1/_upsert", "{\"RequestInfo\":{\"userInfo\":{\"id\":1}},\"tenantId\":\"chad\",\"messages\":[{"
                + "\"code\":\"HEALTH_HRMS_EMAIL_CODE\",\"message\":\"ok\",\"module\":\"<b>x</b>\",\"locale\":\"en_CHAD\"}]}",
                HttpStatus.BAD_REQUEST);
        // One unlisted message in a batch rejects the request.
        send("/messages/v1/_upsert", "{\"RequestInfo\":{\"userInfo\":{\"id\":1}},\"tenantId\":\"chad\",\"messages\":["
                + "{\"code\":\"HEALTH_HRMS_EMAIL_CODE\",\"message\":\"" + TEMPLATE + "\",\"module\":\"m\",\"locale\":\"en\"},"
                + "{\"code\":\"OTHER\",\"message\":\"<b>x</b>\",\"module\":\"m\",\"locale\":\"en\"}]}", HttpStatus.BAD_REQUEST);
    }

    private void send(String endpoint, String body, HttpStatus expected) throws Exception {
        mvc.perform(post(endpoint).contentType("application/json").content(body))
                .andExpect(status().is(expected.value()))
                .andExpect(expected == HttpStatus.OK ? status().isOk() : content().string("REQUEST_CONTENT_NOT_ALLOWED"));
    }

    private static String create(String code, String message) {
        return "{\"RequestInfo\":{\"userInfo\":{\"id\":1}},\"tenantId\":\"chad\",\"messages\":[{\"code\":\"" + code
                + "\",\"message\":\"" + message + "\",\"module\":\"rainmaker-hr\",\"locale\":\"en_CHAD\"}]}";
    }

    private static String update(String code, String message) {
        return "{\"RequestInfo\":{\"userInfo\":{\"id\":1}},\"tenantId\":\"chad\",\"locale\":\"en_CHAD\","
                + "\"module\":\"rainmaker-hr\",\"messages\":[{\"code\":\"" + code + "\",\"message\":\"" + message + "\"}]}";
    }

    /** Stands in for tracer's handler, which answers CustomException with 400. */
    @RestControllerAdvice
    static class ErrorAdvice {
        @ExceptionHandler(CustomException.class)
        ResponseEntity<String> rejected(CustomException exception) {
            return ResponseEntity.badRequest().body(exception.getCode());
        }
    }
}
