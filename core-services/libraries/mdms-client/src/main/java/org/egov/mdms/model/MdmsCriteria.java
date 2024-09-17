package org.egov.mdms.model;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.egov.mdms.model.MasterDetail.MasterDetailBuilder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MdmsCriteria {
	
	@NotNull
	@Size(max=256)
	private String tenantId;
	
	@NotNull
	@Valid
	private List<ModuleDetail> moduleDetails;

}
