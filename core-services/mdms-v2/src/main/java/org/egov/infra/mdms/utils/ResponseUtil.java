package org.egov.infra.mdms.utils;

import org.egov.common.contract.response.ResponseInfo;
import org.egov.infra.mdms.model.Mdms;
import org.egov.infra.mdms.model.MdmsResponseV2;
import org.egov.infra.mdms.model.SchemaDefinition;
import org.egov.infra.mdms.model.SchemaDefinitionResponse;

import java.util.List;

public class ResponseUtil {

    private ResponseUtil(){}

    public static SchemaDefinitionResponse getSchemaDefinitionResponse(List<SchemaDefinition> schemaDefinitions){
        ResponseInfo responseInfo = ResponseInfo.builder().status("SUCCESS").build();
        SchemaDefinitionResponse schemaDefinitionResponse = SchemaDefinitionResponse.builder().schemaDefinitions(schemaDefinitions).responseInfo(responseInfo).build();
        return schemaDefinitionResponse;
    }

    public static MdmsResponseV2 getMasterDataV2Response(List<Mdms> masterDataList){
        ResponseInfo responseInfo = ResponseInfo.builder().status("SUCCESS").build();
        MdmsResponseV2 response = MdmsResponseV2.builder().mdms(masterDataList).responseInfo(responseInfo).build();
        return response;
    }

}
