package digit.web.models;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Builder;

/**
 * Details of a user
 */
@Schema(description = "Details of a user")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-07-19T14:07:58.669830457+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
    @JsonProperty("tenantId")
    @NotNull

    @Size(min = 1, max = 50)
    private String tenantId = null;

    @JsonProperty("username")
    @NotNull

    @Size(min = 1, max = 50)
    private String username = null;

    @JsonProperty("password")
    @NotNull

    @Size(min = 6, max = 100)
    private String password = null;

    @JsonProperty("loginType")
    @NotNull
    @Size(min = 1, max = 20)
    private String loginType = null;

    @JsonProperty("roles")
    @NotNull

    private List<Object> roles = new ArrayList<>();


    public User addRolesItem(Object rolesItem) {
        this.roles.add(rolesItem);
        return this;
    }

}
