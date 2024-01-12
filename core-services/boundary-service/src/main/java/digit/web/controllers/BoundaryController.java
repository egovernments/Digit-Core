package digit.web.controllers;

import digit.service.BoundaryRelationshipService;
import digit.service.BoundaryService;
import digit.service.ReverseGeocodingService;
import digit.web.models.*;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/boundary")
public class BoundaryController {

    private BoundaryService boundaryService;

    private ReverseGeocodingService reverseGeocodingService;

    private BoundaryRelationshipService boundaryRelationshipService;

    @Autowired
    public BoundaryController(BoundaryService boundaryService, ReverseGeocodingService reverseGeocodingService, BoundaryRelationshipService boundaryRelationshipService) {
        this.boundaryService = boundaryService;
        this.reverseGeocodingService = reverseGeocodingService;
        this.boundaryRelationshipService = boundaryRelationshipService;
    }

    /**
     * Request handler for serving boundary entities create request.
     * @param body
     * @return
     */
    @RequestMapping(value = "/_create", method = RequestMethod.POST)
    public ResponseEntity<BoundaryResponse> create(@Valid @RequestBody BoundaryRequest body) {
        BoundaryResponse boundaryResponse = boundaryService.createBoundary(body);
        return new ResponseEntity<>(boundaryResponse, HttpStatus.ACCEPTED);
    }

    /**
     * Request handler for serving boundary entities search request.
     * @param boundarySearchCriteria
     * @param requestInfo
     * @return
     */
    @RequestMapping(value = "/_search", method = RequestMethod.POST)
    public ResponseEntity<BoundaryResponse> search(@Valid @ModelAttribute BoundarySearchCriteria boundarySearchCriteria, @RequestBody RequestInfo requestInfo) {
        BoundaryResponse boundaryResponse = boundaryService.searchBoundary(boundarySearchCriteria,requestInfo);
        return new ResponseEntity<>(boundaryResponse, HttpStatus.OK);
    }

    /**
     * Request handler for serving boundary entities update request.
     * @param body
     * @return
     */
    @RequestMapping(value = "/_update", method = RequestMethod.POST)
    public ResponseEntity<BoundaryResponse> update(@Valid @RequestBody BoundaryRequest body) {
        BoundaryResponse boundaryResponse = boundaryService.updateBoundary(body);
        return new ResponseEntity<>(boundaryResponse,HttpStatus.ACCEPTED);
    }

    @RequestMapping(value = "/_getStateFromCoordinates", method = RequestMethod.POST)
    public ResponseEntity<Object> getState(@Valid @RequestBody CoordinateRequest body) {

        List<String> stateResponse = reverseGeocodingService.getPolyNamesForPoint(body);
        BoundarySearchCriteria boundarySearchCriteria = BoundarySearchCriteria.builder()
                .codes(stateResponse)
                .tenantId(body.getTenantId())
                .build();

        BoundaryResponse boundaryResponse = boundaryService.searchBoundary(boundarySearchCriteria, body.getRequestInfo());

        BoundaryRelationshipSearchCriteria boundaryRelationshipSearchCriteria = BoundaryRelationshipSearchCriteria.builder()
                .tenantId(body.getTenantId())
                .hierarchyType(body.getHierarchyType())
                .boundaryType(body.getBoundaryType())
                .codes(stateResponse)
                .includeChildren(Boolean.TRUE)
                .includeParents(Boolean.FALSE)
                .build();

        BoundarySearchResponse boundaryRelationshipResponse = boundaryRelationshipService.getBoundaryRelationships(boundaryRelationshipSearchCriteria, body.getRequestInfo());

        return new ResponseEntity<>(boundaryRelationshipResponse, HttpStatus.OK);
    }

}