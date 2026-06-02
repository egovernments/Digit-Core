package org.digit.notify.app.model;

import java.util.List;

public record NotifyResponse(
    String notificationId,
    String templateCode,
    List<ChannelDispatchStatus> channels
) {}
