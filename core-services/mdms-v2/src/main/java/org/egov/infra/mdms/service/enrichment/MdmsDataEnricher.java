package org.egov.infra.mdms.service.enrichment;

import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.utils.AuditDetailsEnrichmentUtil;
import org.egov.common.utils.UUIDEnrichmentUtil;
import org.egov.infra.mdms.model.Mdms;
import org.egov.infra.mdms.model.MdmsRequest;
import org.egov.infra.mdms.utils.CompositeUniqueIdentifierGenerationUtil;
import org.egov.tracer.model.CustomException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component
public class MdmsDataEnricher {

    public void enrichCreateRequest(MdmsRequest mdmsRequest, JSONObject schemaObject) {
        Mdms mdms = mdmsRequest.getMdms();
        UUIDEnrichmentUtil.enrichRandomUuid(mdms, "id");
        mdms.setAuditDetails(AuditDetailsEnrichmentUtil.prepareAuditDetails(mdms.getAuditDetails(), mdmsRequest.getRequestInfo(), Boolean.TRUE));
        mdms.setUniqueIdentifier(CompositeUniqueIdentifierGenerationUtil.getUniqueIdentifier(schemaObject, mdmsRequest));
    }

    public AuditDetails getAuditDetails(RequestInfo requestInfo, AuditDetails auditDetails, Boolean isCreateRequest) {
        if(isCreateRequest) {
            auditDetails = AuditDetails.builder().createdBy(requestInfo.getUserInfo().getUuid()).
                    createdTime(System.currentTimeMillis()).lastModifiedBy(requestInfo.getUserInfo().getUuid()).
                    lastModifiedTime(System.currentTimeMillis()).build();
        } else {
            if(auditDetails != null) {
                auditDetails = AuditDetails.builder().createdBy(auditDetails.getCreatedBy()).
                        createdTime(auditDetails.getCreatedTime()).lastModifiedBy(requestInfo.getUserInfo().getUuid()).
                        lastModifiedTime(System.currentTimeMillis()).build();
            } else {
                throw new CustomException("AUDIT_DETAILS_NULL_FOR_UPDATE_REQ","Audit details can not be null for update request");
            }
        }
        return auditDetails;
    }

    public void enrichUpdateRequest(MdmsRequest mdmsRequest) {
        Mdms mdms = mdmsRequest.getMdms();

        if(ObjectUtils.isEmpty(mdms.getAuditDetails()))
            throw new CustomException("AUDIT_DETAILS_ABSENT_ERR", "Audit details cannot be absent for update request");

        mdms.setAuditDetails(getAuditDetails(mdmsRequest.getRequestInfo(), mdms.getAuditDetails(), Boolean.FALSE));
    }
}
