package digit.service.enrichment;

import digit.web.models.BoundaryRequest;
import org.egov.common.utils.UUIDEnrichmentUtil;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class BoundaryEntityEnricher {

    /**
     *  Enrich the create boundary request
     */
    public void enrichCreateBoundaryRequest(BoundaryRequest boundaryRequest) {
        boundaryRequest.getBoundary().forEach(boundary -> {
            UUIDEnrichmentUtil.enrichRandomUuid(boundary,"id");
        });
    }
}
