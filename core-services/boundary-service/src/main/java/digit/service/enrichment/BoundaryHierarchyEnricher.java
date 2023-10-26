package digit.service.enrichment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import digit.web.models.BoundaryTypeHierarchy;
import digit.web.models.BoundaryTypeHierarchyRequest;
import org.egov.common.utils.AuditDetailsEnrichmentUtil;
import org.egov.common.utils.UUIDEnrichmentUtil;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BoundaryHierarchyEnricher {

    private ObjectMapper objectMapper;

    public BoundaryHierarchyEnricher(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Method to enrich id, audit details and boundary hierarchy json node.
     * @param body
     */
    public void enrichBoundaryHierarchyDefinition(BoundaryTypeHierarchyRequest body) {
        UUIDEnrichmentUtil.enrichRandomUuid(body.getBoundaryHierarchy(), "id");
        body.getBoundaryHierarchy().setAuditDetails(AuditDetailsEnrichmentUtil.prepareAuditDetails(body.getBoundaryHierarchy().getAuditDetails(), body.getRequestInfo(), Boolean.TRUE));
        body.getBoundaryHierarchy().setBoundaryHierarchyJsonNode(getBoundaryHierarchyJsonNode(body.getBoundaryHierarchy().getBoundaryHierarchy()));
    }

    /**
     * Method to convert list of boundary hierarchy POJOs to JsonNode for persisting.
     * @param boundaryHierarchyList
     * @return
     */
    private JsonNode getBoundaryHierarchyJsonNode(List<BoundaryTypeHierarchy> boundaryHierarchyList) {
        try {
            String jsonString = objectMapper.writeValueAsString(boundaryHierarchyList);
            JsonNode jsonNode = objectMapper.readTree(jsonString);
            return jsonNode;
        } catch (Exception e) {
            throw new CustomException("JSON_PARSING_ERROR", "Error in converting boundary hierarchy list to JSON");
        }
    }

}
