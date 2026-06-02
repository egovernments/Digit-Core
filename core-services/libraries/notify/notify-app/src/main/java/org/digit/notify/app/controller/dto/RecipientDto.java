package org.digit.notify.app.controller.dto;

import java.util.List;
import java.util.Map;

public record RecipientDto(
    String phone,
    String email,
    List<String> deviceTokens,
    String countryCode,
    Map<String, Object> metadata
) {}
