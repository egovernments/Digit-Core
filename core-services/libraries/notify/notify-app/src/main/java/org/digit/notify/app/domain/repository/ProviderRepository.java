package org.digit.notify.app.domain.repository;

import org.digit.notify.app.domain.entity.ProviderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProviderRepository extends JpaRepository<ProviderEntity, UUID> {

    Optional<ProviderEntity> findByProviderName(String providerName);

    List<ProviderEntity> findByIsActive(boolean isActive);
}
