package org.egov.user.domain.model.project;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

/**
* Field
*/
@Validated


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Field {
    @JsonProperty("key")
    @NotNull
    @Size(min = 2, max = 64)
    private String key = null;

    @JsonProperty("value")
    @NotNull
    @Size(min = 1, max = 10000)
    private String value = null;

}

