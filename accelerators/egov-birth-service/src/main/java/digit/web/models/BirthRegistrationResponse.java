package digit.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;

import org.egov.common.contract.response.ResponseInfo;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Builder;

/**
 * Contract class to send response. Array of  items are used in case of search results or response for create, whereas single  item is used for update
 */
@Schema(description = "Contract class to send response. Array of  items are used in case of search results or response for create, whereas single  item is used for update")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-04-13T14:58:44.100316977+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BirthRegistrationResponse   {
        @JsonProperty("ResponseInfo")

          @Valid
                private ResponseInfo responseInfo = null;

        @JsonProperty("BirthRegistrationApplications")
          @Valid
                private List<BirthRegistrationApplication> birthRegistrationApplications = null;


        public BirthRegistrationResponse addBirthRegistrationApplicationsItem(BirthRegistrationApplication birthRegistrationApplicationsItem) {
            if (this.birthRegistrationApplications == null) {
            this.birthRegistrationApplications = new ArrayList<>();
            }
        this.birthRegistrationApplications.add(birthRegistrationApplicationsItem);
        return this;
        }

}
