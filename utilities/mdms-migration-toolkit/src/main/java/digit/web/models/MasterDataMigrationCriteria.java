package digit.web.models;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MasterDataMigrationCriteria
 */
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2023-06-20T09:54:35.237+05:30[Asia/Calcutta]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MasterDataMigrationCriteria {

    @JsonProperty("tenantId")
    @NotNull
    @Size(min = 2, max = 100)
    private String tenantId = null;


}
