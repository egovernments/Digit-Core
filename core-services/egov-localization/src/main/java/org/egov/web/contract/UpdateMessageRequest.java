package org.egov.web.contract;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;
import org.egov.domain.model.AuthenticatedUser;
import org.egov.domain.model.MessageIdentity;
import org.egov.domain.model.NotAuthenticatedException;
import org.egov.domain.model.Tenant;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateMessageRequest {
	@JsonProperty("RequestInfo")
	private RequestInfo requestInfo;
	@NotEmpty
    @Size(max = 256)
	private String tenantId;
	@NotEmpty
    @Size(max = 255)
	private String locale;
	@NotEmpty
    @Size(max = 255)
	private String module;
	@Size(min = 1)
	@Valid
	private List<UpdateMessage> messages;

	public List<org.egov.domain.model.Message> toDomainMessages() {
		return messages.stream().map(message -> {
			final MessageIdentity messageIdentity = MessageIdentity.builder().code(message.getCode()).module(module)
					.locale(locale).tenant(getTenant()).build();
			return org.egov.domain.model.Message.builder().message(message.getMessage())
					.messageIdentity(messageIdentity).build();
		}).collect(Collectors.toList());
	}

	public Tenant getTenant() {
		return new Tenant(tenantId);
	}

	public AuthenticatedUser getAuthenticatedUser() {
		if (requestInfo == null || requestInfo.getUserInfo() == null || requestInfo.getUserInfo().getId() == null) {
			throw new NotAuthenticatedException();
		}
		return new AuthenticatedUser(requestInfo.getUserInfo().getId());
	}

}
