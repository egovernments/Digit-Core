package org.egov.web.contract;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;
import org.egov.domain.model.MessageIdentity;
import org.egov.domain.model.Tenant;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DeleteMessagesRequest {

	@JsonProperty("RequestInfo")
	private RequestInfo requestInfo;

	@NotNull
    @Size(max = 256)
	private String tenantId;

	@Valid
	@Size(min = 1)
	private List<DeleteMessage> messages;

	public List<MessageIdentity> getMessageIdentities() {
		return messages.stream().map(message -> MessageIdentity.builder().code(message.getCode())
				.module(message.getModule()).locale(message.getLocale()).tenant(getTenant()).build())
				.collect(Collectors.toList());
	}

	public Tenant getTenant() {
		return new Tenant(tenantId);
	}

}
