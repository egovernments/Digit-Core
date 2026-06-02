package org.digit.notify.app.dispatch;

import org.digit.notify.spi.Channel;
import org.digit.notify.spi.DispatchStatus;
import org.jspecify.annotations.Nullable;
import java.time.Instant;

public record AttemptRecord(
    Channel channel,
    String providerName,
    int attemptNo,
    DispatchStatus status,
    @Nullable String reason,
    Instant attemptedAt
) {}
