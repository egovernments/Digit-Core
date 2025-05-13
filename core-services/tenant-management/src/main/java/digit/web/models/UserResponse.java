package digit.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

import digit.web.models.User.User;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Builder;

/**
 * UserResponse
 */
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-08-12T11:40:14.091712534+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse   {
        @JsonProperty("responseInfo")

          @Valid
                private RequestInfo responseInfo = null;

        @JsonProperty("user")
          @Valid
                private List<User> user = null;


        public UserResponse addUserItem(User userItem) {
            if (this.user == null) {
            this.user = new ArrayList<>();
            }
        this.user.add(userItem);
        return this;
        }

}
