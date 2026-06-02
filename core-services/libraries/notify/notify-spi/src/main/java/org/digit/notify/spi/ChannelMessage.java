package org.digit.notify.spi;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import java.util.Map;

@NullMarked
public record ChannelMessage(
    Channel channel,
    String renderedBody,
    @Nullable String renderedSubject,
    @Nullable String renderedTitle,
    Map<String, Object> metadata
) {}
