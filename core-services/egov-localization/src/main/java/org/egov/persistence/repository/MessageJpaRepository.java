package org.egov.persistence.repository;

import org.egov.persistence.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageJpaRepository extends JpaRepository<Message, Long> {
	@Query("select m from Message m where m.tenantId = :tenantId and m.locale = :locale")
	List<Message> find(@Param("tenantId") String tenantId, @Param("locale") String locale);

	@Query("select m from Message m where m.tenantId = :tenantId and m.locale = :locale and m.module = :module")
	List<Message> find(@Param("tenantId") String tenantId, @Param("locale") String locale,
			@Param("module") String module);

	@Query("select m from Message m where m.tenantId = :tenantId and m.locale = :locale and m.module = :module and m.code in :codes")
	List<Message> find(@Param("tenantId") String tenantId, @Param("locale") String locale,
			@Param("module") String module, @Param("codes") List<String> codes);

	@Query("select m.id from Message m where m.tenantId = :tenantId and m.locale = :locale and m.module = :module and m.code = :code")
	List<Message> find(@Param("tenantId") String tenantId, @Param("locale") String locale,
			@Param("module") String module, @Param("code") String code);

	/**
	 * Lightweight projection queries that bypass JPA persistence context.
	 * Returns only the 5 fields needed for message resolution, avoiding
	 * the overhead of loading full entities with audit fields.
	 */
	@Query(value = "SELECT code, locale, module, message, tenantid AS tenantId FROM message WHERE tenantid = :tenantId AND locale = :locale", nativeQuery = true)
	List<MessageProjection> findProjected(@Param("tenantId") String tenantId, @Param("locale") String locale);

	@Query(value = "SELECT code, locale, module, message, tenantid AS tenantId FROM message WHERE tenantid = :tenantId AND locale = :locale AND module = :module", nativeQuery = true)
	List<MessageProjection> findProjected(@Param("tenantId") String tenantId, @Param("locale") String locale,
			@Param("module") String module);

	@Query(value = "SELECT code, locale, module, message, tenantid AS tenantId FROM message WHERE tenantid = :tenantId AND locale = :locale AND module IN (:modules)", nativeQuery = true)
	List<MessageProjection> findProjectedByModules(@Param("tenantId") String tenantId, @Param("locale") String locale,
			@Param("modules") List<String> modules);
}
