package digit.service.validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import digit.errors.ErrorCodes;
import digit.repository.ServiceRequestRepository;
import digit.util.ErrorUtil;
import digit.util.GeoUtil;
import digit.web.models.Boundary;
import digit.web.models.BoundaryRequest;
import digit.web.models.PointGeometry;
import digit.web.models.PolygonGeometry;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class BoundaryEntityValidator {

    private final ObjectMapper objectMapper;
    private final ServiceRequestRepository boundaryRepository;

    @Autowired
    public BoundaryEntityValidator(ObjectMapper objectMapper, ServiceRequestRepository boundaryRepository) {
        this.objectMapper = objectMapper;
        this.boundaryRepository = boundaryRepository;
    }

    /**
     * This method is used to validate the create boundary entity request
     * Validate for valid geometry
     * @param boundaryRequest
     */
    public void validateCreateBoundaryRequest(BoundaryRequest boundaryRequest) {

        // validate the geometry
        validateBoundaryGeometry(boundaryRequest.getBoundary());

        // validate for unique tenantId and code
        validateUniqueTenantIdAndCode(boundaryRequest);
    }

    /**
     * This method is used to validate the update boundary entity request
     * @param boundaryRequest
     */
    public void validateUpdateBoundaryRequest(BoundaryRequest boundaryRequest) {

        // validate for code and tenantId to exist
        validateIfCodeAndTenantIdExist(boundaryRequest);

        // validate for valid geometry
        validateBoundaryGeometry(boundaryRequest.getBoundary());
    }

    /**
     * This method takes a list of boundary entities and validates geometry of
     * the boundary depending on its type.
     * @param boundaryList
     */
    private void validateBoundaryGeometry(List<Boundary> boundaryList) {
        Map<String, String> exceptions = new HashMap<>();

        boundaryList.forEach(boundary -> {
            // Only execute if geometry is present
            if(boundary.getGeometry()!=null) {
                try {
                    if(boundary.getGeometry().get("type").asText().equals("Point")) {
                        GeoUtil.validatePointGeometry(objectMapper.treeToValue(boundary.getGeometry(), PointGeometry.class), exceptions);
                    } else if(boundary.getGeometry().get("type").asText().equals("Polygon")) {
                        GeoUtil.validatePolygonGeometry(objectMapper.treeToValue(boundary.getGeometry(), PolygonGeometry.class), exceptions);
                    } else {
                        throw new CustomException(ErrorCodes.INVALID_GEOMETRY_TYPE_CODE, ErrorCodes.INVALID_GEOMETRY_TYPE_MSG);
                    }
                } catch (JsonProcessingException e) {
                    throw new CustomException(ErrorCodes.INVALID_GEOJSON_CODE, ErrorCodes.INVALID_GEOJSON_MSG);
                }
            }
        });

        ErrorUtil.throwCustomExceptions(exceptions);
    }

    /**
     * This method is used to create a map of tenantId to code from the request
     * @param boundaryRequest
     * @return
     */
    public Map<String, Set<String>> createTenantIdtoCodeMap(BoundaryRequest boundaryRequest) {
        return boundaryRequest.getBoundary().stream()
                .collect(Collectors.groupingBy(Boundary::getTenantId, Collectors.mapping(Boundary::getCode, Collectors.toSet())));
    }

    /**
     * This method is used to validate the uniqueness of tenantId and code in the request
     * @param boundaryRequest
     */
    public void validateUniqueTenantIdAndCode(BoundaryRequest boundaryRequest) {

        // create a map of tenantId to code from request
        Map<String, Set<String>> tenantIdToCodeMap = createTenantIdtoCodeMap(boundaryRequest);

        tenantIdToCodeMap.forEach((tenantId, codes) -> {

            // get the set of codes for a given tenantId from db
            Set<String> codeSet = boundaryRepository.getCodeListByTenantId(tenantId);

            // check if the code already exists in dbb
            for (String code:codes) {
                if(codeSet.contains(code))
                    throw new CustomException(ErrorCodes.DUPLICATE_CODE_CODE,ErrorCodes.DUPLICATE_CODE_MSG + " (" + tenantId + "," + code + ")" );
            }
        });
    }

    /**
     * This method is used to validate if the code and tenantId exist in the db before updating
     * @param boundaryRequest
     */
    public void validateIfCodeAndTenantIdExist(BoundaryRequest boundaryRequest) {

            // create a map of tenantId to code from request
            Map<String, Set<String>> tenantIdToCodeMap = createTenantIdtoCodeMap(boundaryRequest);

            tenantIdToCodeMap.forEach((tenantId, codes) -> {

                // get the set of codes for a given tenantId from db
                Set<String> codeSet = boundaryRepository.getCodeListByTenantId(tenantId);

                // check if the code already exists in dbb
                for (String code:codes) {
                    if(!codeSet.contains(code))
                        throw new CustomException(ErrorCodes.NOT_FOUND_CODE_AND_TENANT_ID_CODE,ErrorCodes.NOT_FOUND_CODE_AND_TENANT_ID_MSG + " (" + tenantId + "," + code + ")" );
                }
            });
    }
}
