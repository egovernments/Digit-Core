package digit.web.models;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Builder;

/**
 * This object holds list of documents attached during the transaciton for a property
 */
@Schema(description = "This object holds list of documents attached during the transaciton for a property")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-03-12T13:40:42.981935029+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Document   {
        @JsonProperty("id")

        @Size(max=64)         private String id = null;

        @JsonProperty("documentType")

                private String documentType = null;

        @JsonProperty("fileStore")

                private String fileStore = null;

        @JsonProperty("documentUid")

        @Size(max=64)         private String documentUid = null;

        @JsonProperty("additionalDetails")

                private Object additionalDetails = null;


}
