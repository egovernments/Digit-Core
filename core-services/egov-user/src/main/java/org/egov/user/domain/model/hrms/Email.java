package org.egov.user.domain.model.hrms;

import java.util.Set;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Email {

    @NotNull
    @Size(min = 1, message = "At least one recipient is required")
    private Set<String> emailTo;

    @NotNull(message = "Subject is required")
    private String subject;

    @NotNull(message = "Body is required")
    private String body;
    @JsonProperty("isHTML")
    private boolean isHTML;

}
