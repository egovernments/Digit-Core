package org.digit.notify.app.controller.dto;

import java.util.List;
import java.util.UUID;

public record ProviderResponseDto(
    UUID id,
    String providerName,
    List<String> channels,
    boolean isActive
) {}
