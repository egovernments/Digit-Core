package digit.web.models;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import digit.web.models.User;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Builder;

/**
 * RequestInfo should be used to carry meta information about the requests to the server as described in the fields below. All eGov APIs will use requestinfo as a part of the request body to carry this meta information. Some of this information will be returned back from the server as part of the ResponseInfo in the response body to ensure correlation.
 */
@Schema(description = "RequestInfo should be used to carry meta information about the requests to the server as described in the fields below. All eGov APIs will use requestinfo as a part of the request body to carry this meta information. Some of this information will be returned back from the server as part of the ResponseInfo in the response body to ensure correlation.")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-03-14T17:06:34.078752728+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestInfo   {
        @JsonProperty("apiId")
          @NotNull

        @Size(max=128)         private String apiId = null;

        @JsonProperty("ver")
          @NotNull

        @Size(max=32)         private String ver = null;

        @JsonProperty("ts")
          @NotNull

                private Long ts = null;

        @JsonProperty("action")
          @NotNull

        @Size(max=32)         private String action = null;

        @JsonProperty("did")

        @Size(max=1024)         private String did = null;

        @JsonProperty("key")

        @Size(max=256)         private String key = null;

        @JsonProperty("msgId")
          @NotNull

        @Size(max=256)         private String msgId = null;

        @JsonProperty("requesterId")

        @Size(max=256)         private String requesterId = null;

        @JsonProperty("authToken")

                private String authToken = null;

        @JsonProperty("userInfo")

          @Valid
                private User userInfo = null;

        @JsonProperty("correlationId")

                private String correlationId = null;


}
