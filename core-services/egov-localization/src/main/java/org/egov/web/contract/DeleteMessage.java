package org.egov.web.contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotEmpty;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DeleteMessage {

    @NotEmpty
    private String code;

	@NotEmpty
    private String module;

	@NotEmpty
	private String locale;
}
