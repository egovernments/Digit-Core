package org.egov.common.utils;

import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.exception.NullCheckException;
import org.springframework.util.ObjectUtils;

import static org.egov.common.error.ErrorCode.*;

public class AuditDetailsEnrichmentUtil {

    private AuditDetailsEnrichmentUtil(){};

    /**
     * This is a utility method to enrich auditDetails so that modules don't have to write logic to
     * enrich auditDetails in case of create and update requests.
     * @param auditDetails
     * @param requestInfo
     * @param isCreateRequest
     */
    public static AuditDetails prepareAuditDetails(AuditDetails auditDetails, RequestInfo requestInfo, Boolean isCreateRequest) {
        // This method validates if auditDetails, requestInfo and userInfo are not null.
        validateInput(auditDetails, requestInfo, isCreateRequest);

        // Check if isCreateRequest flag is true - in this case, enrich createdTime and createdBy fields.
        if(isCreateRequest){
            auditDetails = AuditDetails.builder().build();
            auditDetails.setCreatedTime(System.currentTimeMillis());
            auditDetails.setCreatedBy(requestInfo.getUserInfo().getUuid());
        }

        // Update lastModifiedBy and lastModifiedTime fields for both create and update requests.
        auditDetails.setLastModifiedTime(System.currentTimeMillis());
        auditDetails.setLastModifiedBy(requestInfo.getUserInfo().getUuid());

        // Check if isCreateRequest flag is true - in this case, enrich createdTime and createdBy fields.
        if(isCreateRequest){
            auditDetails.setCreatedTime(System.currentTimeMillis());
            auditDetails.setCreatedBy(requestInfo.getUserInfo().getUuid());
        }

        return auditDetails;
    }

    /**
     * This method validates the input fed to enrichAuditDetails method
     * @param auditDetails
     * @param requestInfo
     * @param isCreateRequest
     */
    private static void validateInput(AuditDetails auditDetails, RequestInfo requestInfo, Boolean isCreateRequest) {
        // Throw null check exception in case isCreateRequest flag is null.
        if(ObjectUtils.isEmpty(isCreateRequest))
            throw new NullCheckException(IS_CREATE_REQUEST_FLAG_NULL_ERROR_MESSAGE);

        // Throw null check exception in case requestInfo is null.
        if(ObjectUtils.isEmpty(requestInfo))
            throw new NullCheckException(REQUEST_INFO_NULL_ERROR_MESSAGE);

        // Throw null check exception in case userInfo within requestInfo is null.
        if(ObjectUtils.isEmpty(requestInfo.getUserInfo()))
            throw new NullCheckException(USER_INFO_NULL_ERROR_MESSAGE);

        // Throw null check exception in case userInfo within requestInfo is null.
        if(ObjectUtils.isEmpty(requestInfo.getUserInfo().getUuid()))
            throw new NullCheckException(USER_UUID_NULL_ERROR_MESSAGE);

        // Check whether createdBy and createdTime fields are set within auditDetails in case of update request.
        if(!isCreateRequest) {
            // Throw null check exception in case auditDetails is null.
            if(ObjectUtils.isEmpty(auditDetails))
                throw new NullCheckException(AUDIT_DETAILS_NULL_ERROR_MESSAGE);

            // Throw null check exception in case createdBy within auditDetails is null
            if (ObjectUtils.isEmpty(auditDetails.getCreatedBy()))
                throw new NullCheckException(CREATED_BY_NULL_ERROR_MESSAGE);

            // Throw null check exception in case createdTime within auditDetails is null
            if (ObjectUtils.isEmpty(auditDetails.getCreatedTime()))
                throw new NullCheckException(CREATED_TIME_NULL_ERROR_MESSAGE);
        }

    }

}
