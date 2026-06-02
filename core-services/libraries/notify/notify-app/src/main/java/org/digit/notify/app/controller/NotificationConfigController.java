package org.digit.notify.app.controller;

import jakarta.validation.Valid;
import org.digit.notify.app.controller.dto.NotificationConfigRequestDto;
import org.digit.notify.app.controller.dto.NotificationConfigResponseDto;
import org.digit.notify.app.controller.mapper.NotificationConfigMapper;
import org.digit.notify.app.service.NotificationService;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/notification-configs")
public class NotificationConfigController {

    private final NotificationService service;
    private final NotificationConfigMapper mapper;

    public NotificationConfigController(NotificationService service, NotificationConfigMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationConfigResponseDto create(
        @RequestHeader("X-Tenant-ID") String tenantId,
        @RequestBody @Valid NotificationConfigRequestDto dto
    ) {
        var entity = mapper.toEntity(dto);
        return mapper.toDto(service.createConfig(entity, tenantId));
    }

    @GetMapping
    public List<NotificationConfigResponseDto> list(
        @RequestHeader("X-Tenant-ID") String tenantId,
        @RequestParam @Nullable String templateCode,
        @RequestParam @Nullable Boolean isActive
    ) {
        return service.listConfigs(tenantId, templateCode, isActive)
            .stream().map(mapper::toDto).toList();
    }

    @GetMapping("/{id}")
    public NotificationConfigResponseDto get(
        @RequestHeader("X-Tenant-ID") String tenantId,
        @PathVariable UUID id
    ) {
        return mapper.toDto(service.getConfig(id, tenantId));
    }

    @PutMapping("/{id}")
    public NotificationConfigResponseDto update(
        @RequestHeader("X-Tenant-ID") String tenantId,
        @PathVariable UUID id,
        @RequestBody @Valid NotificationConfigRequestDto dto
    ) {
        var entity = mapper.toEntity(dto);
        return mapper.toDto(service.updateConfig(id, entity, tenantId));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
        @RequestHeader("X-Tenant-ID") String tenantId,
        @PathVariable UUID id
    ) {
        service.deleteConfig(id, tenantId);
    }
}
