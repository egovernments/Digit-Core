package org.egov.infra.mdms.requestvalidation;

import io.digit.requestvalidation.autoconfigure.RequestValidationAutoConfiguration;
import org.egov.infra.mdms.controller.MDMSControllerV2;
import org.egov.infra.mdms.service.MDMSServiceV2;
import org.egov.tracer.model.CustomException;
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

/** The real controller, annotations and request validation; only the MDMS service is mocked. */
@WebMvcTest(controllers = MDMSControllerV2.class)
@ContextConfiguration(classes = {MDMSControllerV2.class, MdmsMarkupExemption.class,
        MdmsMarkupExemptionWebTest.ErrorAdvice.class})
@ImportAutoConfiguration(RequestValidationAutoConfiguration.class)
@TestPropertySource(properties = {
    "egov.request-validation.enabled=true",
    "egov.request-validation.structured-default=true",
    "egov.request-validation.mode=ENFORCE",
    "egov.mdms.markup.html-fields=EXPENSE.billEmailNotification:/Mdms/data/htmlBody",
    "egov.mdms.markup.type-names=List<String>"
})
class MdmsMarkupExemptionWebTest {
    private static final String EMAIL = "EXPENSE.billEmailNotification";
    private static final String TEMPLATE = "<!DOCTYPE html><html><body><p style='font-family:Arial,sans-serif;color:#333;"
            + "font-size:14px'>Dear {userName},</p><p>You have <strong>{billCount}</strong> bill(s) for <strong>"
            + "{campaignName}</strong>.</p><br/></body></html>";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private MDMSServiceV2 mdmsServiceV2;

    @Test
    void emailTemplatesAndTypeNamesAreAcceptedOnCreateAndUpdate() throws Exception {
        for (String action : new String[] {"_create", "_update"}) {
            send(action, EMAIL, "{\"subject\":\"Bill approval\",\"htmlBody\":\"" + TEMPLATE + "\"}", HttpStatus.ACCEPTED);
            send(action, "HCM-ADMIN-CONSOLE.AppFieldType",
                    "{\"type\":\"checkbox\",\"metadata\":{\"formDataType\":\"List<String>\"}}", HttpStatus.ACCEPTED);
            send(action, "HCM.REGISTRATION_DELIVERY",
                    "{\"components\":[{\"attributes\":[{\"formDataType\":\"List<String>\"}]}]}", HttpStatus.ACCEPTED);
        }
    }

    @Test
    void schemaCodeMayFollowTheData() throws Exception {
        mvc.perform(post("/v2/_create/" + EMAIL).contentType("application/json").content(
                "{\"RequestInfo\":{},\"Mdms\":{\"tenantId\":\"chad\",\"uniqueIdentifier\":\"x\",\"data\":{\"htmlBody\":\""
                        + TEMPLATE + "\"},\"schemaCode\":\"" + EMAIL + "\"}}"))
                .andExpect(status().isAccepted());
    }

    @Test
    void otherSchemasFieldsTypeNamesAndUnsafeMarkupAreStillRejected() throws Exception {
        send("_create", "EXPENSE.Other", "{\"htmlBody\":\"" + TEMPLATE + "\"}", HttpStatus.BAD_REQUEST);
        send("_create", EMAIL, "{\"subject\":\"" + TEMPLATE + "\"}", HttpStatus.BAD_REQUEST);
        send("_update", EMAIL, "{\"htmlBody\":\"" + TEMPLATE + "<img src=x onerror=alert(1)>\"}", HttpStatus.BAD_REQUEST);
        send("_create", EMAIL, "{\"htmlBody\":\"<script>alert(1)</script>\"}", HttpStatus.BAD_REQUEST);
        send("_create", EMAIL, "{\"htmlBody\":{\"nested\":\"<b>x</b>\"}}", HttpStatus.BAD_REQUEST);
        send("_create", "HCM-ADMIN-CONSOLE.AppFieldType", "{\"metadata\":{\"formDataType\":\"<script>\"}}",
                HttpStatus.BAD_REQUEST);
        send("_create", "HCM-ADMIN-CONSOLE.AppFieldType", "{\"metadata\":{\"formDataType\":\"List<b>\"}}",
                HttpStatus.BAD_REQUEST);
        send("_create", "HCM-ADMIN-CONSOLE.AppFieldType", "{\"metadata\":{\"type\":\"List<String>\"}}",
                HttpStatus.BAD_REQUEST);
        send("_create", "HCM.TransformTemplate", "{\"Fields\":[{\"default\":\"<DirEntry 'x.xlsx'>\"}]}",
                HttpStatus.BAD_REQUEST);
        // Markup outside data, or a schemaCode that itself carries markup, is rejected.
        mvc.perform(post("/v2/_create/x").contentType("application/json").content(
                "{\"RequestInfo\":{},\"Mdms\":{\"tenantId\":\"chad\",\"schemaCode\":\"<b>x</b>\",\"uniqueIdentifier\":\"x\","
                        + "\"data\":{\"htmlBody\":\"" + TEMPLATE + "\"}}}"))
                .andExpect(status().isBadRequest());
    }

    private void send(String action, String schemaCode, String data, HttpStatus expected) throws Exception {
        mvc.perform(post("/v2/" + action + "/" + schemaCode).contentType("application/json").content(
                "{\"RequestInfo\":{},\"Mdms\":{\"tenantId\":\"chad\",\"schemaCode\":\"" + schemaCode
                        + "\",\"uniqueIdentifier\":\"x\",\"data\":" + data + ",\"isActive\":true}}"))
                .andExpect(status().is(expected.value()))
                .andExpect(expected == HttpStatus.ACCEPTED ? status().isAccepted()
                        : content().string("REQUEST_CONTENT_NOT_ALLOWED"));
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
