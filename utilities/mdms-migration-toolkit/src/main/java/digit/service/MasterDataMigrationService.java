package digit.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.validation.Valid;

import org.egov.common.utils.AuditDetailsEnrichmentUtil;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import digit.models.coremodels.AuditDetails;
import digit.util.FileReader;
import digit.web.models.MasterDataMigrationRequest;
import digit.web.models.Mdms;
import digit.web.models.MdmsRequest;
import net.minidev.json.JSONArray;

@Service
public class MasterDataMigrationService {

    @Autowired
    private FileReader fileReader;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${master.schema.files.dir}")
    public String schemaFilesDirectory;

    /**
     * This methods accepts master data migration request and triggers
     * creation of MDMS objects
     * @param masterDataMigrationRequest
     */
    public void migrateMasterData(@Valid MasterDataMigrationRequest masterDataMigrationRequest) {

        // Get Master Data map
        Map<String, Map<String, Map<String, JSONArray>>> tenantMap = MDMSApplicationRunnerImpl.getTenantMap();

        // Get tenantId from request
        String tenantId = masterDataMigrationRequest.getMasterDataMigrationCriteria().getTenantId();

        // Build audit details for mdms objects creation
        AuditDetails auditDetails = new AuditDetails();
        RequestInfo requestInfo = masterDataMigrationRequest.getRequestInfo();
        AuditDetailsEnrichmentUtil.enrichAuditDetails(auditDetails, requestInfo, Boolean.TRUE);

        List<Mdms> masterDataList = new ArrayList<>();

        // Check if master data is present for the incoming tenantId.
        if (tenantMap.containsKey(tenantId)) {
            tenantMap.get(tenantId).keySet().forEach(module -> {
                tenantMap.get(tenantId).get(module).keySet().forEach(master -> {

                    // Get master data array for current module and master
                    JSONArray masterDataJsonArray = MDMSApplicationRunnerImpl
                            .getTenantMap()
                            .get(tenantId)
                            .get(module)
                            .get(master);

                    // Build MDMS objects
                    masterDataJsonArray.forEach(masterDatum -> {
                        // Convert JSONArray member to JsonNode
                        JsonNode masterDatumJsonNode = objectMapper.valueToTree(masterDatum);

                        // Build MDMS objects
                        Mdms mdms = Mdms.builder()
                                .schemaCode(module + DOT_SEPARATOR + master)
                                .data(masterDatumJsonNode)
                                .isActive(Boolean.TRUE)
                                .tenantId(tenantId)
                                .uniqueIdentifier(UUID.randomUUID().toString())
                                .auditDetails(auditDetails)
                                .build();

                        MdmsRequest mdmsRequest = MdmsRequest.builder()
                                .mdms(mdms)
                                .requestInfo(requestInfo)
                                .build();

                        // TODO - Make call to MDMS Service with the created request
                        restTemplate.postForObject("http://localhost:8094/mdms-v2/v2/_create/" + mdmsRequest.getMdms().getSchemaCode(), mdmsRequest, Map.class);
                    });
                });
            });
        } else {
            throw new CustomException(MASTER_DATA_MIGRATION_ERROR_CODE, MASTER_DATA_MIGRATION_TENANTID_DOES_NOT_EXIST_ERROR_MESSAGE + masterDataMigrationRequest.getMasterDataMigrationCriteria().getTenantId());
        }

    }

}
