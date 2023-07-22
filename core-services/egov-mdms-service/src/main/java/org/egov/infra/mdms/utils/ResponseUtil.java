package org.egov.infra.mdms.utils;

import org.egov.common.contract.request.RequestInfo;
import org.egov.infra.mdms.model.Mdms;
import org.egov.infra.mdms.model.MdmsResponseV2;
import org.egov.infra.mdms.model.SchemaDefinition;
import org.egov.infra.mdms.model.SchemaDefinitionResponse;

import java.util.List;

public class ResponseUtil {

    private ResponseUtil(){}

    public static SchemaDefinitionResponse getSchemaDefinitionResponse(RequestInfo requestInfo , List<SchemaDefinition> schemaDefinitions){
        //ResponseInfo responseInfo = ResponseInfoUtil.buildResponseInfo(requestInfo,apiVersion, status);
        SchemaDefinitionResponse schemaDefinitionResponse = SchemaDefinitionResponse.builder().schemaDefinitions(schemaDefinitions).responseInfo(null).build();
        return schemaDefinitionResponse;
    }

    public static MdmsResponseV2 getMasterDataV2Response(RequestInfo requestInfo, List<Mdms> masterDataList){
        MdmsResponseV2 response = MdmsResponseV2.builder().mdms(masterDataList).responseInfo(null).build();
        return response;
    }

}
