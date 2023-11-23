package digit.web.controllers;

import digit.service.BoundaryMigrate;
import digit.service.BoundaryService;
import digit.web.models.*;
import digit.web.models.legacy.BoundaryMigrateRequest;
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

    private BoundaryMigrate boundaryMigrate;

    @Autowired
    public BoundaryController(BoundaryService boundaryService, BoundaryMigrate boundaryMigrate) {
        this.boundaryService = boundaryService;
        this.boundaryMigrate = boundaryMigrate;
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

    /**
     * Migrate boundary data from old format to new format
     * @param requestBody
     * @return
     */
    @RequestMapping(value = "/_migrate", method = RequestMethod.POST)
    public ResponseEntity<BoundaryResponse> boundaryMigrate(@RequestBody BoundaryMigrateRequest requestBody) {
        boundaryMigrate.migrate(requestBody);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
