package org.egov.infra.mdms.service.enrichment;

import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.utils.AuditDetailsEnrichmentUtil;
import org.egov.common.utils.UUIDEnrichmentUtil;
import org.egov.infra.mdms.model.SchemaDefinition;
import org.egov.infra.mdms.model.SchemaDefinitionRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SchemaDefinitionEnricher {

    /**
     * This method enriches schemaDefinitionRequest
     * @param schemaDefinitionRequest
     */
    public void enrichCreateRequest(SchemaDefinitionRequest schemaDefinitionRequest) {
        SchemaDefinition schemaDefinition = schemaDefinitionRequest.getSchemaDefinition();

        // Invoke enrichment of uuid on id field of schemaDefinition
        UUIDEnrichmentUtil.enrichRandomUuid(schemaDefinition, "id");

        // Populate auditDetails
        schemaDefinition.setAuditDetails(getAuditDetail(schemaDefinitionRequest.getRequestInfo(),schemaDefinition.getAuditDetails(), true));
    }

    public AuditDetails getAuditDetail(RequestInfo requestInfo, AuditDetails auditDetails, Boolean isCreateRequest) {
        if(isCreateRequest) {
            auditDetails = AuditDetails.builder().createdBy(requestInfo.getUserInfo().getUuid()).
                    createdTime(System.currentTimeMillis()).lastModifiedBy(requestInfo.getUserInfo().getUuid()).
                    lastModifiedTime(System.currentTimeMillis()).build();
        } else {
            if(auditDetails != null ) {
                auditDetails = AuditDetails.builder().createdBy(auditDetails.getCreatedBy()).
                        createdTime(auditDetails.getCreatedTime()).lastModifiedBy(requestInfo.getUserInfo().getUuid()).
                        lastModifiedTime(System.currentTimeMillis()).build();
            } else {
                throw new CustomException("AUDIT_DETAILS_NULL_FOR_UPDATE_REQ","Audit details can not be null for update request");
            }
        }
        return auditDetails;
    }

}
