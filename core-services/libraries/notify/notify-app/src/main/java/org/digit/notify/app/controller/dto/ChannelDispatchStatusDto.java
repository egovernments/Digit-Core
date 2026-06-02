package org.digit.notify.app.controller.dto;

public record ChannelDispatchStatusDto(
    String channel,
    String status,
    String provider,
    String reason
) {}
