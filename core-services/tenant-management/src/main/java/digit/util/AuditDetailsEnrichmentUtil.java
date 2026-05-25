package digit.util;

import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.exception.NullCheckException;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.Objects;

@Component
public class AuditDetailsEnrichmentUtil {

    private AuditDetailsEnrichmentUtil() {
    }

    public static AuditDetails prepareAuditDetails(AuditDetails auditDetails, RequestInfo requestInfo, Boolean isCreateRequest) {

        if(Objects.isNull(requestInfo.getUserInfo())){
            return new AuditDetails();
        }

        validateInput(auditDetails, requestInfo, isCreateRequest);
        if (isCreateRequest) {
            auditDetails = AuditDetails.builder().build();
            auditDetails.setCreatedTime(System.currentTimeMillis());
            auditDetails.setCreatedBy(requestInfo.getUserInfo().getUuid());
        }

        auditDetails.setLastModifiedTime(System.currentTimeMillis());
        auditDetails.setLastModifiedBy(requestInfo.getUserInfo().getUuid());
        if (isCreateRequest) {
            auditDetails.setCreatedTime(System.currentTimeMillis());
            auditDetails.setCreatedBy(requestInfo.getUserInfo().getUuid());
        }

        return auditDetails;
    }

    private static void validateInput(AuditDetails auditDetails, RequestInfo requestInfo, Boolean isCreateRequest) {
        if (ObjectUtils.isEmpty(isCreateRequest)) {
            throw new NullCheckException("IsCreateRequest flag being sent to enrichAuditDetails method must not be null");
        } else if (ObjectUtils.isEmpty(requestInfo)) {
            throw new NullCheckException("RequestInfo being sent to enrichAuditDetails method must not be null");
        } else if (ObjectUtils.isEmpty(requestInfo.getUserInfo())) {
            throw new NullCheckException("UserInfo present inside RequestInfo being sent to enrichAuditDetails method must not be null");
        } else if (ObjectUtils.isEmpty(requestInfo.getUserInfo().getUuid())) {
            throw new NullCheckException("User uuid present inside UserInfo being sent to enrichAuditDetails method must not be null");
        } else {
            if (!isCreateRequest) {
                if (ObjectUtils.isEmpty(auditDetails)) {
                    throw new NullCheckException("AuditDetails being sent to enrichAuditDetails method must not be null");
                }

                if (ObjectUtils.isEmpty(auditDetails.getCreatedBy())) {
                    throw new NullCheckException("CreatedBy present inside AuditDetails being sent to enrichAuditDetails method must not be null in case of update request");
                }

                if (ObjectUtils.isEmpty(auditDetails.getCreatedTime())) {
                    throw new NullCheckException("CreatedTime present inside AuditDetails being sent to enrichAuditDetails method must not be null in case of update request");
                }
            }

        }
    }
}
