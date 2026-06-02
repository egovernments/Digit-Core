package org.digit.notify.app.model;

import org.jspecify.annotations.Nullable;

public record ChannelDispatchStatus(
    String channel,
    String status,
    @Nullable String provider,
    @Nullable String reason
) {}
