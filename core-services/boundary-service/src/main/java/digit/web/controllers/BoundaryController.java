package digit.web.controllers;

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

@Controller
@RequestMapping("/boundary")
public class BoundaryController {

    private BoundaryService boundaryService;

    private ReverseGeocodingService reverseGeocodingService;

    @Autowired
    public BoundaryController(BoundaryService boundaryService, ReverseGeocodingService reverseGeocodingService) {
        this.boundaryService = boundaryService;
        this.reverseGeocodingService = reverseGeocodingService;
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
        String stateResponse = reverseGeocodingService.getState(body);
        return new ResponseEntity<>(stateResponse, HttpStatus.ACCEPTED);
    }

}
