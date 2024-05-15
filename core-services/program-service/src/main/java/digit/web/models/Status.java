package digit.web.models;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Builder;

/**
 * Status
 */
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-05-02T12:10:17.717685845+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Status   {
            /**
            * Gets or Sets statusCode
            */
            public enum StatusCodeEnum {
                        RECEIVED("RECEIVED"),
                        
                        APPROVED("APPROVED"),
                        
                        REJECTED("REJECTED"),
                        
                        INPROCESS("INPROCESS"),
                        
                        INITIATED("INITIATED"),
                        
                        SUCCESSFUL("SUCCESSFUL"),
                        
                        FAILED("FAILED"),
                        
                        PARTIAL("PARTIAL"),
                        
                        CANCELLED("CANCELLED"),
                        
                        COMPLETED("COMPLETED"),
                        
                        ERROR("ERROR"),
                        
                        INFO("INFO"),
                        
                        ACTIVE("ACTIVE"),
                        
                        INACTIVE("INACTIVE");
            
            private String value;
            
            StatusCodeEnum(String value) {
            this.value = value;
            }
            
            @Override
            @JsonValue
            public String toString() {
            return String.valueOf(value);
            }
            
            @JsonCreator
            public static StatusCodeEnum fromValue(String text) {
            for (StatusCodeEnum b : StatusCodeEnum.values()) {
            if (String.valueOf(b.value).equals(text)) {
            return b;
            }
            }
            return null;
            }
            }        @JsonProperty("statusCode")

                private StatusCodeEnum statusCode = null;

        @JsonProperty("statusMessage")

                private String statusMessage = null;


}
