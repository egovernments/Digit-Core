package org.digit.notify.app.domain.repository;

import org.digit.notify.app.domain.entity.NotificationAttemptEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationAttemptRepository extends JpaRepository<NotificationAttemptEntity, UUID> {

    List<NotificationAttemptEntity> findByNotificationId(String notificationId);
}
