package digit.web.models;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ResponseInfo should be used to carry metadata information about the response from the server. apiId, ver and msgId in ResponseInfo should always correspond to the same values in respective request&#x27;s RequestInfo.
 */
@Schema(description = "ResponseInfo should be used to carry metadata information about the response from the server. apiId, ver and msgId in ResponseInfo should always correspond to the same values in respective request's RequestInfo.")
@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2023-06-20T09:54:35.237+05:30[Asia/Calcutta]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseInfo   {
        @JsonProperty("apiId")
          @NotNull

        @Size(max=128)         private String apiId = null;

        @JsonProperty("ver")
          @NotNull

        @Size(max=32)         private String ver = null;

        @JsonProperty("ts")
          @NotNull

                private Long ts = null;

        @JsonProperty("resMsgId")

        @Size(max=256)         private String resMsgId = null;

        @JsonProperty("msgId")

        @Size(max=256)         private String msgId = null;

            /**
            * status of request processing - to be enhanced in futuer to include INPROGRESS
            */
            public enum StatusEnum {
                        SUCCESSFUL("SUCCESSFUL"),

                        FAILED("FAILED");

            private String value;

            StatusEnum(String value) {
            this.value = value;
            }

            @Override
            @JsonValue
            public String toString() {
            return String.valueOf(value);
            }

            @JsonCreator
            public static StatusEnum fromValue(String text) {
            for (StatusEnum b : StatusEnum.values()) {
            if (String.valueOf(b.value).equals(text)) {
            return b;
            }
            }
            return null;
            }
            }        @JsonProperty("status")
          @NotNull

                private StatusEnum status = null;


}
