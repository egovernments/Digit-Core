package org.digit.notify.app.domain.repository;

import org.digit.notify.app.domain.entity.*;
import org.digit.notify.app.domain.entity.config.ChannelConfig;
import org.digit.notify.app.domain.entity.config.ChannelsConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
    "notify.plugins.directory=./providers",
    "spring.flyway.enabled=true",
    "spring.jpa.hibernate.ddl-auto=validate"
})
@Testcontainers
class RepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("notify")
        .withUsername("notify")
        .withPassword("notify");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.url", postgres::getJdbcUrl);
        registry.add("spring.flyway.user", postgres::getUsername);
        registry.add("spring.flyway.password", postgres::getPassword);
    }

    @Autowired
    NotificationConfigRepository configRepository;

    @Autowired
    ProviderMappingRepository mappingRepository;

    @Autowired
    NotificationLogRepository logRepository;

    @Autowired
    NotificationAttemptRepository attemptRepository;

    @Test
    void saveAndRetrieveNotificationConfig_jsonbRoundTrips() {
        var channelConfig = new ChannelConfig();
        channelConfig.setEnabled(true);
        channelConfig.setPriority("HIGH");
        channelConfig.setBody(Map.of("en", "Your OTP is {{otp}}", "default", "Your OTP is {{otp}}"));
        channelConfig.setPayloadBindings(Map.of("otp", "$.data.otp_code"));

        var channels = new ChannelsConfig();
        channels.setSms(channelConfig);

        var entity = new NotificationConfigEntity();
        entity.setTenantId("tenant-1");
        entity.setTemplateCode("OTP_SMS");
        entity.setActive(true);
        entity.setChannels(channels);
        entity.getAuditDetail().setCreatedTime(Instant.now());

        var saved = configRepository.save(entity);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getChannels()).isNotNull();
        assertThat(saved.getChannels().getSms()).isNotNull();
        assertThat(saved.getChannels().getSms().isEnabled()).isTrue();
        assertThat(saved.getChannels().getSms().getBody()).containsKey("en");
    }

    @Test
    void findByTenantIdAndTemplateCodeAndIsActiveTrue_returnsCorrectly() {
        var channels = new ChannelsConfig();
        var sms = new ChannelConfig();
        sms.setEnabled(true);
        sms.setBody(Map.of("default", "Hello"));
        channels.setSms(sms);

        var active = new NotificationConfigEntity();
        active.setTenantId("t1");
        active.setTemplateCode("TEMPLATE_A");
        active.setActive(true);
        active.setChannels(channels);
        configRepository.save(active);

        var inactive = new NotificationConfigEntity();
        inactive.setTenantId("t1");
        inactive.setTemplateCode("TEMPLATE_B");
        inactive.setActive(false);
        inactive.setChannels(channels);
        configRepository.save(inactive);

        assertThat(configRepository.findByTenantIdAndTemplateCodeAndIsActiveTrue("t1", "TEMPLATE_A"))
            .isPresent();
        assertThat(configRepository.findByTenantIdAndTemplateCodeAndIsActiveTrue("t1", "TEMPLATE_B"))
            .isEmpty();
    }

    @Test
    void saveProviderMapping_providersListRoundTrips() {
        var entity = new ProviderMappingEntity();
        entity.setTenantId("t1");
        entity.setChannel("SMS");
        entity.setProviders(List.of("twilio", "kaleyra"));
        entity.getAuditDetail().setCreatedTime(Instant.now());

        var saved = mappingRepository.save(entity);

        assertThat(saved.getProviders()).containsExactly("twilio", "kaleyra");
    }

    @Test
    void findByTenantIdAndChannelAndCountryIsNull_returnsGlobalMapping() {
        var global = new ProviderMappingEntity();
        global.setTenantId("t2");
        global.setChannel("SMS");
        global.setCountry(null);
        global.setProviders(List.of("kaleyra"));
        mappingRepository.save(global);

        var countrySpecific = new ProviderMappingEntity();
        countrySpecific.setTenantId("t2");
        countrySpecific.setChannel("SMS");
        countrySpecific.setCountry("IN");
        countrySpecific.setProviders(List.of("twilio"));
        mappingRepository.save(countrySpecific);

        var found = mappingRepository.findByTenantIdAndChannelAndCountryIsNull("t2", "SMS");
        assertThat(found).isPresent();
        assertThat(found.get().getProviders()).containsExactly("kaleyra");
    }

    @Test
    void saveNotificationLogAndAttempts_findByNotificationIdReturnsBoth() {
        var log = new NotificationLogEntity();
        log.setNotificationId("ntf_test123");
        log.setTenantId("t1");
        log.setTemplateCode("OTP");
        log.setCreatedAt(Instant.now());
        logRepository.save(log);

        var attempt1 = new NotificationAttemptEntity();
        attempt1.setNotificationId("ntf_test123");
        attempt1.setChannel("SMS");
        attempt1.setProviderName("twilio");
        attempt1.setAttemptNo(1);
        attempt1.setStatus("DISPATCHED");
        attempt1.setAttemptedAt(Instant.now());
        attemptRepository.save(attempt1);

        var attempt2 = new NotificationAttemptEntity();
        attempt2.setNotificationId("ntf_test123");
        attempt2.setChannel("EMAIL");
        attempt2.setProviderName("sendgrid");
        attempt2.setAttemptNo(1);
        attempt2.setStatus("DISPATCHED");
        attempt2.setAttemptedAt(Instant.now());
        attemptRepository.save(attempt2);

        var attempts = attemptRepository.findByNotificationId("ntf_test123");
        assertThat(attempts).hasSize(2);
    }
}
