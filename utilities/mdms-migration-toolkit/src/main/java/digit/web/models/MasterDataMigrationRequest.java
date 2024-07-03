package digit.web.models;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MasterDataMigrationRequest
 */
@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2023-06-20T09:54:35.237+05:30[Asia/Calcutta]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MasterDataMigrationRequest {
    @JsonProperty("masterDataMigrationCriteria")
    @Valid
    private MasterDataMigrationCriteria masterDataMigrationCriteria = null;

    @JsonProperty("requestInfo")
    @NotNull
    @Valid
    private RequestInfo requestInfo = null;


}
