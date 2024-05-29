package digit.web.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

/**
 * Status
 */
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-05-02T12:10:17.717685845+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class 	Status {
	@JsonProperty("statusCode")

	private StatusCodeEnum statusCode = null;
	@JsonProperty("statusMessage")

	private String statusMessage = null;

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

		@JsonCreator
		public static StatusCodeEnum fromValue(String text) {
			for (StatusCodeEnum b : StatusCodeEnum.values()) {
				if (String.valueOf(b.value).equals(text)) {
					return b;
				}
			}
			return null;
		}

		@Override
		@JsonValue
		public String toString() {
			return String.valueOf(value);
		}
	}


}
