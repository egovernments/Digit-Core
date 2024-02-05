package org.egov.infra.mdms.utils;

import org.egov.infra.mdms.model.MdmsCriteriaReqV2;
import org.egov.infra.mdms.model.MdmsCriteriaV2;
import org.egov.infra.mdms.model.MultiSchemaSearchReq;
import org.springframework.stereotype.Component;

@Component
public class MdmsUtil {
    public MdmsCriteriaV2 convertToMdmsCriteriaV2(MultiSchemaSearchReq multiSchemaSearchReq) {

        MdmsCriteriaV2 mdmsCriteriaV2 = MdmsCriteriaV2.builder()
                .tenantId(multiSchemaSearchReq.getMdmsCriteria().getTenantId())
                .ids(multiSchemaSearchReq.getMdmsCriteria().getIds())
                .limit(multiSchemaSearchReq.getMdmsCriteria().getLimit())
                .offset(multiSchemaSearchReq.getMdmsCriteria().getOffset())
                .isActive(multiSchemaSearchReq.getMdmsCriteria().getIsActive())
                .filterMap(multiSchemaSearchReq.getMdmsCriteria().getFilterMap())
                .schemaCodeFilterMap(multiSchemaSearchReq.getMdmsCriteria().getSchemaCodeFilterMap())
                .uniqueIdentifiers(multiSchemaSearchReq.getMdmsCriteria().getUniqueIdentifiers())
                .uniqueIdentifiersForRefVerification(multiSchemaSearchReq.getMdmsCriteria().getUniqueIdentifiersForRefVerification())
                .build();

        return  mdmsCriteriaV2;
    }

    public MdmsCriteriaReqV2 convertToMdmsCriteriaReqV2(MultiSchemaSearchReq multiSchemaSearchReq) {

        MdmsCriteriaV2 mdmsCriteriaV2 = convertToMdmsCriteriaV2(multiSchemaSearchReq);

        MdmsCriteriaReqV2 mdmsCriteriaReqV2 = MdmsCriteriaReqV2.builder()
                .mdmsCriteria(mdmsCriteriaV2)
                .requestInfo(multiSchemaSearchReq.getRequestInfo())
                .build();

        return mdmsCriteriaReqV2;
    }
}
