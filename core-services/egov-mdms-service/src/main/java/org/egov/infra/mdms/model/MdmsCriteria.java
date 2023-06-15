package org.egov.infra.mdms.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * MdmsCriteria
 */
@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2023-05-30T09:26:57.838+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MdmsCriteria {
    @JsonProperty("tenantId")

    @Size(min = 1, max = 100)
    private String tenantId = null;

    @JsonProperty("moduleDetails")
    @Valid
    private List<ModuleDetail> moduleDetails = null;


    public MdmsCriteria addModuleDetailsItem(ModuleDetail moduleDetailsItem) {
        if (this.moduleDetails == null) {
            this.moduleDetails = new ArrayList<>();
        }
        this.moduleDetails.add(moduleDetailsItem);
        return this;
    }

}
