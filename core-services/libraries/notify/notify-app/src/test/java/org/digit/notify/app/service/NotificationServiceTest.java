package org.digit.notify.app.service;

import org.digit.notify.app.dispatch.DispatchEngine;
import org.digit.notify.app.dispatch.DispatchOutcome;
import org.digit.notify.app.dispatch.AttemptRecord;
import org.digit.notify.app.domain.entity.NotificationConfigEntity;
import org.digit.notify.app.domain.entity.ProviderMappingEntity;
import org.digit.notify.app.domain.repository.*;
import org.digit.notify.app.exception.DuplicateMappingException;
import org.digit.notify.app.exception.EntityNotFoundException;
import org.digit.notify.app.exception.ValidationException;
import org.digit.notify.app.model.NotifyRequest;
import org.digit.notify.spi.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock NotificationConfigRepository configRepository;
    @Mock ProviderMappingRepository mappingRepository;
    @Mock ProviderRepository providerRepository;
    @Mock NotificationLogRepository logRepository;
    @Mock NotificationAttemptRepository attemptRepository;
    @Mock DispatchEngine dispatchEngine;

    @InjectMocks NotificationService service;

    private NotifyRequest sampleRequest() {
        var recipient = new Recipient("+911234567890", null, List.of(), "IN", Map.of());
        return new NotifyRequest("TMPL_001", recipient, Map.of(), null, Map.of());
    }

    @Test
    void sendNotification_happyPath_returnsResponseWithNtfPrefix() {
        var config = new NotificationConfigEntity();
        when(configRepository.findByTenantIdAndTemplateCodeAndIsActiveTrue("t1", "TMPL_001"))
            .thenReturn(Optional.of(config));

        var results = List.of(
            DispatchResult.dispatched(Channel.SMS, "twilio"),
            DispatchResult.dispatched(Channel.EMAIL, "sendgrid"),
            DispatchResult.skipped(Channel.WHATSAPP, "disabled"),
            DispatchResult.skipped(Channel.PUSH, "disabled")
        );
        var attempts = List.of(
            new AttemptRecord(Channel.SMS, "twilio", 1, DispatchStatus.DISPATCHED, null, Instant.now()),
            new AttemptRecord(Channel.EMAIL, "sendgrid", 1, DispatchStatus.DISPATCHED, null, Instant.now())
        );
        when(dispatchEngine.dispatch(any(), any(), any()))
            .thenReturn(new DispatchOutcome(results, attempts));
        when(logRepository.save(any())).thenReturn(null);
        when(attemptRepository.saveAll(any())).thenReturn(List.of());

        var response = service.sendNotification(sampleRequest(), "t1");

        assertThat(response.notificationId()).startsWith("ntf_");
        assertThat(response.channels()).hasSize(4);
        verify(logRepository, times(1)).save(any());
        verify(attemptRepository, times(1)).saveAll(argThat(list ->
            ((List<?>) list).size() == 2));
    }

    @Test
    void sendNotification_configNotFound_throwsEntityNotFoundException() {
        when(configRepository.findByTenantIdAndTemplateCodeAndIsActiveTrue("t1", "TMPL_001"))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.sendNotification(sampleRequest(), "t1"))
            .isInstanceOf(EntityNotFoundException.class);
        verify(dispatchEngine, never()).dispatch(any(), any(), any());
    }

    @Test
    void createMapping_duplicateCountrySpecific_throwsDuplicateMappingException() {
        var existing = new ProviderMappingEntity();
        when(mappingRepository.findByTenantIdAndChannelAndCountry("t1", "SMS", "IN"))
            .thenReturn(Optional.of(existing));

        var entity = new ProviderMappingEntity();
        entity.setChannel("SMS");
        entity.setCountry("IN");
        entity.setProviders(List.of("twilio"));

        assertThatThrownBy(() -> service.createMapping(entity, "t1"))
            .isInstanceOf(DuplicateMappingException.class);
    }

    @Test
    void createMapping_unknownProviderName_throwsValidationException() {
        when(mappingRepository.findByTenantIdAndChannelAndCountry(any(), any(), any()))
            .thenReturn(Optional.empty());
        when(providerRepository.findByProviderName("unknown-provider"))
            .thenReturn(Optional.empty());

        var entity = new ProviderMappingEntity();
        entity.setChannel("SMS");
        entity.setCountry("IN");
        entity.setProviders(List.of("unknown-provider"));

        assertThatThrownBy(() -> service.createMapping(entity, "t1"))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("unknown-provider");
    }
}
