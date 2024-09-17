package digit.web.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This is acting ID token of the authenticated user on the server. Any value provided by the clients will be ignored and actual user based on authtoken will be used on the server.
 */
@Schema(description = "This is acting ID token of the authenticated user on the server. Any value provided by the clients will be ignored and actual user based on authtoken will be used on the server.")
@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2023-06-20T09:54:35.237+05:30[Asia/Calcutta]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User   {
        @JsonProperty("tenantId")
          @NotNull

                private String tenantId = null;

        @JsonProperty("id")

                private Integer id = null;

        @JsonProperty("uuid")

                private String uuid = null;

        @JsonProperty("userName")
          @NotNull

                private String userName = null;

        @JsonProperty("mobileNumber")

                private String mobileNumber = null;

        @JsonProperty("emailId")

                private String emailId = null;

        @JsonProperty("roles")
          @NotNull
          @Valid
                private List<Role> roles = new ArrayList<>();

        @JsonProperty("salutation")

                private String salutation = null;

        @JsonProperty("name")

                private String name = null;

        @JsonProperty("gender")

                private String gender = null;

        @JsonProperty("alternateMobileNumber")

                private String alternateMobileNumber = null;

        @JsonProperty("altContactNumber")

                private String altContactNumber = null;

        @JsonProperty("pan")

                private String pan = null;

        @JsonProperty("aadhaarNumber")

                private String aadhaarNumber = null;

        @JsonProperty("permanentAddress")

        @Size(max=300)         private String permanentAddress = null;

        @JsonProperty("permanentCity")

        @Size(max=300)         private String permanentCity = null;

        @JsonProperty("permanentPincode")

        @Size(max=6)         private String permanentPincode = null;

        @JsonProperty("correspondenceCity")

        @Size(max=50)         private String correspondenceCity = null;

        @JsonProperty("correspondencePincode")

        @Size(max=6)         private String correspondencePincode = null;

        @JsonProperty("correspondenceAddress")

        @Size(max=300)         private String correspondenceAddress = null;

        @JsonProperty("active")

                private Boolean active = null;

        @JsonProperty("locale")

        @Size(max=10)         private String locale = null;

        @JsonProperty("type")

        @Size(max=20)         private String type = null;

        @JsonProperty("accountLocked")

                private Boolean accountLocked = null;

        @JsonProperty("accountLockedDate")

                private Long accountLockedDate = null;

        @JsonProperty("fatherOrHusbandName")

        @Size(max=100)         private String fatherOrHusbandName = null;

        @JsonProperty("relationship")

        @Size(max=20)         private String relationship = null;

        @JsonProperty("signature")

                private String signature = null;

        @JsonProperty("bloodGroup")

        @Size(max=3)         private String bloodGroup = null;

        @JsonProperty("photo")

                private String photo = null;

        @JsonProperty("identificationMark")

                private String identificationMark = null;

        @JsonProperty("createdBy")

                private Long createdBy = null;

        @JsonProperty("password")

                private String password = null;

        @JsonProperty("otpReference")

                private String otpReference = null;

        @JsonProperty("lastModifiedBy")

                private Long lastModifiedBy = null;

        @JsonProperty("createdDate")

          @Valid
                private LocalDate createdDate = null;

        @JsonProperty("lastModifiedDate")

          @Valid
                private LocalDate lastModifiedDate = null;

        @JsonProperty("dob")

                private Long dob = null;

        @JsonProperty("pwdExpiryDate")

                private Long pwdExpiryDate = null;


        public User addRolesItem(Role rolesItem) {
        this.roles.add(rolesItem);
        return this;
        }

}
