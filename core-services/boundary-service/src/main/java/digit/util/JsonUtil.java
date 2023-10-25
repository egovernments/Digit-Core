package digit.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import digit.web.models.BoundaryTypeHierarchy;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JsonUtil {

    private ObjectMapper objectMapper;

    public JsonUtil(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public JsonNode getBoundaryHierarchyJsonNode(List<BoundaryTypeHierarchy> boundaryHierarchyList) {
        try {
            String jsonString = objectMapper.writeValueAsString(boundaryHierarchyList);
            JsonNode jsonNode = objectMapper.readTree(jsonString);
            return jsonNode;
        } catch (Exception e) {
            throw new CustomException("JSON_PARSING_ERROR", "Error in converting boundary hierarchy list to JSON");
        }
    }

}
