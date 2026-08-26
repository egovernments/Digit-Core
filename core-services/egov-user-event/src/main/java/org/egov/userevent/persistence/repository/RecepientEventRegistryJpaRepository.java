package org.egov.userevent.persistence.repository;

import org.egov.userevent.persistence.entity.RecepientEventRegistryEntity;
import org.egov.userevent.persistence.entity.RecepientEventRegistryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RecepientEventRegistryJpaRepository
		extends JpaRepository<RecepientEventRegistryEntity, RecepientEventRegistryId> {

	@Modifying
	@Query("DELETE FROM RecepientEventRegistryEntity r WHERE r.eventId = :eventId")
	void deleteByEventId(String eventId);
}
