package org.digit.notify.app.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ProviderMappingRequestDto(
    @NotBlank String channel,
    String country,
    @NotEmpty List<String> providers
) {}
