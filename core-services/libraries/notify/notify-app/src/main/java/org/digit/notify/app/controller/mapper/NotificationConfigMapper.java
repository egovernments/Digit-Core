package org.digit.notify.app.controller.mapper;

import org.digit.notify.app.controller.dto.NotificationConfigRequestDto;
import org.digit.notify.app.controller.dto.NotificationConfigResponseDto;
import org.digit.notify.app.domain.entity.NotificationConfigEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationConfigMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "auditDetail", ignore = true)
    NotificationConfigEntity toEntity(NotificationConfigRequestDto dto);

    @Mapping(target = "isActive", source = "active")
    NotificationConfigResponseDto toDto(NotificationConfigEntity entity);
}
