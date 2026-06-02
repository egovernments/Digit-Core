package org.digit.notify.app.domain.repository;

import org.digit.notify.app.domain.entity.ProviderMappingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProviderMappingRepository extends JpaRepository<ProviderMappingEntity, UUID> {

    List<ProviderMappingEntity> findByTenantIdAndChannel(String tenantId, String channel);

    Optional<ProviderMappingEntity> findByTenantIdAndChannelAndCountry(
        String tenantId, String channel, String country);

    Optional<ProviderMappingEntity> findByTenantIdAndChannelAndCountryIsNull(
        String tenantId, String channel);
}
