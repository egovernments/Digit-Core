package org.egov.infra.mdms.service.enrichment;

import org.egov.common.contract.models.AuditDetails;
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

    public void enrichCreateRequest(MdmsRequest mdmsRequest, JSONObject schemaObject, String clientId) {
        Mdms mdms = mdmsRequest.getMdms().get(0);
        UUIDEnrichmentUtil.enrichRandomUuid(mdms, "id");
        mdms.setAuditDetails(getAuditDetails(clientId, mdms.getAuditDetails(), Boolean.TRUE));
        mdms.setUniqueIdentifier(CompositeUniqueIdentifierGenerationUtil.getUniqueIdentifier(schemaObject, mdmsRequest));
    }

    public AuditDetails getAuditDetails(String clientId, AuditDetails auditDetails, Boolean isCreateRequest) {
        if(isCreateRequest) {
            auditDetails = AuditDetails.builder().createdBy(clientId).
                    createdTime(System.currentTimeMillis()).lastModifiedBy(clientId).
                    lastModifiedTime(System.currentTimeMillis()).build();
        } else {
            if(auditDetails != null) {
                auditDetails = AuditDetails.builder().createdBy(auditDetails.getCreatedBy()).
                        createdTime(auditDetails.getCreatedTime()).lastModifiedBy(clientId).
                        lastModifiedTime(System.currentTimeMillis()).build();
            } else {
                throw new CustomException("AUDIT_DETAILS_NULL_FOR_UPDATE_REQ","Audit details can not be null for update request");
            }
        }
        return auditDetails;
    }

    public void enrichUpdateRequest(MdmsRequest mdmsRequest, String clientId) {
        Mdms mdms = mdmsRequest.getMdms().get(0);

        if(ObjectUtils.isEmpty(mdms.getAuditDetails()))
            throw new CustomException("AUDIT_DETAILS_ABSENT_ERR", "Audit details cannot be absent for update request");

        mdms.setAuditDetails(getAuditDetails(clientId, mdms.getAuditDetails(), Boolean.FALSE));
    }
}
