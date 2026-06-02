package org.digit.notify.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.digit.notify.app.exception.EntityNotFoundException;
import org.digit.notify.app.model.ChannelDispatchStatus;
import org.digit.notify.app.model.NotifyResponse;
import org.digit.notify.app.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "notify.plugins.directory=./providers",
    "spring.flyway.enabled=true"
})
@Testcontainers
class NotifyControllerIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("notify").withUsername("notify").withPassword("notify");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.url", postgres::getJdbcUrl);
        registry.add("spring.flyway.user", postgres::getUsername);
        registry.add("spring.flyway.password", postgres::getPassword);
    }

    @Autowired WebApplicationContext wac;
    @Autowired ResponseHeaderFilter responseHeaderFilter;
    final ObjectMapper objectMapper = new ObjectMapper();
    @MockitoBean NotificationService notificationService;

    MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
            .addFilters(responseHeaderFilter)
            .build();
    }

    @Test
    void postNotify_validRequest_returns202WithNotificationId() throws Exception {
        var response = new NotifyResponse("ntf_01ABC123", "OTP_SMS", List.of(
            new ChannelDispatchStatus("SMS", "DISPATCHED", "twilio", null)
        ));
        when(notificationService.sendNotification(any(), eq("tenant-1"))).thenReturn(response);

        var body = Map.of(
            "templateCode", "OTP_SMS",
            "recipient", Map.of("phone", "+911234567890", "deviceTokens", List.of()),
            "payload", Map.of("data", Map.of("otp", "123456"))
        );

        mockMvc.perform(post("/notify")
                .header("X-Tenant-ID", "tenant-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.notificationId").value("ntf_01ABC123"))
            .andExpect(header().exists("X-Response-Time"));
    }

    @Test
    void postNotify_missingTenantIdHeader_returns400() throws Exception {
        var body = Map.of(
            "templateCode", "OTP_SMS",
            "recipient", Map.of("phone", "+911234567890", "deviceTokens", List.of()),
            "payload", Map.of()
        );

        mockMvc.perform(post("/notify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void postNotify_serviceThrowsEntityNotFoundException_returns404() throws Exception {
        when(notificationService.sendNotification(any(), any()))
            .thenThrow(new EntityNotFoundException("NotificationConfig", "OTP_SMS"));

        var body = Map.of(
            "templateCode", "OTP_SMS",
            "recipient", Map.of("phone", "+911234567890", "deviceTokens", List.of()),
            "payload", Map.of()
        );

        mockMvc.perform(post("/notify")
                .header("X-Tenant-ID", "tenant-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }
}
