package org.digit.notify.app.controller.dto;

import java.util.List;

public record NotifyResponseDto(
    String notificationId,
    String templateCode,
    List<ChannelDispatchStatusDto> channels
) {}
