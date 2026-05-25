package digit.web.models;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Builder;

/**
 * Geometry
 */
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-03-14T17:06:34.078752728+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Geometry   {
            /**
            * Gets or Sets type
            */
            public enum TypeEnum {
                        POLYGON("Polygon"),
                        
                        POINT("Point");
            
            private String value;
            
            TypeEnum(String value) {
            this.value = value;
            }
            
            @Override
            @JsonValue
            public String toString() {
            return String.valueOf(value);
            }
            
            @JsonCreator
            public static TypeEnum fromValue(String text) {
            for (TypeEnum b : TypeEnum.values()) {
            if (String.valueOf(b.value).equals(text)) {
            return b;
            }
            }
            return null;
            }
            }        @JsonProperty("type")

                private TypeEnum type = null;

        @JsonProperty("coordinates")
          @Valid
                private List<List<List<BigDecimal>>> coordinates = null;


        public Geometry addCoordinatesItem(List<List<BigDecimal>> coordinatesItem) {
            if (this.coordinates == null) {
            this.coordinates = new ArrayList<>();
            }
        this.coordinates.add(coordinatesItem);
        return this;
        }

}
