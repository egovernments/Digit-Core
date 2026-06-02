package org.digit.notify.app.controller;

import jakarta.validation.Valid;
import org.digit.notify.app.controller.dto.NotifyRequestDto;
import org.digit.notify.app.controller.dto.NotifyResponseDto;
import org.digit.notify.app.controller.mapper.NotifyMapper;
import org.digit.notify.app.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notify")
public class NotifyController {

    private final NotificationService notificationService;
    private final NotifyMapper mapper;

    public NotifyController(NotificationService notificationService, NotifyMapper mapper) {
        this.notificationService = notificationService;
        this.mapper = mapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public NotifyResponseDto notify(
        @RequestHeader("X-Tenant-ID") String tenantId,
        @RequestBody @Valid NotifyRequestDto requestDto
    ) {
        var request = mapper.toDomain(requestDto);
        var response = notificationService.sendNotification(request, tenantId);
        return mapper.toDto(response);
    }
}
