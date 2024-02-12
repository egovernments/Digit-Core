package org.egov.boundary.web.contract;

import jakarta.validation.Valid;

import org.egov.boundary.domain.model.CrossHierarchy;
import org.egov.common.contract.request.RequestInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CrossHierarchySearchRequest {

	@Valid
	@JsonProperty("RequestInfo")
	private RequestInfo requestInfo = null;
	@Valid
	@JsonProperty("CrossHierarchy")
	private CrossHierarchy  crossHierarchy  = null;

	public RequestInfo getRequestInfo() {
		return requestInfo;
	}

	public void setRequestInfo(RequestInfo requestInfo) {
		this.requestInfo = requestInfo;
	}

	public CrossHierarchy getCrossHierarchy() {
		return crossHierarchy;
	}

	public void setCrossHierarchy(CrossHierarchy crossHierarchy) {
		this.crossHierarchy = crossHierarchy;
	}
	
}
