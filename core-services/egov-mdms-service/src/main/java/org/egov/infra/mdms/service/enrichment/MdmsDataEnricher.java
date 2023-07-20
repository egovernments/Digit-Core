package org.egov.infra.mdms.service.enrichment;

import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.request.RequestInfo;
import org.egov.infra.mdms.model.Mdms;
import org.egov.infra.mdms.model.MdmsRequest;
import org.egov.infra.mdms.model.SchemaDefinition;
import org.egov.infra.mdms.model.SchemaDefinitionRequest;
import org.egov.infra.mdms.utils.CompositeUniqueIdentifierGenerationUtil;
import org.egov.tracer.model.CustomException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MdmsDataEnricher {

    public void enrichCreateRequest(MdmsRequest mdmsRequest, JSONObject schemaObject) {
        Mdms mdms = mdmsRequest.getMdms();
        mdms.setId(UUID.randomUUID().toString());
        mdms.setAuditDetails(getAuditDetail(mdmsRequest.getRequestInfo(),mdms.getAuditDetails(), true));
        mdms.setUniqueIdentifier(CompositeUniqueIdentifierGenerationUtil.getUniqueIdentifier(schemaObject, mdmsRequest));
    }

    public AuditDetails getAuditDetail(RequestInfo requestInfo, AuditDetails auditDetails, Boolean isCreateRequest) {
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
        mdms.setAuditDetails(getAuditDetail(mdmsRequest.getRequestInfo(), mdms.getAuditDetails(), Boolean.FALSE));
    }
}
