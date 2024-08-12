package digit.web.models;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import digit.web.models.AuditDetails;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Builder;

/**
 * Details of Tenant Configuration
 */
@Schema(description = "Details of Tenant Configuration")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-08-12T11:40:14.091712534+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TenantConfig   {
        @JsonProperty("id")

          @Valid
                private UUID id = null;

        @JsonProperty("headerUrl")

        @Size(min=1,max=20)         private String headerUrl = null;

        @JsonProperty("footerUrl")

        @Size(min=1,max=100)         private String footerUrl = null;

            /**
            * The tenant admin can configure a default login type for all users within the tenant.
            */
            public enum DefaultLoginTypeEnum {
                        PASSWORD("Password"),
                        
                        OTP("OTP"),
                        
                        _2FA("2FA");
            
            private String value;
            
            DefaultLoginTypeEnum(String value) {
            this.value = value;
            }
            
            @Override
            @JsonValue
            public String toString() {
            return String.valueOf(value);
            }
            
            @JsonCreator
            public static DefaultLoginTypeEnum fromValue(String text) {
            for (DefaultLoginTypeEnum b : DefaultLoginTypeEnum.values()) {
            if (String.valueOf(b.value).equals(text)) {
            return b;
            }
            }
            return null;
            }
            }        @JsonProperty("defaultLoginType")

                private DefaultLoginTypeEnum defaultLoginType = null;

        @JsonProperty("enableUserBasedLogin")

                private Boolean enableUserBasedLogin = null;

        @JsonProperty("additionalAttributes")

                private Object additionalAttributes = null;

        @JsonProperty("isActive")

                private Boolean isActive = null;

        @JsonProperty("auditDetails")

          @Valid
                private AuditDetails auditDetails = null;


}
