package org.digit.notify.app.domain.repository;

import org.digit.notify.app.domain.entity.NotificationConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationConfigRepository extends JpaRepository<NotificationConfigEntity, UUID> {

    Optional<NotificationConfigEntity> findByTenantIdAndTemplateCodeAndIsActiveTrue(
        String tenantId, String templateCode);

    List<NotificationConfigEntity> findByTenantId(String tenantId);

    List<NotificationConfigEntity> findByTenantIdAndIsActive(String tenantId, boolean isActive);

    List<NotificationConfigEntity> findByTenantIdAndTemplateCode(String tenantId, String templateCode);
}
