package org.digit.notify.spi;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import java.util.List;
import java.util.Map;

@NullMarked
public record Recipient(
    @Nullable String phone,
    @Nullable String email,
    List<String> deviceTokens,
    @Nullable String countryCode,
    Map<String, Object> metadata
) {}
