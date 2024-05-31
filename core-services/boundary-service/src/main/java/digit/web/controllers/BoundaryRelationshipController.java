package digit.web.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import digit.service.BoundaryRelationshipService;
import digit.web.models.*;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/boundary-relationships")
public class BoundaryRelationshipController {

    private BoundaryRelationshipService boundaryRelationshipService;

    @Autowired
    public BoundaryRelationshipController(BoundaryRelationshipService boundaryRelationshipService) {
        this.boundaryRelationshipService = boundaryRelationshipService;
    }

    /**
     * Request handler for serving boundary relationships create request.
     * @param body
     * @return
     */
    @RequestMapping(value = "/_create", method = RequestMethod.POST)
    public ResponseEntity<BoundaryRelationshipResponse> create(@Valid @RequestBody BoundaryRelationshipRequest body) {
        BoundaryRelationshipResponse boundaryRelationshipResponse = boundaryRelationshipService.createBoundaryRelationship(body);
        return new ResponseEntity<>(boundaryRelationshipResponse, HttpStatus.ACCEPTED);
    }

    /**
     * Request handler for serving boundary relationships search request.
     * @param boundaryRelationshipSearchCriteria
     * @param requestInfo
     * @return
     */
    @RequestMapping(value = "/_search", method = RequestMethod.POST)
    public ResponseEntity<BoundarySearchResponse> search(@Valid @ModelAttribute BoundaryRelationshipSearchCriteria boundaryRelationshipSearchCriteria, @RequestBody RequestInfo requestInfo) {
        BoundarySearchResponse boundarySearchResponse = boundaryRelationshipService.getBoundaryRelationships(boundaryRelationshipSearchCriteria, requestInfo);
        return new ResponseEntity<>(boundarySearchResponse, HttpStatus.OK);
    }

    /**
     * Request handler for serving boundary relationships update request.
     * @param body
     * @return
     */
    @RequestMapping(value = "/_update", method = RequestMethod.POST)
    public ResponseEntity<BoundaryRelationshipResponse> update(@Valid @RequestBody BoundaryRelationshipRequest body) {
        BoundaryRelationshipResponse boundaryRelationshipResponse = boundaryRelationshipService.updateBoundaryRelationship(body);
        return new ResponseEntity<>(boundaryRelationshipResponse, HttpStatus.ACCEPTED);
    }

}
