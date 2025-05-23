package org.egov.infra.mdms.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * ModuleDetail
 */
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2023-05-30T09:26:57.838+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ModuleDetail {
    @JsonProperty("moduleName")
    @NotNull

    @Size(min = 1, max = 100)
    private String moduleName = null;

    @JsonProperty("masterDetails")
    @NotNull
    @Valid
    private List<MasterDetail> masterDetails = new ArrayList<>();


    public ModuleDetail addMasterDetailsItem(MasterDetail masterDetailsItem) {
        this.masterDetails.add(masterDetailsItem);
        return this;
    }

}
