package org.digit.notify.app.controller;

import jakarta.validation.Valid;
import org.digit.notify.app.controller.dto.ProviderMappingRequestDto;
import org.digit.notify.app.controller.dto.ProviderMappingResponseDto;
import org.digit.notify.app.controller.mapper.ProviderMappingMapper;
import org.digit.notify.app.service.NotificationService;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/provider-mappings")
public class ProviderMappingController {

    private final NotificationService service;
    private final ProviderMappingMapper mapper;

    public ProviderMappingController(NotificationService service, ProviderMappingMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProviderMappingResponseDto create(
        @RequestHeader("X-Tenant-ID") String tenantId,
        @RequestBody @Valid ProviderMappingRequestDto dto
    ) {
        var entity = mapper.toEntity(dto);
        return mapper.toDto(service.createMapping(entity, tenantId));
    }

    @GetMapping
    public List<ProviderMappingResponseDto> list(
        @RequestHeader("X-Tenant-ID") String tenantId,
        @RequestParam @Nullable String channel
    ) {
        return service.listMappings(tenantId, channel).stream().map(mapper::toDto).toList();
    }

    @PutMapping("/{id}")
    public ProviderMappingResponseDto update(
        @RequestHeader("X-Tenant-ID") String tenantId,
        @PathVariable UUID id,
        @RequestBody @Valid ProviderMappingRequestDto dto
    ) {
        var entity = mapper.toEntity(dto);
        return mapper.toDto(service.updateMapping(id, entity, tenantId));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
        @RequestHeader("X-Tenant-ID") String tenantId,
        @PathVariable UUID id
    ) {
        service.deleteMapping(id, tenantId);
    }
}
