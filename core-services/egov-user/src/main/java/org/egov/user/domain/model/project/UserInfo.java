package org.egov.user.domain.model.project;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Validated


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
public class UserInfo   {
        @JsonProperty("tenantId")
      @NotNull



    private String tenantId = null;

        @JsonProperty("uuid")
    


    private String uuid = null;

        @JsonProperty("userName")
      @NotNull



    private String userName = null;

        @JsonProperty("password")
    


    private String password = null;

        @JsonProperty("idToken")
    


    private String idToken = null;

        @JsonProperty("mobile")
    


    private String mobile = null;

        @JsonProperty("email")
    


    private String email = null;

        @JsonProperty("primaryrole")
      @NotNull

  @Valid


    private List<Role> primaryrole = new ArrayList<>();

        @JsonProperty("additionalroles")
    
  @Valid


    private List<TenantRole> additionalroles = null;


        public UserInfo addPrimaryroleItem(Role primaryroleItem) {
        this.primaryrole.add(primaryroleItem);
        return this;
        }

        public UserInfo addAdditionalrolesItem(TenantRole additionalrolesItem) {
            if (this.additionalroles == null) {
            this.additionalroles = new ArrayList<>();
            }
        this.additionalroles.add(additionalrolesItem);
        return this;
        }

}

