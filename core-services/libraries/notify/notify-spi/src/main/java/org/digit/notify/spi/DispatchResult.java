package org.digit.notify.spi;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public record DispatchResult(
    Channel channel,
    DispatchStatus status,
    String providerName,
    @Nullable String reason
) {
    public static DispatchResult dispatched(Channel channel, String providerName) {
        return new DispatchResult(channel, DispatchStatus.DISPATCHED, providerName, null);
    }

    public static DispatchResult skipped(Channel channel, String reason) {
        return new DispatchResult(channel, DispatchStatus.SKIPPED, "none", reason);
    }

    public static DispatchResult failed(Channel channel, String providerName, String reason) {
        return new DispatchResult(channel, DispatchStatus.FAILED, providerName, reason);
    }
}
