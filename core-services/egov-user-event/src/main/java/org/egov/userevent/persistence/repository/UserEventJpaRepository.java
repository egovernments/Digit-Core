package org.egov.userevent.persistence.repository;

import org.egov.userevent.persistence.entity.UserEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserEventJpaRepository extends JpaRepository<UserEventEntity, String> {
}
