package org.egov.infra.mdms.repository;

import org.egov.common.contract.request.RequestInfo;
import org.egov.infra.mdms.model.AuditLog;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository {

    public List<AuditLog> search(RequestInfo requestInfo, String tenantId, String objectId, int offset, int limit);

}
