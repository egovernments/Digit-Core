package digit.service;

import digit.config.ApplicationProperties;
import digit.kafka.Producer;
import digit.repository.impl.BoundaryRepositoryImpl;
import digit.service.enrichment.BoundaryEntityEnricher;
import digit.service.validator.BoundaryEntityValidator;
import digit.util.ResponseUtil;
import digit.web.models.Boundary;
import digit.web.models.BoundaryRequest;
import digit.web.models.BoundaryResponse;
import digit.web.models.BoundarySearchCriteria;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.common.utils.ResponseInfoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BoundaryService {

    private final BoundaryEntityValidator boundaryEntityValidator;
    private final Producer producer;
    private final ResponseUtil responseUtil;
    private final ApplicationProperties configuration;
    private final BoundaryRepositoryImpl repository;

    @Autowired
    public BoundaryService(BoundaryEntityValidator boundaryEntityValidator, Producer producer, ResponseUtil responseUtil, ApplicationProperties configuration, BoundaryRepositoryImpl repository) {
        this.boundaryEntityValidator = boundaryEntityValidator;
        this.producer = producer;
        this.responseUtil = responseUtil;
        this.configuration = configuration;
        this.repository = repository;
    }

    /**
     * This method is used to create a boundary entity
     * @param boundaryRequest is the request object
     * @return boundaryResponse
     */
    public BoundaryResponse createBoundary(BoundaryRequest boundaryRequest) {

        // validate the request
        boundaryEntityValidator.validateCreateBoundaryRequest(boundaryRequest);

        // enrich the request
        BoundaryEntityEnricher.enrichCreateBoundaryRequest(boundaryRequest);

        // create response
        BoundaryResponse boundaryResponse = responseUtil.createBoundaryResponse(boundaryRequest);

        // push to kafka
        repository.create(boundaryRequest);

        return boundaryResponse;
    }

    /**
     * This method is used to search for boundary entity
     * @param boundarySearchCriteria
     * @return
     */
    public BoundaryResponse searchBoundary(BoundarySearchCriteria boundarySearchCriteria, RequestInfo requestInfo) {

        // Search for boundary entity
        List<Boundary> boundaryList = repository.search(boundarySearchCriteria);

        // create response info
        ResponseInfo responseInfo = ResponseInfoUtil.createResponseInfoFromRequestInfo(requestInfo,Boolean.TRUE);

        // create response
        BoundaryResponse boundaryResponse = BoundaryResponse.builder().boundary(boundaryList).responseInfo(responseInfo).build();

        return boundaryResponse;

    }

    public BoundaryResponse updateBoundary(BoundaryRequest boundaryRequest) {

        // validate the request
        boundaryEntityValidator.validateUpdateBoundaryRequest(boundaryRequest);

        // enrich the request
        BoundaryEntityEnricher.enrichUpdateBoundaryRequest(boundaryRequest);

        // create response
        BoundaryResponse boundaryResponse = responseUtil.createBoundaryResponse(boundaryRequest);

        // push to kafka
        repository.update(boundaryRequest);

        return boundaryResponse;
    }
}
