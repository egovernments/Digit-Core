package digit.web.models;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import digit.web.models.Role1;
import digit.web.models.TenantRole;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Builder;

/**
 * This is acting ID token of the authenticated user on the server. Any value provided by the clients will be ignored and actual user based on authtoken will be used on the server.
 */
@Schema(description = "This is acting ID token of the authenticated user on the server. Any value provided by the clients will be ignored and actual user based on authtoken will be used on the server.")
@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2023-10-16T17:02:11.361704+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInfo   {
        @JsonProperty("tenantId")
          @NotNull

                private String tenantId = null;

        @JsonProperty("id")

                private Integer id = null;

        @JsonProperty("userName")
          @NotNull

                private String userName = null;

        @JsonProperty("mobile")

                private String mobile = null;

        @JsonProperty("email")

                private String email = null;

        @JsonProperty("primaryrole")
          @NotNull
          @Valid
                private List<Role1> primaryrole = new ArrayList<>();

        @JsonProperty("additionalroles")
          @Valid
                private List<TenantRole> additionalroles = null;


        public UserInfo addPrimaryroleItem(Role1 primaryroleItem) {
        this.primaryrole.add(primaryroleItem);
        return this;
        }

        public UserInfo addAdditionalrolesItem(TenantRole additionalrolesItem) {
            if (this.additionalroles == null) {
            this.additionalroles = new ArrayList<>();
            }
        this.additionalroles.add(additionalrolesItem);
        return this;
        }

}
