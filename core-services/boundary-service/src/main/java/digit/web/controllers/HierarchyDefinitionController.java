package digit.web.controllers;

import digit.service.BoundaryHierarchyDefinitionService;
import digit.web.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/boundary-hierarchy-definition")
public class HierarchyDefinitionController {

    private BoundaryHierarchyDefinitionService boundaryHierarchyDefinitionService;

    @Autowired
    public HierarchyDefinitionController(BoundaryHierarchyDefinitionService boundaryHierarchyDefinitionService) {
        this.boundaryHierarchyDefinitionService = boundaryHierarchyDefinitionService;
    }

    /**
     * Request handler for serving hierarchy definition create requests.
     * @param body
     * @return
     */
    @RequestMapping(value = "/_create", method = RequestMethod.POST)
    public ResponseEntity<BoundaryTypeHierarchyResponse> create(@Valid @RequestBody BoundaryTypeHierarchyRequest body) {
        BoundaryTypeHierarchyResponse boundaryTypeHierarchyResponse = boundaryHierarchyDefinitionService.createBoundaryHierarchyDefinition(body);
        return new ResponseEntity<>(boundaryTypeHierarchyResponse, HttpStatus.ACCEPTED);
    }

    /**
     * Request handler for serving hierarchy definition search requests.
     * @param body
     * @return
     */
    @RequestMapping(value = "/_search", method = RequestMethod.POST)
    public ResponseEntity<BoundaryTypeHierarchyResponse> search(@Valid @RequestBody BoundaryTypeHierarchySearchRequest body) {
        BoundaryTypeHierarchyResponse boundaryTypeHierarchyResponse = boundaryHierarchyDefinitionService.searchBoundaryHierarchyDefinition(body);
        return new ResponseEntity<>(boundaryTypeHierarchyResponse, HttpStatus.OK);
    }

}
