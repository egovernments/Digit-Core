package org.egov.pgr.web.models.individual;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonIgnoreProperties(
		ignoreUnknown = true
)
public enum Gender {
	MALE("MALE"),
	FEMALE("FEMALE"),
	OTHER("OTHER"),
	TRANSGENDER("TRANSGENDER");

	private String value;

	private Gender(String value) {
		this.value = value;
	}

	@JsonValue
	public String toString() {
		return String.valueOf(this.value);
	}

	@JsonCreator
	public static Gender fromValue(String text) {
		for(Gender b : values()) {
			if (String.valueOf(b.value).equals(text)) {
				return b;
			}
		}

		return null;
	}
}

