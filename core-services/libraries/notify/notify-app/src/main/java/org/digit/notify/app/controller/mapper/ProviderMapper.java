package org.digit.notify.app.controller.mapper;

import org.digit.notify.app.controller.dto.ProviderResponseDto;
import org.digit.notify.app.domain.entity.ProviderEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProviderMapper {

    @Mapping(target = "isActive", source = "active")
    ProviderResponseDto toDto(ProviderEntity entity);
}
