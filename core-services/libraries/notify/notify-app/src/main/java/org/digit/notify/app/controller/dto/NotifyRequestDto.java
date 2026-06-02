package org.digit.notify.app.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record NotifyRequestDto(
    @NotBlank String templateCode,
    @NotNull RecipientDto recipient,
    @NotNull Map<String, Object> payload,
    String locale,
    Map<String, Object> metadata
) {}
