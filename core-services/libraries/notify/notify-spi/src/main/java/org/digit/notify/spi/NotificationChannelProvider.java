package org.digit.notify.spi;

import org.jspecify.annotations.NullMarked;
import java.util.Map;

@NullMarked
public interface NotificationChannelProvider {

    Channel supportedChannel();

    String providerName();

    DispatchResult send(
        ChannelMessage message,
        Recipient recipient,
        Map<String, Object> metadata
    );
}
