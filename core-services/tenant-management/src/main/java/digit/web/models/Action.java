package digit.web.models;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import digit.web.models.AuditDetails;
import io.swagger.v3.oas.annotations.media.Schema;
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
 * Details of an action
 */
@Schema(description = "Details of an action")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-08-12T11:40:14.091712534+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Action   {
        @JsonProperty("id")

          @Valid
                private UUID id = null;

        @JsonProperty("uri")
          @NotNull

        @Size(min=1,max=256)         private String uri = null;

        @JsonProperty("description")
          @NotNull

        @Size(min=1,max=256)         private String description = null;

        @JsonProperty("isActive")

                private Boolean isActive = null;

        @JsonProperty("tag")

        @Size(min=1)         private List<String> tag = null;

                /**
                * Gets or Sets accessType
                */
                public enum AccessTypeEnum {
                            OPEN("OPEN"),
                            
                            PROTECTED("PROTECTED");
                
                private String value;
                
                AccessTypeEnum(String value) {
                this.value = value;
                }
                
                @Override
                @JsonValue
                public String toString() {
                return String.valueOf(value);
                }
                
                @JsonCreator
                public static AccessTypeEnum fromValue(String text) {
                for (AccessTypeEnum b : AccessTypeEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                return b;
                }
                }
                return null;
                }
                }        @JsonProperty("accessType")
          @NotNull

        @Size(min=1)         private List<AccessTypeEnum> accessType = new ArrayList<>();

        @JsonProperty("auditDetails")

          @Valid
                private AuditDetails auditDetails = null;


        public Action addTagItem(String tagItem) {
            if (this.tag == null) {
            this.tag = new ArrayList<>();
            }
        this.tag.add(tagItem);
        return this;
        }

        public Action addAccessTypeItem(AccessTypeEnum accessTypeItem) {
        this.accessType.add(accessTypeItem);
        return this;
        }

}
