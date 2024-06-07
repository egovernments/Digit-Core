package digit.web.models;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import digit.web.models.BirthRegistrationApplication;
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
import org.egov.common.contract.request.RequestInfo;

/**
 * Contract class to receive request. Array of  items are used in case of create, whereas single  item is used for update
 */
@Schema(description = "Contract class to receive request. Array of  items are used in case of create, whereas single  item is used for update")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-04-13T14:58:44.100316977+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BirthRegistrationRequest   {
        @JsonProperty("RequestInfo")

          @Valid
                private RequestInfo requestInfo = null;

        @JsonProperty("BirthRegistrationApplications")
          @Valid
                private List<BirthRegistrationApplication> birthRegistrationApplications = null;


        public BirthRegistrationRequest addBirthRegistrationApplicationsItem(BirthRegistrationApplication birthRegistrationApplicationsItem) {
            if (this.birthRegistrationApplications == null) {
            this.birthRegistrationApplications = new ArrayList<>();
            }
        this.birthRegistrationApplications.add(birthRegistrationApplicationsItem);
        return this;
        }

}
