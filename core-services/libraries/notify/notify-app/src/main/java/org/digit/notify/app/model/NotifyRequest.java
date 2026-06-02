package org.digit.notify.app.model;

import org.digit.notify.spi.Recipient;
import org.jspecify.annotations.Nullable;
import java.util.Map;

public record NotifyRequest(
    String templateCode,
    Recipient recipient,
    Map<String, Object> payload,
    @Nullable String locale,
    Map<String, Object> metadata
) {}
