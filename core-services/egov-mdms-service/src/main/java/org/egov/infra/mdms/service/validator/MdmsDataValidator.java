package org.egov.infra.mdms.service.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.TypeRef;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.egov.infra.mdms.model.*;
import org.egov.infra.mdms.repository.MdmsDataRepository;
import org.egov.infra.mdms.service.SchemaDefinitionService;
import org.egov.infra.mdms.utils.CompositeUniqueIdentifierGenerationUtil;
import org.egov.infra.mdms.utils.ErrorUtil;
import org.egov.infra.mdms.utils.FallbackUtil;
import org.egov.tracer.model.CustomException;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import static org.egov.infra.mdms.utils.MDMSConstants.*;

@Component
@Slf4j
public class MdmsDataValidator {

    private final SchemaDefinitionService schemaDefinitionService;

    private final MdmsDataRepository mdmsDataRepository;

    @Autowired
    public MdmsDataValidator(SchemaDefinitionService schemaDefinitionService, MdmsDataRepository mdmsDataRepository) {
        this.schemaDefinitionService = schemaDefinitionService;
        this.mdmsDataRepository = mdmsDataRepository;
    }

    /**
     * This method performs business validations on create master data request.
     * @param mdmsRequest
     * @param schemaObject
     */
    public void validateCreateRequest(MdmsRequest mdmsRequest, JSONObject schemaObject) {
        // Initialize error map and fetch schema
        Map<String, String> errors = new HashMap<>();

        // Validations are performed here on the incoming data
        validateDataWithSchemaDefinition(mdmsRequest, schemaObject, errors);
        checkDuplicate(schemaObject, mdmsRequest);
        validateReference(schemaObject, mdmsRequest.getMdms());

        // Throw validation errors
        ErrorUtil.throwCustomExceptions(errors);
    }

    /**
     * This method validates the incoming master data against the schema for which the master
     * data is being created and populates violations(if any) in errors map.
     * @param mdmsRequest
     * @param schemaObject
     * @param errors
     */
    private void validateDataWithSchemaDefinition(MdmsRequest mdmsRequest, JSONObject schemaObject,Map<String, String> errors) {
        try {
            // Load incoming master data as a json object
            JSONObject dataObject = new JSONObject(mdmsRequest.getMdms().getData().toString());

            // Load schema
            Schema schema = SchemaLoader.load(schemaObject);

            // Validate data against the schema
            schema.validate(dataObject);
        } catch (ValidationException e) {
            // Populate errors map for all the validation errors
            Integer count = 0;
            if (!e.getCausingExceptions().isEmpty()) {
                for (ValidationException validationException : e.getCausingExceptions()) {
                    ++count;
                    errors.put("INVALID_REQUEST_".concat(validationException.getKeyword().toUpperCase()).concat(count.toString()), validationException.getErrorMessage());
                }
            } else {
                errors.put("INVALID_REQUEST", e.getErrorMessage());
            }
        } catch (Exception e) {
            throw new CustomException("MASTER_DATA_VALIDATION_ERR", "An unknown error occurred while validating provided master data against the schema - " + e.getMessage());
        }
    }

    /**
     * This method checks whether the master data which is being created already
     * exists in the database or not.
     * @param schemaObject
     * @param mdmsRequest
     */
    private void checkDuplicate(JSONObject schemaObject, MdmsRequest mdmsRequest) {
        // Get the uniqueIdentifier for the incoming master data request
        String uniqueIdentifier = CompositeUniqueIdentifierGenerationUtil.getUniqueIdentifier(schemaObject, mdmsRequest);

        // Fetch master data
        List<Mdms> masterData = fetchMasterData(MdmsCriteriaV2.builder()
                .tenantId(mdmsRequest.getMdms().getTenantId())
                .uniqueIdentifiers(Collections.singleton(uniqueIdentifier))
                .schemaCode(mdmsRequest.getMdms().getSchemaCode())
                .isActive(Boolean.TRUE)
                .build());

        // Throw error if the provided master data already exists
        if (masterData != null && masterData.size() != 0) {
            throw new CustomException("DUPLICATE_RECORD", "Duplicate record");
        }

    }

    /**
     * This method fetches master data from the database depending on the criteria
     * being passed.
     * @param mdmsCriteria
     * @return
     */
    private List<Mdms> fetchMasterData(MdmsCriteriaV2 mdmsCriteria) {
        // Make a call to the repository to check if the provided master data already exists
        List<Mdms> moduleMasterData = mdmsDataRepository.searchV2(mdmsCriteria);

        return moduleMasterData;

    }

    /**
     * This method validates whether the provided reference exists in the database.
     * @param schemaObject
     * @param mdms
     */
    private void validateReference(JSONObject schemaObject, Mdms mdms) {
        if (schemaObject.has(X_REFERENCE_SCHEMA_KEY)) {
            org.json.JSONArray referenceSchema = (org.json.JSONArray) schemaObject.get(X_REFERENCE_SCHEMA_KEY);

            if (referenceSchema != null && referenceSchema.length() > 0) {
                JsonNode mdmsData = mdms.getData();

                IntStream.range(0, referenceSchema.length()).forEach(i -> {
                    Set<String> uniqueIdentifiersForRefVerification = new HashSet<>();

                    JSONObject jsonObject = referenceSchema.getJSONObject(i);
                    String refFieldPath = jsonObject.getString(FIELD_PATH_KEY);
                    String schemaCode = jsonObject.getString(SCHEMA_CODE_KEY);
                    Object refResult = JsonPath.read(mdmsData.toString(), CompositeUniqueIdentifierGenerationUtil.getJsonPathExpressionFromDotSeparatedPath(refFieldPath));

                    addTypeCastedUniqueIdentifiersToVerificationSet(refResult, uniqueIdentifiersForRefVerification);
                    List<String> subTenantListForFallback = FallbackUtil.getSubTenantListForFallBack(mdms.getTenantId());

                    Boolean isRefDataFound = Boolean.FALSE;

                    for(String subTenant : subTenantListForFallback) {
                        List<Mdms> moduleMasterData = mdmsDataRepository.searchV2(
                                MdmsCriteriaV2.builder().tenantId(subTenant).uniqueIdentifiersForRefVerification(uniqueIdentifiersForRefVerification).schemaCode(schemaCode).build());

                        if (moduleMasterData.size() == uniqueIdentifiersForRefVerification.size()) {
                            isRefDataFound = Boolean.TRUE;
                            break;
                        }
                    }

                    if(!isRefDataFound) {
                        throw new CustomException("REFERENCE_VALIDATION_ERR", "Provided reference value does not exist in database");
                    }

                });
            }
        }
    }

    /**
     * This method takes the reference object provided in the data create request, type casts it into String
     * and adds it to the uniqueIdentifiers set for performing search.
     * @param refResult
     * @param uniqueIdentifiersForRefVerification
     */
    private void addTypeCastedUniqueIdentifiersToVerificationSet(Object refResult, Set<String> uniqueIdentifiersForRefVerification) {
        if (refResult instanceof String) {
            uniqueIdentifiersForRefVerification.add((String) refResult);
        } else if(refResult instanceof Number){
            uniqueIdentifiersForRefVerification.add(String.valueOf(refResult));
        } else if (refResult instanceof List) {
            uniqueIdentifiersForRefVerification.addAll((Collection<? extends String>) refResult);
        } else {
            throw new CustomException("REFERENCE_VALIDATION_ERR", "Reference must only be of the type string, number or a list of strings/numbers");
        }
    }

    /**
     * This method performs business validations on create master data request.
     * @param mdmsRequest
     * @param schemaObject
     */
    public void validateUpdateRequest(MdmsRequest mdmsRequest, JSONObject schemaObject) {
        // Initialize error map and fetch schema
        Map<String, String> errors = new HashMap<>();

        // Validations are performed here on the incoming data
        String uniqueIdentifierOfExistingRecord = fetchUniqueIdentifier(mdmsRequest);
        validateIfUniqueFieldsAreNotBeingUpdated(uniqueIdentifierOfExistingRecord, CompositeUniqueIdentifierGenerationUtil.getUniqueIdentifier(schemaObject, mdmsRequest));
        validateDataWithSchemaDefinition(mdmsRequest, schemaObject, errors);
        validateReference(schemaObject, mdmsRequest.getMdms());

        // Throw validation errors
        ErrorUtil.throwCustomExceptions(errors);

    }

    /**
     * This method validates if any fields defined under unique field list are being updated.
     * @param uniqueIdentifierOfExistingRecord
     * @param uniqueIdentifier
     */
    private void validateIfUniqueFieldsAreNotBeingUpdated(String uniqueIdentifierOfExistingRecord, String uniqueIdentifier) {
        if(!uniqueIdentifierOfExistingRecord.equalsIgnoreCase(uniqueIdentifier)) {
            throw new CustomException("UNIQUE_KEY_UPDATE_ERR", "Updating fields defined as unique is not allowed.");
        }
    }

    /**
     * This method checks if the master that is being created already exists or not.
     * @param mdmsRequest
     * @return
     */
    private String fetchUniqueIdentifier(MdmsRequest mdmsRequest) {
        if(ObjectUtils.isEmpty(mdmsRequest.getMdms().getId()))
            throw new CustomException("MASTER_DATA_ID_ABSENT_ERR", "Providing master data id is mandatory for update operation.");

        Set<String> idForSearch = new HashSet<>();
        idForSearch.add(mdmsRequest.getMdms().getId());

        // Fetch master data from database
        List<Mdms> masterData = fetchMasterData(MdmsCriteriaV2.builder()
                .tenantId(mdmsRequest.getMdms().getTenantId())
                .ids(idForSearch)
                .build());

        // Throw error if the provided master data does not exist
        if (masterData == null || masterData.size() == 0) {
            throw new CustomException("MASTER_DATA_NOT_FOUND", "Master data not found for update operation.");
        } // Return unique identifier if the master data exists
        else {
            return masterData.get(0).getUniqueIdentifier();
        }
    }
}
