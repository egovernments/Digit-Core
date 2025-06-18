package org.egov.persistence.repository;

import org.egov.persistence.entity.Message;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface DefaultDataJpaRepository extends CrudRepository<Message, String> {

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO message (id, tenantid, locale, module, code, message, createdby, createddate, lastmodifiedby, lastmodifieddate) " +
        "SELECT uuid_generate_v4(), :targetTenantId, locale, module, code, message, createdby, :currentTimestamp, lastmodifiedby, NULL " +
        "FROM message " +
        "WHERE tenantid = :defaultTenantId AND locale = :locale AND module IN :modules " +
        "ON CONFLICT (tenantid, locale, module, code) DO NOTHING", nativeQuery = true)
    int copyMessageDefinitions(
        @Param("defaultTenantId") String defaultTenantId,
        @Param("targetTenantId") String targetTenantId,
        @Param("currentTimestamp") java.sql.Timestamp currentTimestamp,
        @Param("locale") String locale,
        @Param("modules") List<String> modules);
}
