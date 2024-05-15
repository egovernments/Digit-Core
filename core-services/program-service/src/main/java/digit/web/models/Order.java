package digit.web.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Sorting order
 */
public enum Order {
	ASC("asc"), DESC("desc");

	private String value;

	Order(String value) {
		this.value = value;
	}

	@JsonCreator
	public static Order fromValue(String text) {
		for (Order b : Order.values()) {
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
