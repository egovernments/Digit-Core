package org.digit.notify.app.controller.mapper;

import org.digit.notify.app.controller.dto.NotifyRequestDto;
import org.digit.notify.app.controller.dto.NotifyResponseDto;
import org.digit.notify.app.controller.dto.RecipientDto;
import org.digit.notify.app.model.NotifyRequest;
import org.digit.notify.app.model.NotifyResponse;
import org.digit.notify.spi.Recipient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Collections;

@Mapper(componentModel = "spring")
public interface NotifyMapper {

    @Mapping(target = "metadata", expression = "java(dto.metadata() != null ? dto.metadata() : new java.util.HashMap<>())")
    NotifyRequest toDomain(NotifyRequestDto dto);

    @Mapping(target = "deviceTokens", expression = "java(dto.deviceTokens() != null ? dto.deviceTokens() : java.util.Collections.emptyList())")
    @Mapping(target = "metadata", expression = "java(dto.metadata() != null ? dto.metadata() : new java.util.HashMap<>())")
    Recipient toDomain(RecipientDto dto);

    NotifyResponseDto toDto(NotifyResponse response);
}
