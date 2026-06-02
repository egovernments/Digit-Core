package org.digit.notify.app.controller;

import org.digit.notify.app.controller.dto.ProviderResponseDto;
import org.digit.notify.app.controller.dto.ProviderStatusRequestDto;
import org.digit.notify.app.controller.mapper.ProviderMapper;
import org.digit.notify.app.service.NotificationService;
import org.jspecify.annotations.Nullable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/providers")
public class ProviderController {

    private final NotificationService service;
    private final ProviderMapper mapper;

    public ProviderController(NotificationService service, ProviderMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    public List<ProviderResponseDto> list(
        @RequestParam @Nullable String channel,
        @RequestParam @Nullable Boolean isActive
    ) {
        return service.listProviders(channel, isActive).stream().map(mapper::toDto).toList();
    }

    @PatchMapping("/{id}/status")
    public ProviderResponseDto updateStatus(
        @PathVariable UUID id,
        @RequestBody ProviderStatusRequestDto dto
    ) {
        return mapper.toDto(service.updateProviderStatus(id, dto.isActive()));
    }
}
