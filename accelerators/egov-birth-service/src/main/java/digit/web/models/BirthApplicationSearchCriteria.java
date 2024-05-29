package digit.web.models;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;
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
 * BirthApplicationSearchCriteria
 */
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-04-13T14:58:44.100316977+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BirthApplicationSearchCriteria   {
        @JsonProperty("tenantId")
          @NotNull

                private String tenantId = null;

        @JsonProperty("status")

                private String status = null;

        @JsonProperty("ids")

        @Size(max=50)         private List<String> ids = null;

        @JsonProperty("applicationNumber")

        @Size(min=2,max=64)         private String applicationNumber = null;


        public BirthApplicationSearchCriteria addIdsItem(String idsItem) {
            if (this.ids == null) {
            this.ids = new ArrayList<>();
            }
        this.ids.add(idsItem);
        return this;
        }

}
