package org.egov.persistence.repository;

import org.egov.persistence.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.QueryHint;
import java.util.List;
import java.util.stream.Stream;

import static org.hibernate.jpa.QueryHints.HINT_FETCH_SIZE;
import static org.hibernate.jpa.QueryHints.HINT_READONLY;;

@Repository
public interface MessageJpaRepository extends JpaRepository<Message, String> {
    @Query("select m from Message m where m.tenantId = :tenantId and m.locale = :locale")
    @QueryHints(value = {
            @QueryHint(name = HINT_FETCH_SIZE, value = "1000"),
            @QueryHint(name = HINT_READONLY, value = "true")
    })
    @Transactional(readOnly = true)
    Stream<Message> find(@Param("tenantId") String tenantId, @Param("locale") String locale);


    @Query("select m from Message m where m.tenantId = :tenantId and m.locale = :locale and m.module in :modules")
    List<Message> findByModules(
        @Param("tenantId") String tenantId,
        @Param("locale") String locale,
        @Param("modules") List<String> modules);

	@Query("select m from Message m where m.tenantId = :tenantId and m.locale = :locale and m.module = :module")
	List<Message> find(@Param("tenantId") String tenantId, @Param("locale") String locale,
			@Param("module") String module);

	@Query("select m from Message m where m.tenantId = :tenantId and m.locale = :locale and m.module = :module and m.code in :codes")
	List<Message> find(@Param("tenantId") String tenantId, @Param("locale") String locale,
			@Param("module") String module, @Param("codes") List<String> codes);

	@Query("select m.id from Message m where m.tenantId = :tenantId and m.locale = :locale and m.module = :module and m.code = :code")
	List<Message> find(@Param("tenantId") String tenantId, @Param("locale") String locale,
			@Param("module") String module, @Param("code") String code);
}
