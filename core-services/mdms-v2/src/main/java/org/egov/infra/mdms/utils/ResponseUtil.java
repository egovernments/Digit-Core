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
        SchemaDefinitionResponse schemaDefinitionResponse = SchemaDefinitionResponse.builder().schemaDefinitions(schemaDefinitions).build();
        return schemaDefinitionResponse;
    }

    public static MdmsResponseV2 getMasterDataV2Response(List<Mdms> masterDataList){
        MdmsResponseV2 response = MdmsResponseV2.builder().mdms(masterDataList).build();
        return response;
    }

}
