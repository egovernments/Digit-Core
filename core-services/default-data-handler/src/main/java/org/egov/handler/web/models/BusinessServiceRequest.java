package org.egov.handler.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.workflow.BusinessService;

import java.util.ArrayList;
import java.util.List;

@Data
@ToString
public class BusinessServiceRequest {

	@JsonProperty("RequestInfo")
	private RequestInfo requestInfo;

	@JsonProperty("BusinessServices")
	@Valid
	@NotNull
	private List<BusinessService> businessServices;


	public BusinessServiceRequest addBusinessServiceItem(BusinessService businessServiceItem) {
		if (this.businessServices == null) {
			this.businessServices = new ArrayList<>();
		}
		this.businessServices.add(businessServiceItem);
		return this;
	}



}

