package org.digit.notify.app.controller.dto;

import java.util.List;
import java.util.UUID;

public record ProviderMappingResponseDto(
    UUID id,
    String tenantId,
    String channel,
    String country,
    List<String> providers
) {}
