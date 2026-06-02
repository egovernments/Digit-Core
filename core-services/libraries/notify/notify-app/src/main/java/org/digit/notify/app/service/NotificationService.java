package org.digit.notify.app.service;

import com.github.f4b6a3.ulid.UlidCreator;
import org.digit.notify.app.dispatch.DispatchEngine;
import org.digit.notify.app.domain.entity.NotificationAttemptEntity;
import org.digit.notify.app.domain.entity.NotificationConfigEntity;
import org.digit.notify.app.domain.entity.NotificationLogEntity;
import org.digit.notify.app.domain.entity.ProviderEntity;
import org.digit.notify.app.domain.entity.ProviderMappingEntity;
import org.digit.notify.app.domain.repository.*;
import org.digit.notify.app.exception.DuplicateConfigException;
import org.digit.notify.app.exception.DuplicateMappingException;
import org.digit.notify.app.exception.EntityNotFoundException;
import org.digit.notify.app.exception.ValidationException;
import org.digit.notify.app.model.ChannelDispatchStatus;
import org.digit.notify.app.model.NotifyRequest;
import org.digit.notify.app.model.NotifyResponse;
import org.digit.notify.spi.Recipient;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationConfigRepository configRepository;
    private final ProviderMappingRepository mappingRepository;
    private final ProviderRepository providerRepository;
    private final NotificationLogRepository logRepository;
    private final NotificationAttemptRepository attemptRepository;
    private final DispatchEngine dispatchEngine;

    public NotificationService(
        NotificationConfigRepository configRepository,
        ProviderMappingRepository mappingRepository,
        ProviderRepository providerRepository,
        NotificationLogRepository logRepository,
        NotificationAttemptRepository attemptRepository,
        DispatchEngine dispatchEngine
    ) {
        this.configRepository = configRepository;
        this.mappingRepository = mappingRepository;
        this.providerRepository = providerRepository;
        this.logRepository = logRepository;
        this.attemptRepository = attemptRepository;
        this.dispatchEngine = dispatchEngine;
    }

    @Transactional
    public NotifyResponse sendNotification(NotifyRequest request, String tenantId) {
        var config = configRepository
            .findByTenantIdAndTemplateCodeAndIsActiveTrue(tenantId, request.templateCode())
            .orElseThrow(() -> new EntityNotFoundException("NotificationConfig", request.templateCode()));

        var outcome = dispatchEngine.dispatch(request, config, tenantId);

        String notificationId = "ntf_" + UlidCreator.getUlid().toString();

        var log = new NotificationLogEntity();
        log.setNotificationId(notificationId);
        log.setTenantId(tenantId);
        log.setTemplateCode(request.templateCode());
        log.setRecipientRef(resolveRecipientRef(request.recipient()));
        log.setCreatedAt(Instant.now());
        logRepository.save(log);

        var attemptEntities = outcome.attempts().stream().map(a -> {
            var entity = new NotificationAttemptEntity();
            entity.setNotificationId(notificationId);
            entity.setChannel(a.channel().name());
            entity.setProviderName(a.providerName());
            entity.setAttemptNo(a.attemptNo());
            entity.setStatus(a.status().name());
            entity.setReason(a.reason());
            entity.setAttemptedAt(a.attemptedAt());
            return entity;
        }).toList();
        attemptRepository.saveAll(attemptEntities);

        var channelStatuses = outcome.results().stream().map(r ->
            new ChannelDispatchStatus(r.channel().name(), r.status().name(), r.providerName(), r.reason())
        ).toList();

        return new NotifyResponse(notificationId, request.templateCode(), channelStatuses);
    }

    private String resolveRecipientRef(Recipient recipient) {
        if (recipient.email() != null) return recipient.email();
        if (recipient.phone() != null) return recipient.phone();
        return "unknown";
    }

    @Transactional
    public NotificationConfigEntity createConfig(NotificationConfigEntity entity, String tenantId) {
        configRepository.findByTenantIdAndTemplateCode(tenantId, entity.getTemplateCode())
            .stream().findAny().ifPresent(existing -> {
                throw new DuplicateConfigException(tenantId, entity.getTemplateCode());
            });
        entity.setTenantId(tenantId);
        entity.setActive(true);
        entity.getAuditDetail().setCreatedTime(Instant.now());
        return configRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public NotificationConfigEntity getConfig(UUID id, String tenantId) {
        return configRepository.findById(id)
            .filter(c -> c.getTenantId().equals(tenantId))
            .orElseThrow(() -> new EntityNotFoundException("NotificationConfig", id.toString()));
    }

    @Transactional(readOnly = true)
    public List<NotificationConfigEntity> listConfigs(
        String tenantId, @Nullable String templateCode, @Nullable Boolean isActive
    ) {
        if (templateCode != null) {
            return configRepository.findByTenantIdAndTemplateCode(tenantId, templateCode);
        }
        if (isActive != null) {
            return configRepository.findByTenantIdAndIsActive(tenantId, isActive);
        }
        return configRepository.findByTenantId(tenantId);
    }

    @Transactional
    public NotificationConfigEntity updateConfig(UUID id, NotificationConfigEntity updated, String tenantId) {
        var existing = getConfig(id, tenantId);
        existing.setTemplateCode(updated.getTemplateCode());
        existing.setChannels(updated.getChannels());
        existing.setActive(updated.isActive());
        existing.getAuditDetail().setLastModifiedTime(Instant.now());
        return configRepository.save(existing);
    }

    @Transactional
    public void deleteConfig(UUID id, String tenantId) {
        configRepository.delete(getConfig(id, tenantId));
    }

    @Transactional
    public ProviderMappingEntity createMapping(ProviderMappingEntity entity, String tenantId) {
        if (entity.getCountry() != null) {
            mappingRepository.findByTenantIdAndChannelAndCountry(
                tenantId, entity.getChannel(), entity.getCountry()
            ).ifPresent(e -> {
                throw new DuplicateMappingException(tenantId, entity.getChannel(), entity.getCountry());
            });
        } else {
            mappingRepository.findByTenantIdAndChannelAndCountryIsNull(
                tenantId, entity.getChannel()
            ).ifPresent(e -> {
                throw new DuplicateMappingException(tenantId, entity.getChannel(), null);
            });
        }

        entity.getProviders().forEach(name ->
            providerRepository.findByProviderName(name).orElseThrow(() ->
                new ValidationException("Provider '" + name + "' not found in registry"))
        );

        entity.setTenantId(tenantId);
        entity.getAuditDetail().setCreatedTime(Instant.now());
        return mappingRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public List<ProviderMappingEntity> listMappings(String tenantId, @Nullable String channel) {
        if (channel != null) {
            return mappingRepository.findByTenantIdAndChannel(tenantId, channel);
        }
        return mappingRepository.findAll().stream()
            .filter(m -> m.getTenantId().equals(tenantId))
            .toList();
    }

    @Transactional
    public ProviderMappingEntity updateMapping(UUID id, ProviderMappingEntity updated, String tenantId) {
        var existing = mappingRepository.findById(id)
            .filter(m -> m.getTenantId().equals(tenantId))
            .orElseThrow(() -> new EntityNotFoundException("ProviderMapping", id.toString()));
        existing.setProviders(updated.getProviders());
        existing.getAuditDetail().setLastModifiedTime(Instant.now());
        return mappingRepository.save(existing);
    }

    @Transactional
    public void deleteMapping(UUID id, String tenantId) {
        var existing = mappingRepository.findById(id)
            .filter(m -> m.getTenantId().equals(tenantId))
            .orElseThrow(() -> new EntityNotFoundException("ProviderMapping", id.toString()));
        mappingRepository.delete(existing);
    }

    @Transactional(readOnly = true)
    public List<ProviderEntity> listProviders(@Nullable String channel, @Nullable Boolean isActive) {
        if (isActive != null) {
            return providerRepository.findByIsActive(isActive);
        }
        return providerRepository.findAll();
    }

    @Transactional
    public ProviderEntity updateProviderStatus(UUID id, boolean isActive) {
        var entity = providerRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Provider", id.toString()));
        entity.setActive(isActive);
        entity.getAuditDetail().setLastModifiedTime(Instant.now());
        return providerRepository.save(entity);
    }
}
