package digit.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PolygonGeometry {

    @JsonProperty("type")
    private String type;

    @JsonProperty("coordinates")
    private List<List<List<Double>>> coordinates;

}
