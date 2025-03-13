package org.egov.wf.web.models.individual;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

/**
 * AdditionalFields
 */
@Validated


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdditionalFields {
	@JsonProperty("schema")
	@Size(min = 2, max = 64)
	private String schema = null;

	@JsonProperty("version")
	@Min(1)
	private Integer version = null;

	@JsonProperty("fields")
	@Valid
	private List<Field> fields = null;


	public AdditionalFields addFieldsItem(Field fieldsItem) {
		if (this.fields == null) {
			this.fields = new ArrayList<>();
		}
		this.fields.add(fieldsItem);
		return this;
	}

}



