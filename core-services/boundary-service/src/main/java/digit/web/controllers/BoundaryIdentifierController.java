package digit.web.controllers;

import digit.service.BoundaryIdentifierService;
import digit.web.models.*;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/boundary-identifier")
public class BoundaryIdentifierController {

    private BoundaryIdentifierService boundaryIdentifierService;

    @Autowired
    public BoundaryIdentifierController(BoundaryIdentifierService boundaryIdentifierService) {
        this.boundaryIdentifierService = boundaryIdentifierService;
    }

    /**
     * Request handler for serving bulk boundary identifier create request.
     * @param body
     * @return
     */
    @RequestMapping(value = "/_create", method = RequestMethod.POST)
    public ResponseEntity<BoundaryIdentifierResponse> create(@Valid @RequestBody BoundaryIdentifierRequest body) {
        BoundaryIdentifierResponse response = boundaryIdentifierService.createBoundaryIdentifier(body);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    /**
     * Request handler for serving boundary identifier search request.
     * @param criteria
     * @param requestInfo
     * @return
     */
    @RequestMapping(value = "/_search", method = RequestMethod.POST)
    public ResponseEntity<BoundaryIdentifierMapResponse> search(@Valid @ModelAttribute BoundaryIdentifierSearchCriteria criteria, @RequestBody RequestInfo requestInfo) {
        BoundaryIdentifierMapResponse response = boundaryIdentifierService.searchBoundaryIdentifier(criteria, requestInfo);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Request handler for serving bulk boundary identifier update request.
     * @param body
     * @return
     */
    @RequestMapping(value = "/_update", method = RequestMethod.POST)
    public ResponseEntity<BoundaryIdentifierResponse> update(@Valid @RequestBody BoundaryIdentifierRequest body) {
        BoundaryIdentifierResponse response = boundaryIdentifierService.updateBoundaryIdentifier(body);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

}
