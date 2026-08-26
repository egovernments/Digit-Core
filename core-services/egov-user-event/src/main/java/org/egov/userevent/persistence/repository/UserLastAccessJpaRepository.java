package org.egov.userevent.persistence.repository;

import org.egov.userevent.persistence.entity.UserLastAccessEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserLastAccessJpaRepository extends JpaRepository<UserLastAccessEntity, String> {
}
