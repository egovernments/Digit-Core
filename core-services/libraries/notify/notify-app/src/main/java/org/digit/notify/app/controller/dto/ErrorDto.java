package org.digit.notify.app.controller.dto;

import org.jspecify.annotations.Nullable;

public record ErrorDto(
    String code,
    String message,
    String timestamp,
    @Nullable String traceId
) {}
