package org.digit.notify.app.domain.repository;

import org.digit.notify.app.domain.entity.NotificationLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface NotificationLogRepository extends JpaRepository<NotificationLogEntity, UUID> {

    Optional<NotificationLogEntity> findByNotificationId(String notificationId);
}
