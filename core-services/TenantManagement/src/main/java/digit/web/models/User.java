package digit.web.models;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import digit.web.models.AuditDetails;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Builder;

/**
 * Details of a user
 */
@Schema(description = "Details of a user")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-08-12T11:40:14.091712534+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User   {
        @JsonProperty("id")

          @Valid
                private UUID id = null;

        @JsonProperty("tenantId")
          @NotNull

        @Size(min=1,max=50)         private String tenantId = null;

        @JsonProperty("userName")
          @NotNull

        @Size(min=1,max=50)         private String userName = null;

        @JsonProperty("email")

        @Size(min=1,max=50)         private String email = null;

        @JsonProperty("emailVarified")

                private Boolean emailVarified = null;

        @JsonProperty("mobile")

        @Size(min=1,max=50)         private String mobile = null;

        @JsonProperty("mobileVarified")

                private Boolean mobileVarified = null;

            /**
            * Type of login for the user
            */
            public enum LoginTypeEnum {
                        PASSWORD("Password"),
                        
                        OTP("OTP"),
                        
                        _2FA("2FA");
            
            private String value;
            
            LoginTypeEnum(String value) {
            this.value = value;
            }
            
            @Override
            @JsonValue
            public String toString() {
            return String.valueOf(value);
            }
            
            @JsonCreator
            public static LoginTypeEnum fromValue(String text) {
            for (LoginTypeEnum b : LoginTypeEnum.values()) {
            if (String.valueOf(b.value).equals(text)) {
            return b;
            }
            }
            return null;
            }
            }        @JsonProperty("loginType")
          @NotNull

                private LoginTypeEnum loginType = null;

        @JsonProperty("loginCount")

          @Valid
                private BigDecimal loginCount = null;

        @JsonProperty("roles")
          @NotNull

                private List<Object> roles = new ArrayList<>();

        @JsonProperty("auditDetails")

          @Valid
                private AuditDetails auditDetails = null;


        public User addRolesItem(Object rolesItem) {
        this.roles.add(rolesItem);
        return this;
        }

}
