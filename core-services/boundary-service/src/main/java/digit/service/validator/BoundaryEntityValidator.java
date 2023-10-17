package digit.service.validator;

import digit.web.models.BoundaryRequest;
import org.springframework.stereotype.Component;

@Component
public class BoundaryEntityValidator {

    /**
     * This method is used to validate the create boundary entity request
     * Validate for valid geometry
     * @param boundaryRequest
     */
    public void validateCreateBoundaryRequest(BoundaryRequest boundaryRequest) {
        // validate the request
    }
}
