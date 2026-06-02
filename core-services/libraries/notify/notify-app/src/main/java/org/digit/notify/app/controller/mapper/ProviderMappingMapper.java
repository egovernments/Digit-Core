package org.digit.notify.app.controller.mapper;

import org.digit.notify.app.controller.dto.ProviderMappingRequestDto;
import org.digit.notify.app.controller.dto.ProviderMappingResponseDto;
import org.digit.notify.app.domain.entity.ProviderMappingEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProviderMappingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "auditDetail", ignore = true)
    ProviderMappingEntity toEntity(ProviderMappingRequestDto dto);

    ProviderMappingResponseDto toDto(ProviderMappingEntity entity);
}
